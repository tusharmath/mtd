import org.scalatest.FunSpec
class RangeRequestSpec extends FunSpec {
  describe("createRanges()") {
    describe("when empty") {
      it("should return nothing") {
        val actual = RangeRequest.createRanges(0, 10, 0)
        assert(actual.isEmpty)
      }
    }
    describe("when size > 0") {
      it("should return ranges") {
        val expected = Some(
          List(
            RangeRequest(0, 5),
            RangeRequest(6, 10)
          )
        )
        val actual = RangeRequest.createRanges(10, 2, 0)
        assert(actual === expected)
      }
    }
    describe("when count=3") {
      it("should return ranges") {
        val expected = Some(
          List(
            RangeRequest(0, 3),
            RangeRequest(4, 7),
            RangeRequest(8, 10)
          )
        )
        val actual = RangeRequest.createRanges(10, 3, 0)
        assert(actual === expected)
      }
    }
    describe("when min is set") {
      it("should return 1 range") {
        val expected = Some(
          List(
            RangeRequest(0, 10)
          )
        )
        val actual = RangeRequest.createRanges(10, 3, 7)
        println(actual)
        assert(actual === expected)
      }
      it("should return 2 ranges") {
        val expected = Some(
          List(
            RangeRequest(0, 5),
            RangeRequest(6, 10)
          )
        )
        val actual = RangeRequest.createRanges(10, 3, 4)
        assert(actual === expected)
      }
    }
  }
  describe("getMaxParallelism()") {
    it("should return 1") {
      val actual   = RangeRequest.getMaxParallelism(10, 3, 6)
      val expected = 1
      assert(actual == expected)
    }

    it("should return 2") {
      val actual   = RangeRequest.getMaxParallelism(10, 3, 4)
      val expected = 2
      assert(actual == expected)
    }
  }
}
