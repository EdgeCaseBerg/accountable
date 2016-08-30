package service

import testhelpers.ServiceTestWithDB
import models.domain._
import java.util.UUID
import java.time.Instant

import org.scalatest._
import matchers.ShouldMatchers._
import OptionValues._

import scala.concurrent.ExecutionContext.Implicits.global

class ExpenseManagementServiceTest() extends ServiceTestWithDB {

	var expenseGroup: Option[ExpenseGroup] = None

	addSQLScriptForFlywayToLoad("conf/db/testFixtures/R__ExpenseManagementServiceTestFixtures.sql")

	val fixtureExpenseId = UUID.fromString("07f256f0-f830-495d-b6cb-fe39bb5f2f36")

	val fixtureExpenseGroupId = UUID.fromString("b95bd45d-f654-42fb-8cea-7c7e8db230e9")

	var testExpense: Option[Expense] = None

	"The ExpenseManagementService" should "make an expense group by name" in {
		withExpenseManagementService { expenseManagementService =>
			val expenseGroupName = "Test Group"
			whenReady(expenseManagementService.createNewExpenseGroupWithName(expenseGroupName)) { madeExpenseGroup =>
				expenseGroup = Option(madeExpenseGroup)
				assertResult(expenseGroupName)(madeExpenseGroup.name)
			}
		}
	}

	it should "list the newly created group" in {
		assume(expenseGroup.isDefined)
		withExpenseManagementService { expenseManagementService =>
			whenReady(expenseManagementService.listExpenseGroups()) { groups =>
				assert(groups.find(_.name == expenseGroup.get.name).isDefined)
			}
		}
	}

	it should "list the current week's expense (loaded from fixture)" in {
		withExpenseManagementService { expenseManagementService =>
			whenReady(expenseManagementService.listCurrentWeeksCurrentExpenses()) { expenses =>
				assertResult(fixtureExpenseId)(expenses.head.expenseId)
			}
		}
	}

	it should "list the current week's expense by group (loaded from fixture)" in {
		withExpenseManagementService { expenseManagementService =>
			whenReady(expenseManagementService.listCurrentWeeksCurrentExpensesWithGroup()) { expensesByGroup =>
				val fixtureGroup = expensesByGroup.find { case (group, listOfExpenses) => group.groupId == fixtureExpenseGroupId }
				val (group, listOfExpenses) = fixtureGroup.value
				assertResult(fixtureExpenseGroupId)(group.groupId)
				assertResult(fixtureExpenseId)(listOfExpenses.head.expenseId)
			}
		}
	}

	it should "persist a new expense to a group and appear in the listing" in {
		assume(expenseGroup.isDefined)
		withExpenseManagementService { expenseManagementService =>
			val newExpense = Expense(100, "Test Expense", Instant.now().getEpochSecond())
			val futureGroups = for {
				_ <- expenseManagementService.createExpenseAndAddToGroup(newExpense, expenseGroup.get)
				expensesByGroup <- expenseManagementService.listCurrentWeeksCurrentExpensesWithGroup()
			} yield expensesByGroup

			whenReady(futureGroups) { expensesByGroup =>
				assert(expensesByGroup.contains(expenseGroup.get))
				val expense = expensesByGroup(expenseGroup.get).find(_.expenseId == newExpense.expenseId).value
				assertResult(newExpense)(expense)
				testExpense = Option(newExpense)
			}
		}
	}

	it should "switch an existing expense's group if createExpenseAndAddToGroup called with one" in {
		assume(testExpense.isDefined)
		withExpenseManagementService { expenseManagementService =>
			val futureGroups = for {
				_ <- expenseManagementService.createExpenseAndAddToGroup(testExpense.get, ExpenseGroup("Doesn't matter", fixtureExpenseGroupId))
				expensesByGroup <- expenseManagementService.listCurrentWeeksCurrentExpensesWithGroup()
			} yield expensesByGroup

			whenReady(futureGroups) { expensesByGroup =>
				val (group, listOfExpenses) = expensesByGroup.find { case (group, listOfExpenses) => group.groupId == fixtureExpenseGroupId }.value
				assert(listOfExpenses.find(_.expenseId == testExpense.get.expenseId).isDefined)
			}
		}
	}
}