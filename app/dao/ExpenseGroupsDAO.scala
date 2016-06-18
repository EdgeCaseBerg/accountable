package dao

import models.domain._
import scala.concurrent.Future
import java.util.UUID

/** Data Access Object Trait for expense groups
 *
 *  Data layer interface for accessing and retrieving information about expense groups.
 *  @note Contains only the neccesary methods to support the application
 */
trait ExpenseGroupsDAO {

	/** Creates a new expense group
	 *
	 *  @param expenseGroup The group to be persisted to the database
	 *  @return A failed Future if anything goes wrong, a successful Future otherwise
	 */
	def createExpenseGroup(expenseGroup: ExpenseGroup): Future[Unit]

	/** List all expense groups
	 *
	 *  @return A Future containing a list of ExpenseGroups
	 */
	def listExpenseGroups(): Future[List[ExpenseGroup]]

	/** Add an expense by its UUID to the given expenseGroup
	 *
	 *  @note An expense may only belong to one group, if the expense belongs to another group, than it will be moved to the one passed to this method
	 *
	 *  @param expenseId The UUID of the expense to be added to the group
	 *  @param expenseGroup The ExpenseGroup the expense should be added to
	 *  @return A failed Future if anything goes wrong, a successful Future otherwise
	 */
	def addExpenseToGroup(expenseId: UUID, expenseGroup: ExpenseGroup): Future[Unit]

}