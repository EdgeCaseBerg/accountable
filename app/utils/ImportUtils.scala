package utils

import models.domain._

object ImportUtils {
	/** Parses the given line of text into an Expense
	 *  @param lineItemString The string to parse, read from an accounts file of BGI
	 *  @see {@link https://github.com/EdgeCaseBerg/BGI/blob/maImportUtilsster/src/util/ctl.c#L548 Method from BGI that creates these line items}
	 *  @return the parsed Expense or the exception preventing parsing in an Either.
	 */
	def expenseFromLineItemBGIString(lineItemString: String): Either[Throwable, Expense] = ???

	/** Parses the given line of text into an ExpenseGroup
	 *  @see {@link https://github.com/EdgeCaseBerg/BGI/blob/master/src/util/ctl.c#L455 Method from BGI that updates accounts}
	 *  @return the parsed ExpenseGroup or the exception preventing parsing in an Either.
	 */
	def expenseGroupFromBGIString(accountString: String): Either[Throwable, ExpenseGroup] = ???
}