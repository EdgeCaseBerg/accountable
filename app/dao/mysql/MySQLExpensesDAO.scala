package dao.mysql

import dao._
import models.domain._
import scala.concurrent.Future
import javax.inject.Inject
import java.time.Instant

class MySQLExpensesDAO @Inject() (mysqlConnector: MySQLConnector) extends ExpensesDAO {

	/** @inheritdoc
	 */
	def createNewExpense(expense: Expense): Future[Unit] = ???

	/** @inheritdoc
	 */
	def listExpensesDuringWeekOf(epochInstant: Instant): Future[List[Expense]] = ???

	/** @inheritdoc
	 */
	def listExpensesByGroupDuringWeekOf(epochInstant: Instant): Future[Map[ExpenseGroup, List[Expense]]] = ???

}