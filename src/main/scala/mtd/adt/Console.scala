package mtd.adt

trait Console[F[_]] {
  def getArgs(): F[Array[String]]
}
