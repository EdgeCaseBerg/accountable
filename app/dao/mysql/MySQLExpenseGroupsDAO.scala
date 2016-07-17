package dao.mysql

import dao._
import models.domain._
import utils._

import scala.concurrent.{ Future, future, ExecutionContext }
import javax.inject.Inject
import java.util.UUID

import anorm._

class MySQLExpenseGroupsDAO @Inject() (mysqlConnector: MySQLConnector)(implicit executionContext: ExecutionContext) extends ExpenseGroupsDAO {

	def addExpenseToGroup(expenseId: UUID, expenseGroup: ExpenseGroup): Future[Unit] = ???
	def createExpenseGroup(expenseGroup: ExpenseGroup): Future[Unit] = ???
	def listExpenseGroups(): Future[List[ExpenseGroup]] = ???

}