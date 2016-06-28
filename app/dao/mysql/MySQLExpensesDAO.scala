package dao.mysql

import dao._
import models.domain._
import scala.concurrent.{ Future, future, ExecutionContext }
import javax.inject.Inject
import java.time.Instant
import anorm._

class MySQLExpensesDAO @Inject() (mysqlConnector: MySQLConnector)(implicit executionContext: ExecutionContext) extends ExpensesDAO {

	/** @inheritdoc
	 */
	def createNewExpense(expense: Expense): Future[Unit] = {
		val insertOperation = future {
			mysqlConnector.withTransaction { implicit connection =>
				val possibleNewId = SQL(
					"""INSERT INTO expenses (amountInCents,name,dateOccured,expenseId) VALUES ({amountInCents}, {name}, {dateOccured}, expenseId)"""
				).on(
						"amountInCents" -> expense.amountInCents,
						"name" -> expense.name,
						"dateOccured" -> expense.dateOccured,
						"expenseId" -> expense.expenseId
					).executeInsert()
				possibleNewId.fold(throw new RuntimeException(s"Could not save expense ${expense.expenseId}"))(identity)
			}
			()
		}
		insertOperation
	}
	/** @inheritdoc
	 */
	def listExpensesDuringWeekOf(epochInstant: Instant): Future[List[Expense]] = ???

	/** @inheritdoc
	 */
	def listExpensesByGroupDuringWeekOf(epochInstant: Instant): Future[Map[ExpenseGroup, List[Expense]]] = ???

}