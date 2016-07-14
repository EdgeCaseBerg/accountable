package dao.mysql

import dao._
import models.domain._
import utils._

import scala.concurrent.{ Future, future, ExecutionContext }
import javax.inject.Inject
import java.time._
import java.time.format.DateTimeFormatter
import anorm._

class MySQLExpensesDAO @Inject() (mysqlConnector: MySQLConnector)(implicit executionContext: ExecutionContext) extends ExpensesDAO {

	/** @inheritdoc
	 */
	def createNewExpense(expense: Expense): Future[Unit] = {
		val insertOperation = Future {
			mysqlConnector.withTransaction { implicit connection =>
				val localTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(expense.dateOccured), ZoneId.systemDefault)
				val formattedLocalTime = DateTimeFormatter.ISO_LOCAL_DATE.format(localTime)
				val numberOfInsertedRow = SQL(
					"""INSERT INTO expenses (amountInCents,name,dateOccured,expenseId) VALUES ({amountInCents}, {name}, {dateOccured}, {expenseId})"""
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
	def listExpensesDuringWeekOf(epochInstant: Instant): Future[List[Expense]] = Future {
		val startOfWeek = TimeUtils.getWeekOf(epochInstant)
		val localTime = ZonedDateTime.ofInstant(startOfWeek, ZoneId.systemDefault)
		val formattedLocalTime = DateTimeFormatter.ISO_LOCAL_DATE.format(localTime)
		mysqlConnector.withReadOnlyConnection { implicit connection =>
			val sql = SQL("""
				SELECT expenses.amountInCents, expenses.name, expenses.dateOccured, expenses.expenseId FROM expenses
				WHERE expenses.dateOccured BETWEEN {weekStart} AND ({weekStart} + INTERVAL 1 WEEK)
			""").on("weekStart" -> formattedLocalTime).as(MySqlToDomainColumnParsers.expenseParser *)
			sql.toList
		}
	}

	/** @inheritdoc
	 */
	def listExpensesByGroupDuringWeekOf(epochInstant: Instant): Future[Map[ExpenseGroup, List[Expense]]] = Future {
		val startOfWeek = TimeUtils.getWeekOf(epochInstant)
		val localTime = ZonedDateTime.ofInstant(startOfWeek, ZoneId.systemDefault)
		val formattedLocalTime = DateTimeFormatter.ISO_LOCAL_DATE.format(localTime)
		mysqlConnector.withReadOnlyConnection { implicit connection =>
			val sql = SQL("""
					SELECT 
					expenses.amountInCents, expenses.name, expenses.dateOccured, expenses.expenseId, 
					case when eg.name is null then {defaultGroupName} else eg.name end as 'groupName',
					case when eg.groupId is null then {defaultGroupId} else eg.groupId end as 'groupId'
					FROM expenses 
					LEFT JOIN expenseGroupToExpense ON expenses.expenseId = expenseGroupToExpense.expenseId
					LEFT JOIN expenseGroups eg ON eg.groupId = expenseGroupToExpense.groupId
					WHERE expenses.dateOccured BETWEEN {weekStart} AND ({weekStart} + INTERVAL 1 WEEK)
					GROUP BY expenses.expenseId
			""").on(
				"weekStart" -> formattedLocalTime,
				"defaultGroupName" -> ungroupedGroup.name,
				"defaultGroupId" -> ungroupedGroup.groupId
			).as(MySqlToDomainColumnParsers.expensesJoinedToGroupsParser *)

			val mutableMap = scala.collection.mutable.Map[ExpenseGroup, List[Expense]]()
			sql.toList.foreach { expenseExpenseGroupTuple =>
				val (expense, group) = expenseExpenseGroupTuple
				if (mutableMap.contains(group)) {
					mutableMap(group) = (mutableMap(group).toSet ++ Set(expense)).toList
				} else {
					mutableMap(group) = List(expense)
				}
			}
			mutableMap.toMap
		}
	}

}