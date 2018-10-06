import cats._
import cats.data.OptionT
import cats.effect._
import cats.implicits._
import org.http4s.client.Client
import org.http4s.client.blaze.Http1Client
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Method, Request, Response, Uri}

import scala.util.Try
import mtd.adt._
import mtd.error._

object Main extends App {
  val ContentLengthHeader = CaseInsensitiveString("content-length")

  implicit val http = new Http[IO] {
    override def client(): IO[Client[IO]] = Http1Client[IO]()
  }

  implicit val logger = new Logger[IO] {
    private val logger            = org.log4s.getLogger
    def debug(msg: Any): IO[Unit] = IO { logger.debug(msg.toString) }
    def trace(msg: Any): IO[Unit] = IO { logger.trace(msg.toString) }
    def info(msg: Any): IO[Unit]  = IO { logger.info(msg.toString) }
    def warn(msg: Any): IO[Unit]  = IO { logger.warn(msg.toString) }
    def error(msg: Any): IO[Unit] = IO { logger.error(msg.toString) }
  }

  implicit val console = new Console[IO] {
    override def getArgs(): IO[Array[String]] = IO.pure(args)
  }

  def getContentLength[F[_]: Monad](response: Response[F]): OptionT[F, Int] = {
    val ContentLength = CaseInsensitiveString("content-length")
    for {
      header <- response.headers.get(ContentLength).toOptionT[F]
      length <- Try { header.value.toInt }.toOption.toOptionT[F]
    } yield length
  }

  def mkHeadRequest[F[_]](uri: Uri)(implicit H: Http[F],
                                    M: Monad[F]): F[Response[F]] = {
    for {
      client   <- H.client()
      response <- client.fetch(Request[F](Method.HEAD, uri))(M.pure)
    } yield response
  }

  def getURLArgs[F[_]: Monad](implicit C: Console[F]): OptionT[F, Uri] = {
    for {
      args     <- OptionT.liftF(C.getArgs())
      firstArg <- { if (args.length > 0) Some(args(0)) else None }.toOptionT[F]
      uri      <- Uri.fromString(firstArg).toOption.toOptionT[F]
    } yield uri
  }

  def program[F[_]](
      implicit L: Logger[F],
      H: Http[F],
      C: Console[F],
      M: MonadError[F, Throwable]
  ): F[Unit] = {
    for {
      _        <- L.info(s"ARGS: ${args.mkString(",")}")
      uri      <- getURLArgs[F].value.flatMap(_.liftTo[F](InvalidURL: Throwable))
      _        <- L.info(s"URI: $uri")
      response <- mkHeadRequest[F](uri)
      length <- getContentLength(response).value.flatMap(
                 _.liftTo[F](ContentLengthCouldNotBeDetected: Throwable))
      _ <- L.info(s"LENGTH: $length")
    } yield ()
  }

  program[IO].attempt
    .flatMap {
      case Left(err) => logger.error(err)
      case Right(_)  => IO.unit
    }
    .unsafeRunSync()
}
