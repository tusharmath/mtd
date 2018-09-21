import cats._
import cats.effect._
import cats.implicits._
import org.http4s.client.Client
import org.http4s.client.blaze.Http1Client
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Method, Request, Uri}

import scala.util.Try

object Main extends App {
  val ContentLengthHeader = CaseInsensitiveString("content-length")

  trait Http[F[_]] {
    def client(): F[Client[F]]
  }

  trait Logger[F[_]] {
    def debug(msg: Any): F[Unit]
    def trace(msg: Any): F[Unit]
    def info(msg: Any): F[Unit]
    def warn(msg: Any): F[Unit]
    def error(msg: Any): F[Unit]
  }

  implicit val http = new Http[IO] {
    override def client(): IO[Client[IO]] = Http1Client[IO]()
  }
  implicit val logger = new Logger[IO] {
    private val logger = org.log4s.getLogger

    def debug(msg: Any): IO[Unit] = IO { logger.debug(msg.toString) }
    def trace(msg: Any): IO[Unit] = IO { logger.trace(msg.toString) }
    def info(msg: Any): IO[Unit]  = IO { logger.info(msg.toString) }
    def warn(msg: Any): IO[Unit]  = IO { logger.warn(msg.toString) }
    def error(msg: Any): IO[Unit] = IO { logger.error(msg.toString) }
  }

  trait MTDError extends Throwable
  case class HttpHeaderMissing(headerName: String) extends MTDError {
    override def toString: String = s"Missing HTTP header: $headerName"
  }

  implicit class EitherOps[B](either: Either[Throwable, B]) {
    def liftF[F[_]](implicit M: ApplicativeError[F, Throwable]): F[B] = {
      either.fold(M.raiseError, M.pure)
    }
  }

  implicit class OptionOps[A](option: Option[A]) {
    def liftF[F[_]](e: Throwable)(
        implicit M: ApplicativeError[F, Throwable]): F[A] = {
      option match {
        case None        => M.raiseError(e)
        case Some(value) => M.pure(value)
      }
    }
  }

  def program[F[_]](url: String)(
      implicit L: Logger[F],
      H: Http[F],
      M: MonadError[F, Throwable]
  ): F[Unit] = {
    val ContentLength = CaseInsensitiveString("content-length")
    for {
      client   <- H.client()
      uri      <- Uri.fromString(url).liftF[F]
      _        <- L.debug(s"URI: $uri")
      response <- client.fetch(Request[F](Method.HEAD, uri))(M.pure)
      _        <- L.debug("Response recv")
      header <- response.headers
                 .get(ContentLength)
                 .liftF[F](HttpHeaderMissing("content-length"))
      length <- Try { header.value.toInt }.toEither.liftF[F]
      _      <- L.info(length)
    } yield ()
  }

  program[IO]("https://example.com").attempt
    .flatMap {
      case Left(err) => logger.error(err)
      case Right(_)  => IO.unit
    }
    .unsafeRunSync()
}
