package dao.mysql

import anorm._
import anorm.SqlParser._

import java.util.{ UUID, Date }
import scala.util.{ Try, Success, Failure }

import models.domain._

import play.api.Logger

//val amountInCents: Long, val name: String, val dateOccured: Long, val expenseId: UUID = UUID.randomUUI

object MySqlToDomainColumnParsers {
	val expenseParser: RowParser[Expense] = {
		get[Long]("expenses.amountInCents") ~
			get[String]("expenses.name") ~
			get[Date]("expenses.dateOccured") ~
			get[String]("expenses.expenseId") map {
				case amountInCents ~ name ~ dateOccured ~ expenseId => {
					val epochFromDate = dateOccured.getTime / 1000
					Try(UUID.fromString(expenseId)) match {
						case Success(uuid) => Expense(amountInCents, name, epochFromDate, uuid)
						case Failure(_) => {
							Logger.error(s"Could not load Expense with expenseId of '$expenseId', generating new UUID for expense")
							Expense(amountInCents, name, epochFromDate)
						}
					}
				}
			}
	}

	val expenseGroupParser: RowParser[ExpenseGroup] = {
		get[String]("expenseGroups.name") ~
			get[String]("expenseGroups.groupId") map {
				case name ~ groupId => {
					Try(UUID.fromString(groupId)) match {
						case Success(uuid) => ExpenseGroup(name, uuid)
						case Failure(_) => {
							Logger.error(s"Could not load ExpenseGroup with groupId of '$groupId', generating temporary UUID for group")
							ExpenseGroup(name)
						}
					}
				}
			}
	}

	val expensesJoinedToGroupsParser: RowParser[Tuple2[Expense, ExpenseGroup]] = {
		expenseParser ~
			get[String]("groupName") ~
			get[String]("groupId") map {
				case expense ~ name ~ groupId => {
					val group = Try(UUID.fromString(groupId)) match {
						case Success(uuid) => ExpenseGroup(name, uuid)
						case Failure(_) => {
							Logger.error(s"Could not load ExpenseGroup with groupId of '$groupId', generating temporary UUID for group")
							ExpenseGroup(name)
						}
					}
					(expense, group)
				}
			}
	}
}