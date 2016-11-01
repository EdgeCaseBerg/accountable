package utils

object DisplayUtils {

	/** Converts an amount in cents to a formatted dollar string
	 *  @param amountInCents An amount in cents
	 *  @return A string formatted in dollars that is the same value as the amount in cents
	 */
	def centsToDollars(amountInCents: Long) = {
		"%d.%02d".format((amountInCents / 100), amountInCents - ((amountInCents / 100) * 100))
	}
}