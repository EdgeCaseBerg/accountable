package service

import testhelpers.ServiceTestWithDB
import models.domain._

class ExpenseManagementServiceTest() extends ServiceTestWithDB {

	var expenseGroup: Option[ExpenseGroup] = None

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
}