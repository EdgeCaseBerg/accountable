package dao.mysql

import dao._
import dao.exceptions.ExpenseDoesNotExistException
import models.domain._
import utils._
import TimeUtils.LongAsInstant

import scala.concurrent.{ Future, future, ExecutionContext }
import javax.inject.Inject
import java.time._
import java.time.temporal.ChronoUnit.DAYS
import java.time.format.DateTimeFormatter
import java.util.UUID
import anorm._

class MySQLExpensesDAO @Inject() (mysqlConnector: MySQLConnector)(implicit executionContext: ExecutionContext) extends ExpensesDAO {

	/** @inheritdoc
	 */
	def createNewExpense(expense: Expense): Future[Unit] = {
		val insertOperation = Future {
			mysqlConnector.withTransaction { implicit connection =>
				val formattedLocalTime = TimeUtils.formatToUTCDateString(expense.dateOccured.toInstant)
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
		val formattedLocalTime = TimeUtils.formatToUTCDateString(startOfWeek)
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
		val formattedLocalTime = TimeUtils.formatToUTCDateString(startOfWeek)
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

	/** @inheritdoc
	 */
	def findExpenseById(expenseId: UUID): Future[Expense] = Future {
		mysqlConnector.withReadOnlyConnection { implicit connection =>
			val sql = SQL(
				"""SELECT expenses.amountInCents, expenses.name, expenses.dateOccured, expenses.expenseId FROM expenses
								 WHERE expenseId = {expenseId}
							"""
			).on("expenseId" -> expenseId).as(MySqlToDomainColumnParsers.expenseParser *)
			sql.toList.headOption.fold(
				throw new ExpenseDoesNotExistException(s"Could not find any expense by the ID ${expenseId}", null)
			)(identity)
		}
	}

	/** @inheritdoc
	 */
	def listOfAvailableWeeksWithExpenses(): Future[Seq[Instant]] = Future {
		mysqlConnector.withReadOnlyConnection { implicit connection =>
			val sql = SQL(
				"""SELECT MIN(dateOccured) as min FROM expenses"""
			).as(SqlParser.date("min") map { case m => m.getTime / 1000 } *)
			sql.toList.headOption.fold(Seq(Instant.now())) {
				case min =>
					/** Create a list, one per week, from the minimum to the maximum */
					val startWeek = TimeUtils.getWeekOf(min.toInstant)
					val thisWeek = TimeUtils.getWeekOf(Instant.now())
					val listBuffer = collection.mutable.ListBuffer(startWeek)
					var week = startWeek
					while (thisWeek.isAfter(week)) {
						week = week.plus(7, DAYS)
						listBuffer += week
					}
					listBuffer.toList
			}
		}
	}
}