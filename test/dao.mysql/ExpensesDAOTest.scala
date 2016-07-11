package utils

import dao.mysql._
import models.domain._

import org.scalatest.{ FlatSpec, Matchers }
import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class ExpensesDAOTest extends testhelpers.MigratedAndCleanDatabase {

	lazy val expensesDAO = new MySQLExpensesDAO(mySqlConnection)

	val myTestExpense = Expense(amountInCents = 100, name = "Test Expense", dateOccured = 1467083533)

	val fixtureGroup = ExpenseGroup("TestGroup", UUID.fromString("b95bd45d-f654-42fb-8cea-7c7e8db230e9"))

	addSQLScriptForFlywayToLoad("conf/db/testFixtures/R__ExpensesDAOTestFixtures.sql")

	"The ExpensesDAO" should "create a new Expense" in {
		whenReady(expensesDAO.createNewExpense(myTestExpense)) { unit =>
			assert(true)
		}
	}

	it should "return the created expense when listing the week it is from" in {
		whenReady(expensesDAO.listExpensesDuringWeekOf(Instant.ofEpochSecond(myTestExpense.dateOccured))) { expensesList =>
			expensesList.find(_.expenseId == myTestExpense.expenseId).fold(fail("Did not load saved expense")) { theLoadedExpense =>
				assertResult(myTestExpense)(theLoadedExpense)
			}
		}
	}

	it should "return expenses by their group" in {
		val epoch = Instant.ofEpochSecond(myTestExpense.dateOccured)
		whenReady(expensesDAO.listExpensesByGroupDuringWeekOf(epoch)) { groupToExpenseMap =>
			/** It should have 2 because the fixture has 1 group and the newly created one is ungrouped
			 */
			assertResult(2)(groupToExpenseMap.size)
			groupToExpenseMap.contains(fixtureGroup)
			groupToExpenseMap.contains(expensesDAO.ungroupedGroup)
		}
	}

}
