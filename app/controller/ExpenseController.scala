package controller

import play.api.mvc._
import javax.inject.Inject
import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext
import service._

class ExpenseController @Inject() (expenseManagementService: ExpenseManagementService, executionContext: ExecutionContext) extends Controller {
	implicit val ec = executionContext

	def listCurrentExpenses = Action.async {
		val listOfCurrentWeeksCurrentExpenses = expenseManagementService.listCurrentWeeksCurrentExpenses()
		listOfCurrentWeeksCurrentExpenses.map { expenses =>
			Ok(expenses.mkString(","))
		}.recover {
			case NonFatal(e) => {
				BadRequest("Could not load this weeks expenses").flashing("error" -> e.getMessage())
			}
		}
	}

	def summarizeWeeksExpenses = Action.async {
		expenseManagementService.listCurrentWeeksCurrentExpensesWithGroup.map { expensesByGroup =>
			Ok(expensesByGroup.toString)
		}.recover {
			case NonFatal(e) => {
				BadRequest("Could not load this weeks expenses").flashing("error" -> e.getMessage())
			}
		}
	}

	def createExpense = Action {
		// TO-DO: Bind form, create expense
		BadRequest("todo")
	}

}