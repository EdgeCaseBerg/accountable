package utils

import dao.mysql._
import models.domain._

import org.scalatest.{ FlatSpec, Matchers }
import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

class ExpensesDAOTest extends testhelpers.MigratedAndCleanDatabase {

	lazy val expensesDAO = new MySQLExpensesDAO(mySqlConnection)

	val myTestExpense = Expense(amountInCents = 100, name = "Test Expense", dateOccured = 1467083533)

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

}
