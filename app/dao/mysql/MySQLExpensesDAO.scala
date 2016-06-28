package dao.mysql

import dao._
import models.domain._
import scala.concurrent.{ Future, future, ExecutionContext }
import javax.inject.Inject
import java.time._
import java.time.format.DateTimeFormatter
import anorm._

class MySQLExpensesDAO @Inject() (mysqlConnector: MySQLConnector)(implicit executionContext: ExecutionContext) extends ExpensesDAO {

	/** @inheritdoc
	 */
	def createNewExpense(expense: Expense): Future[Unit] = {
		val insertOperation = future {
			mysqlConnector.withTransaction { implicit connection =>
				val localTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(expense.dateOccured), ZoneId.systemDefault)
				val formattedLocalTime = DateTimeFormatter.ISO_LOCAL_DATE.format(localTime)
				val numberOfInsertedRow = SQL(
					"""INSERT INTO expenses (amountInCents,name,dateOccured,expenseId) VALUES ({amountInCents}, {name}, {dateOccured}, expenseId)"""
				).on(
						"amountInCents" -> expense.amountInCents,
						"name" -> expense.name,
						"dateOccured" -> formattedLocalTime,
						"expenseId" -> expense.expenseId
					).executeUpdate()
				if (numberOfInsertedRow != 1) {
					throw new RuntimeException(s"Could not insert expense with id of ${expense.expenseId}")
				}
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