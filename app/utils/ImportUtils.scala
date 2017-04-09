package utils

import models.domain._
import java.util.Scanner
import scala.util.{ Try, Success, Failure }

object ImportUtils {

	/** Parses the given line of text into an Expense
	 *  @param lineItemString The string to parse, read from an accounts file of BGI
	 *  @see {@link https://github.com/EdgeCaseBerg/BGI/blob/master/src/util/ctl.c#L548 Method from BGI that creates these line items}
	 *  @return the parsed Expense or the exception preventing parsing in an Either.
	 */
	def expenseFromLineItemBGIString(lineItemString: String): Either[Throwable, Expense] = {
		val lineItemScanner: Scanner = new Scanner(lineItemString)
		try {
			val epochSecond = lineItemScanner.nextLong()
			val name = lineItemScanner.next("[^0-9]+".r.pattern)
			val amountString = lineItemScanner.next("[0-9]+.[0-9]{2}".r.pattern)
			val amountWholePart = amountString.split('.')(0).toInt
			val amountCentsPart = amountString.split('.')(1).toInt
			val amountInCents = (amountWholePart * 100) + amountCentsPart
			Right(Expense(amountInCents, name, epochSecond))
		} catch {
			case e: Throwable => Left(e)
		} finally {
			lineItemScanner.close()
		}
	}

	/** Parses the given line of text into an ExpenseGroup
	 *  @see {@link https://github.com/EdgeCaseBerg/BGI/blob/master/src/util/ctl.c#L455 Method from BGI that updates accounts}
	 *  @return the parsed ExpenseGroup or the exception preventing parsing in an Either.
	 */
	def expenseGroupFromBGIString(accountString: String): Either[Throwable, ExpenseGroup] = ???

}