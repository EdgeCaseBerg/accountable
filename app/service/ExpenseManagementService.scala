package service

import dao._
import dao.exceptions._
import models.domain._

import javax.inject.Inject
import java.time.Instant
import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global // For now, Change later

/** Service to coordinate actions involving both ExpenseGroups and Expenses
 *
 *  @note The constructor to ExpenseManagementService is annotated with javax.inject.Inject for use by dependency injection frameworks.
 *
 *  @param expenseGroupsDAO An instance of a class implementing the ExpenseGroupsDAO trait
 *  @param expensesDAO An instance of a class implementing the ExpensesDAO trait
 */
class ExpenseManagementService @Inject() (expenseGroupsDAO: ExpenseGroupsDAO, expensesDAO: ExpensesDAO) {

	/** Creates and persists an ExpenseGroup to the database
	 *
	 *  @param name The name of the ExpenseGroup to create
	 *
	 *  @return The created ExpenseGroup in the result of a future, or an exception
	 */
	def createNewExpenseGroupWithName(name: String): Future[ExpenseGroup] = {
		val expenseGroup = ExpenseGroup(name)
		expenseGroupsDAO.createExpenseGroup(expenseGroup).map { _ =>
			expenseGroup
		}.recover {
			case e: DAOException => throw e // No real backup plan, so just throw the exception
		}
	}

	/** List all expense groups
	 *
	 *  @return A Future containing a list of ExpenseGroups
	 */
	def listExpenseGroups() = expenseGroupsDAO.listExpenseGroups()

	/** Persist an Expense and link it to an ExpenseGroup
	 *
	 *  @note If the expense already exists this method will simply add it to the group
	 *
	 *  @param expense The Expense to persist to the underlying DAO
	 *  @param expenseGroup The group to add the expense to once it's persisted
	 */
	def createExpenseAndAddToGroup(expense: Expense, expenseGroup: ExpenseGroup): Future[Unit] = {
		expensesDAO.createNewExpense(expense).map { _ =>
			expense
		}.recover {
			case e: DuplicateDataException => expense // If it's duplicate that's fine, just add it to the group
		}.map { createdExpense =>
			expenseGroupsDAO.addExpenseToGroup(expense.expenseId, expenseGroup)
		}
	}

	/** List the expenses for the current week
	 */
	def listCurrentWeeksCurrentExpenses(): Future[List[Expense]] = {
		expensesDAO.listExpensesDuringWeekOf(Instant.now())
	}

	/** List the expenses, grouped by their ExpenseGroup, for the current week
	 */
	def listCurrentWeeksCurrentExpensesWithGroup(): Future[Map[ExpenseGroup, List[Expense]]] = {
		expensesDAO.listExpensesByGroupDuringWeekOf(Instant.now())
	}

}