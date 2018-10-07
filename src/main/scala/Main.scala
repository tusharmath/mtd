import cats.effect._
import mtd.adt._
import org.http4s.client.Client
import org.http4s.client.blaze.Http1Client

object Main extends App {
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

  Download
    .program[IO]
    .attempt
    .flatMap {
      case Left(err) => logger.error(err)
      case Right(_)  => IO.unit
    }
    .unsafeRunSync()
}
