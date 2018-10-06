package mtd.adt
import org.http4s.client.Client

trait Http[F[_]] {
  def client(): F[Client[F]]
}
