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

	/** @inheritdoc
	 */
	def createExpenseGroup(expenseGroup: ExpenseGroup): Future[Unit] = {
		val insertOperation = Future {
			mysqlConnector.withTransaction { implicit connection =>
				val numberOfInsertedRow = SQL(
					"""INSERT INTO expenseGroups (name,groupId) VALUES ({name}, {groupId})"""
				).on(
						"name" -> expenseGroup.name,
						"groupId" -> expenseGroup.groupId
					).executeUpdate()
				if (numberOfInsertedRow != 1) {
					throw new RuntimeException(s"Could not create expenseGroup with id of ${expenseGroup.groupId}")
				}
			}
			()
		}
		insertOperation
	}

	def listExpenseGroups(): Future[List[ExpenseGroup]] = ???

}