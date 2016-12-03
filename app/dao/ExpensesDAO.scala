package dao

import models.domain._
import scala.concurrent.Future
import java.time.Instant
import java.util.UUID

/** Data Access Object Trait for expenses
 *
 *  Data layer interface for accessing and retrieving information about expenses.
 *  @note Contains only the neccesary methods to support the application
 */
trait ExpensesDAO {

	/** A group that serves as a catch all for any expenses that do not belong
	 *  in a group when listExpensesByGroupDuringWeekOf
	 */
	final val ungroupedGroup = ExpenseGroup("ungrouped")

	/** Creates a new expense in the database.
	 *
	 *  Implementation Detail: If I was using an auto-increment column for the ID I
	 *  would return an Expense from this, but since we're using just UUID's we
	 *  don't need a round trip to the database to find out the ID of the entity
	 *  that was persisted as we already have it.
	 *
	 *  @param expense An Expense object to save to the database
	 *  @return A failed Future if anything goes wrong, a successful Future otherwise
	 */
	def createNewExpense(expense: Expense): Future[Unit]

	/** Lists the expenses that took place during the same week as the given Instant
	 *
	 *  See [[utils.TimeUtils]] (This has not been created yet) for how an instant is
	 *  converted to a week period.
	 *
	 *  @param epochInstant An instance in time that falls in the same week as the one you want expenses for
	 *  @return A Future containing a list of expenses that took place during the same week as the instant given
	 */
	def listExpensesDuringWeekOf(epochInstant: Instant): Future[List[Expense]]

	/** Lists the expenses that took place during the same week as the given instance, keyed in a map by their group
	 *
	 *  @note If an expense is not in a group, it will be placed into the "ungroupedGroup" group, you cannot rely on this group's UUID to be the same between requests
	 *
	 *  See [[utils.TimeUtils]] (This has not been created yet) for how an instant is
	 *  converted to a week period.
	 *
	 *  @param epochInstant An instance in time that falls in the same week as the one you want expenses for
	 *  @param A Future containing a map of ExpenseGroup to the list of expenses that occured in the same week as the instant given
	 */
	def listExpensesByGroupDuringWeekOf(epochInstant: Instant): Future[Map[ExpenseGroup, List[Expense]]]

	/** Retrieves an expense by its ID or fails.
	 *  @param expenseId The UUID of the Expense
	 *  @return A future containing the expense identified by the given expenseId, if not found a failed future will be returned.
	 */
	def findExpenseById(expenseId: UUID): Future[Expense]

	/** Retrieve a list of week's one could request a summary or expenses for
	 *  @return A future list of Instances each being the start of an expense week
	 */
	def listOfAvailableWeeksWithExpenses(): Future[Seq[Instant]]

}