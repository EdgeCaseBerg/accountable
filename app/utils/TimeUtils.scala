package utils

import java.time._
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS

/** A utility for dealing with time
 */
object TimeUtils {
	/** Based on the given epoch date, return the starting epoch time of that week
	 *
	 *  @note the start of a week is defined as the Sunday most recently in the past, 0:00 Hours
	 *
	 *  @param epochInstant The time to retrieve a week for
	 *  @return The Instant in time when the week began
	 */
	def getWeekOf(epochInstant: Instant): Instant = {
		val currentLocalDateTime: LocalDate = LocalDateTime.ofInstant(epochInstant, ZoneId.of("UTC")).toLocalDate()
		val startOfTheWeek = currentLocalDateTime.getDayOfWeek match {
			case DayOfWeek.SUNDAY => currentLocalDateTime.atStartOfDay(ZoneId.of("UTC"))
			case _ => {
				val daysToSubtract = ((7 + currentLocalDateTime.getDayOfWeek().getValue()) % 8) + 1
				currentLocalDateTime.minusDays(daysToSubtract).atStartOfDay(ZoneId.of("UTC"))
			}
		}
		Instant.from(startOfTheWeek)
	}

	def getYearOfInstant(epochInstant: Instant): Int = {
		val currentLocalDateTime: LocalDate = LocalDateTime.ofInstant(epochInstant, ZoneId.of("UTC")).toLocalDate()
		DateTimeFormatter.ofPattern("yyyy").format(currentLocalDateTime).toInt
	}

	def getMonthOfInstant(epochInstant: Instant): Int = {
		val currentLocalDateTime: LocalDate = LocalDateTime.ofInstant(epochInstant, ZoneId.of("UTC")).toLocalDate()
		DateTimeFormatter.ofPattern("MM").format(currentLocalDateTime).toInt
	}

	def getMonthOfInstantAsString(epochInstant: Instant): String = {
		val currentLocalDateTime: LocalDate = LocalDateTime.ofInstant(epochInstant, ZoneId.of("UTC")).toLocalDate()
		DateTimeFormatter.ofPattern("MMMM").format(currentLocalDateTime)
	}

	/** Helper to quickly get the current epoch second */
	def now(): Long = {
		Instant.now().getEpochSecond()
	}

	/** Helper to get the current epoch second in an html5 format */
	def html5Now(): String = {
		epochSecondsToHtml5DateString(now)
	}

	/** Implicit to quickly convert a long into an Instant value
	 *
	 *  @param long The Long to convert by calling #toInstant on it
	 */
	implicit class LongAsInstant(long: Long) {
		/** Convert a long into an Instant instance
		 *  @return An Instant based on interpretting the given long as an epoch time
		 */
		def toInstant = Instant.ofEpochSecond(long)
	}

	/** Format an Instant to be ready for insertion into a database Date format of YYYY-MM-DD, zoning the instant to UTC time
	 *  @param epochInstant The time to format
	 *  @return A string representation of the date, formatted into UTC YYYY-MM-DD format
	 */
	def formatToUTCDateString(epochInstant: Instant): String = {
		val localTime = ZonedDateTime.ofInstant(epochInstant, ZoneId.of("UTC")).truncatedTo(DAYS)
		DateTimeFormatter.ISO_LOCAL_DATE.format(localTime)
	}

	/** Format that HTML5 input[type=date] use for their value */
	val html5DateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

	/** Convert a string from a date input value to epoch seconds
	 *  @param yyyy-MM-dd The date string in yyyy-MM-dd format, such as 2001-9-11
	 *  @return The given date represented in epoch seconds
	 */
	def html5StringToEpochSecond(`yyyy-MM-dd`: String) = {
		LocalDate.parse(`yyyy-MM-dd`, html5DateFormat).atStartOfDay.toEpochSecond(ZoneOffset.UTC)
	}

	/** Converts epoch seconds to a date string matching html5's date input format
	 *  @param epochSeconds The epoch seconds to convert
	 *  @return A string of the form yyyy-MM-dd corresponding to the given epoch seconds
	 */
	def epochSecondsToHtml5DateString(epochSeconds: Long) = {
		val zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.of("UTC")).truncatedTo(DAYS)
		html5DateFormat.format(zonedDateTime)
	}

}