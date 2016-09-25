package dao.mysql

import dao.exceptions._
import models.domain._

import org.scalatest.{ FlatSpec, Matchers }
import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class ExpensesDAOTest extends testhelpers.MigratedAndCleanDatabase {

	lazy val expensesDAO = new MySQLExpensesDAO(mySqlConnection)

	val myTestExpense = Expense(amountInCents = 100, name = "Test Expense", dateOccured = 1466985600)

	val fixtureGroup = ExpenseGroup("TestGroup", UUID.fromString("b95bd45d-f654-42fb-8cea-7c7e8db230e9"))

	addSQLScriptForFlywayToLoad("conf/db/testFixtures/R__ExpensesDAOTestFixtures.sql")

	"The ExpensesDAO" should "create a new Expense" in {
		whenReady(expensesDAO.createNewExpense(myTestExpense)) { unit =>
			assert(true)
		}
	}

	it should "map a data truncation error to an application specific error" in {
		val invalidExpense = myTestExpense.copy(name = "A" * 513) // Magic number comes from the migration and database definition
		whenReady(expensesDAO.createNewExpense(invalidExpense).failed) { exception =>
			exception shouldBe an[DataTooLargeException]
		}
	}

	it should "return the created expense when listing the week it is from" in {
		whenReady(expensesDAO.listExpensesDuringWeekOf(Instant.ofEpochSecond(myTestExpense.dateOccured))) { expensesList =>
			expensesList.find(_.expenseId == myTestExpense.expenseId).fold(fail("Did not load saved expense")) { theLoadedExpense =>
				assertResult(myTestExpense)(theLoadedExpense)
			}
		}
	}

	it should "return ungrouped expenses in a default group" in {
		val epoch = Instant.ofEpochSecond(myTestExpense.dateOccured)
		whenReady(expensesDAO.listExpensesByGroupDuringWeekOf(epoch)) { groupToExpenseMap =>
			assertResult(1)(groupToExpenseMap.size)
			assert(groupToExpenseMap.contains(expensesDAO.ungroupedGroup))
			groupToExpenseMap(expensesDAO.ungroupedGroup).find(_.expenseId == myTestExpense.expenseId).fold(fail("Did not find expense in group")) { theFoundExpense =>
				assertResult(myTestExpense)(theFoundExpense)
			}
		}
	}

	it should "return grouped expenses in their group" in {
		val epoch = Instant.ofEpochSecond(1435449600)
		whenReady(expensesDAO.listExpensesByGroupDuringWeekOf(epoch)) { groupToExpenseMap =>
			assertResult(1)(groupToExpenseMap.size)
			assert(groupToExpenseMap.contains(fixtureGroup))
		}
	}

}
