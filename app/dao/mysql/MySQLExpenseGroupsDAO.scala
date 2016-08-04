package dao.mysql

import dao._
import models.domain._
import utils._

import scala.concurrent.{ Future, future, ExecutionContext }
import javax.inject.Inject
import java.util.UUID

import anorm._

class MySQLExpenseGroupsDAO @Inject() (mysqlConnector: MySQLConnector)(implicit executionContext: ExecutionContext) extends ExpenseGroupsDAO {

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

	/** @inheritdoc
	 */
	def listExpenseGroups(): Future[List[ExpenseGroup]] = Future {
		mysqlConnector.withReadOnlyConnection { implicit connection =>
			val sql = SQL("""
				SELECT expenseGroups.name, expenseGroups.groupId FROM expenseGroups
			""").as(MySqlToDomainColumnParsers.expenseGroupParser *)
			sql.toList
		}
	}

	/** @inheritdoc
	 */
	def addExpenseToGroup(expenseId: UUID, expenseGroup: ExpenseGroup) = Future {
		mysqlConnector.withTransaction { implicit connection =>
			val numberOfInsertedRow = SQL("""
				INSERT INTO expenseGroupToExpense (expenseId, groupId) VALUES ({expenseId}, {groupId}) ON DUPLICATE KEY UPDATE groupId = {groupId}
				""").on(
				"expenseId" -> expenseId,
				"groupId" -> expenseGroup.groupId
			).executeUpdate()
			if (numberOfInsertedRow < 1) {
				throw new RuntimeException(s"Could not add expenseId ${expenseId} to group ${expenseGroup.name}")
			}
			()
		}
	}

}