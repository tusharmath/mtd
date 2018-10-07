import Main.args
import cats.data.OptionT
import cats.implicits._
import cats.{Monad, MonadError}
import mtd.adt.{Console, Http, Logger}
import mtd.error.{ContentLengthCouldNotBeDetected, InvalidURL}
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Header, Method, Request, Response, Uri}

import scala.annotation.tailrec
import scala.util.Try
object Download {
  def canAcceptRangedRequests[F[_]](response: Response[F]): Boolean = {
    val AcceptRanges = CaseInsensitiveString("accept-ranges")
    response.headers.get(AcceptRanges) match {
      case Some(header) => header.value == "bytes"
      case None         => false
    }
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
  def getURLFromArgs[F[_]: Monad](implicit C: Console[F]): OptionT[F, Uri] = {
    for {
      args     <- OptionT.liftF(C.getArgs())
      firstArg <- { if (args.length > 0) Some(args(0)) else None }.toOptionT[F]
      uri      <- Uri.fromString(firstArg).toOption.toOptionT[F]
    } yield uri
  }
  def mkRangeRequest[F[_]](uri: Uri, range: (Int, Int))(
      implicit H: Http[F],
      M: Monad[F]): F[Stream[Byte]] = {
    ???
  }

  def program[F[_]](
      implicit L: Logger[F],
      H: Http[F],
      C: Console[F],
      M: MonadError[F, Throwable]
  ): F[Unit] = {
    for {
      args     <- C.getArgs()
      _        <- L.info(s"ARGS: ${args.mkString(",")}")
      uri      <- getURLFromArgs[F].value.flatMap(_.liftTo[F](InvalidURL: Throwable))
      _        <- L.info(s"URI: $uri")
      response <- mkHeadRequest[F](uri)
      _        <- L.info(s"RANGE_REQUEST: ${canAcceptRangedRequests(response)}")

      length <- getContentLength(response).value.flatMap(
                 _.liftTo[F](ContentLengthCouldNotBeDetected: Throwable))
      _ <- L.info(s"LENGTH: $length")
    } yield ()
  }
}
