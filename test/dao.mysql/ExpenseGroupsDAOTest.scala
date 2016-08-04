package utils

import dao.mysql._
import dao.exceptions._
import models.domain._

import org.scalatest.{ FlatSpec, Matchers }
import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

class ExpenseGroupsDAOTest extends testhelpers.MigratedAndCleanDatabase {

	lazy val expenseGroupsDAO = new MySQLExpenseGroupsDAO(mySqlConnection)

	/** We have to use the expensesDAO in order to verify that expenses are
	 *  in the right groups after we do the operations with the expenseGroupsDAO.
	 *  if the ExpensesDAO tests don't pass, don't expect these ones too
	 */
	lazy val expensesDAO = new MySQLExpensesDAO(mySqlConnection)

	val fixtureExpense = Expense(amountInCents = 100, name = "Test Expense", dateOccured = 1467072000, expenseId = UUID.fromString("07f256f0-f830-495d-b6cb-fe39bb5f2f36"))
	val fixtureGroup = ExpenseGroup("FixtureGroup", UUID.fromString("b95bd45d-f654-42fc-8cea-7c7e8db230e9"))

	val myTestGroup = ExpenseGroup("TestGroup", UUID.fromString("b95bd45d-f654-42fb-8cea-7c7e8db230e9"))

	addSQLScriptForFlywayToLoad("conf/db/testFixtures/R__ExpenseGroupsDAOTestFixtures.sql")

	"The ExpenseGroupsDAO" should "create a new ExpenseGroup" in {
		whenReady(expenseGroupsDAO.createExpenseGroup(myTestGroup)) { unit =>
			assert(true)
		}
	}

	it should "map a data truncation error to an application specific error" in {
		val invalidGroup = myTestGroup.copy(name = "A" * 65) // Magic number comes from the migration and database definition
		whenReady(expenseGroupsDAO.createExpenseGroup(invalidGroup).failed) { exception =>
			exception shouldBe an[DataTooLargeException]
		}
	}

	it should "fail to create a duplicate group" in {
		whenReady(expenseGroupsDAO.createExpenseGroup(myTestGroup).failed) { exception =>
			exception shouldBe an[DuplicateDataException]
		}
	}

	it should "list the created expense groups and the ones in the database" in {
		whenReady(expenseGroupsDAO.listExpenseGroups()) { expenseGroups =>
			assert(expenseGroups.contains(fixtureGroup))
			assert(expenseGroups.contains(myTestGroup))
		}
	}

	it should "fail to add an expense that doesn't exist to a group" in {
		whenReady(expenseGroupsDAO.addExpenseToGroup(UUID.fromString("b95ad45d-f654-42fb-8cfa-7c7e8db230e7"), myTestGroup).failed) { exception =>
			exception shouldBe an[ExpenseDoesNotExistException]
		}
	}

	it should "fail to add an expense to a group that doesn't exist" in {
		whenReady(expenseGroupsDAO.addExpenseToGroup(fixtureExpense.expenseId, myTestGroup.copy(groupId = UUID.fromString("b95ad45d-f654-42fb-8cfa-7c7e8db230e7"))).failed) { exception =>
			exception shouldBe an[ExpenseGroupDoesNotExistException]
		}
	}

	it should "succeed in adding an existing expense to a group" in {
		val epoch = Instant.ofEpochSecond(fixtureExpense.dateOccured)
		val futureResults = for {
			_ <- expenseGroupsDAO.addExpenseToGroup(fixtureExpense.expenseId, myTestGroup)
			expensesByGroup <- expensesDAO.listExpensesByGroupDuringWeekOf(epoch)
		} yield expensesByGroup
		whenReady(futureResults) { expensesByGroup =>
			assert(expensesByGroup.contains(myTestGroup))
			expensesByGroup(myTestGroup).contains(fixtureExpense)
		}
	}

	it should "switch an expense's group if the expense is added to that group if it belonged to another group" in {
		val epoch = Instant.ofEpochSecond(fixtureExpense.dateOccured)
		val futureResults = for {
			_ <- expenseGroupsDAO.addExpenseToGroup(fixtureExpense.expenseId, fixtureGroup)
			expensesByGroup <- expensesDAO.listExpensesByGroupDuringWeekOf(epoch)
		} yield expensesByGroup
		whenReady(futureResults) { expensesByGroup =>
			assert(expensesByGroup.contains(fixtureGroup))
			expensesByGroup(fixtureGroup).contains(fixtureExpense)
		}
	}

}
