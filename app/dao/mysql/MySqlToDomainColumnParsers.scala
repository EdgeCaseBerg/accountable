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
}