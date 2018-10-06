package mtd.adt

trait Logger[F[_]] {
  def debug(msg: Any): F[Unit]
  def trace(msg: Any): F[Unit]
  def info(msg: Any): F[Unit]
  def warn(msg: Any): F[Unit]
  def error(msg: Any): F[Unit]
}
