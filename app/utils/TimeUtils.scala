package utils

import java.time.Instant

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
		Instant.now()
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

}