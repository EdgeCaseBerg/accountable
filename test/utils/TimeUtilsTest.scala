package utils

import org.scalatest.{ FlatSpec, Matchers }
import TimeUtils._
import java.time.temporal.ChronoUnit.DAYS

class TimeUtilsTest extends FlatSpec with Matchers {

	val sunday12Am = 1466294400
	val testCasesThatReturnSunday = (0 until 7).map(t => sunday12Am + 86400 * t)
	val midDayOfEachTestCaseThatReturnsSunday = testCasesThatReturnSunday.map(_ + 86400 / 2)
	val nextSunday12Am = 1466899200
	val previousSunday = 1465689600

	"getWeekOf" should "return the next sunday if the time given falls in it" in {
		assertResult(nextSunday12Am.toInstant) {
			TimeUtils.getWeekOf(sunday12Am.toInstant.plus(8, DAYS))
		}
	}

	it should "return the previousSunday if the time given falls in it" in {
		assertResult(previousSunday.toInstant) {
			TimeUtils.getWeekOf(sunday12Am.toInstant.minus(2, DAYS))
		}
	}

	for (testCase <- testCasesThatReturnSunday ++ midDayOfEachTestCaseThatReturnsSunday) {
		it should s"return ${sunday12Am} when given ${testCase}" in {
			assertResult(sunday12Am.toInstant) {
				TimeUtils.getWeekOf(testCase.toInstant)
			}
		}
	}

	"html5StringToEpochSecond" should "convert 2001-09-11 to 1000166400" in {
		assertResult(1000166400)(html5StringToEpochSecond("2001-09-11"))
	}

	"epochSecondsToHtml5DateString" should "convert 1000166400 to 2001-09-11" in {
		assertResult("2001-09-11")(epochSecondsToHtml5DateString(1000166400))
	}
}
