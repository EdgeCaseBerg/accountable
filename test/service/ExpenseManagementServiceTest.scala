package service

import testhelpers.ServiceTestWithDB
import models.domain._
import java.util.UUID

import org.scalatest._
import matchers.ShouldMatchers._
import OptionValues._

class ExpenseManagementServiceTest() extends ServiceTestWithDB {

	var expenseGroup: Option[ExpenseGroup] = None

	addSQLScriptForFlywayToLoad("conf/db/testFixtures/R__ExpenseManagementServiceTestFixtures.sql")

	val fixtureExpenseId = UUID.fromString("07f256f0-f830-495d-b6cb-fe39bb5f2f36")

	val fixtureExpenseGroupId = UUID.fromString("b95bd45d-f654-42fb-8cea-7c7e8db230e9")

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
		withExpenseManagementService { expenseManagementService =>
			whenReady(expenseManagementService.listExpenseGroups()) { groups =>
				assert(groups.find(_.name == "Test Group").isDefined)
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
}