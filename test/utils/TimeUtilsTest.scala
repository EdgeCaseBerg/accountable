package utils

import org.scalatest.{ FlatSpec, Matchers }
import TimeUtils._
import java.time.temporal.ChronoUnit.DAYS

class TimeUtilsTest extends FlatSpec with Matchers {

	val sunday12Am = 1466294400
	val testCasesThatReturnSunday = (0 until 7).map(t => sunday12Am + 86400 * t)
	val midDayOfEachTestCaseThatReturnsSunday = testCasesThatReturnSunday.map(_ + 86400 / 2)
	val nextSunday12Am = 1466985600

	"getWeekOf" should "return the next sunday if the time given falls in it" in {
		assertResult(nextSunday12Am.toInstant) {
			TimeUtils.getWeekOf(sunday12Am.toInstant.plus(8, DAYS))
		}
	}

	for (testCase <- testCasesThatReturnSunday ++ midDayOfEachTestCaseThatReturnsSunday) {
		it should s"return ${sunday12Am} when given ${testCase}" in {
			assertResult(sunday12Am.toInstant) {
				TimeUtils.getWeekOf(testCase.toInstant)
			}
		}
	}
}
