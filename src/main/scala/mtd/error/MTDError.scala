package mtd.error

sealed trait MTDError                  extends Throwable
object ContentLengthCouldNotBeDetected extends MTDError
object InvalidURL                      extends MTDError
