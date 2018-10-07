import org.http4s.Header

import scala.annotation.tailrec

case class RangeRequest(start: Int, end: Int) {
  def header: Header = Header("range", s"bytes=$start-$end")
}

object RangeRequest {
  @tailrec
  def getMaxParallelism(size: Int, maxParallelism: Int, minSize: Int): Int = {
    val rangeSize = size / maxParallelism
    if (maxParallelism <= 1) 1
    else if (rangeSize < minSize)
      getMaxParallelism(size, maxParallelism - 1, minSize)
    else maxParallelism
  }

  def createRanges(size: Int,
                   maxParallelism: Int,
                   minRange: Int): Option[List[RangeRequest]] = {
    if (size == 0) None
    else {
      val parallelism = getMaxParallelism(size, maxParallelism, minRange)
      val rangeSize   = size / parallelism + 1
      Some((0 until parallelism).toList.map(i => {
        val start = i * rangeSize
        val stop  = Math.min(start + rangeSize - 1, size)
        RangeRequest(start, stop)
      }))
    }
  }
}
