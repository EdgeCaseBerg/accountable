package controller

import play.api.mvc._
import play.filters.csrf._
import javax.inject.Inject
import scala.util.control.NonFatal
import scala.concurrent.{ ExecutionContext, Future }
import service._
import models.domain._
import forms.ExpenseForms

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

	def showCreateExpenseForm = CSRFAddToken {
		Action.async { implicit request =>
			expenseManagementService.listExpenseGroups.recover {
				case NonFatal(e) => List.empty[ExpenseGroup]
			}.map { expenseGroups =>
				Ok("Add expense to any of the groups. " + expenseGroups.mkString(","))
			}
		}
	}

	def createExpense = CSRFCheck {
		Action.async { implicit request =>
			ExpenseForms.createExpenseForm.bindFromRequest.fold(
				formWithErrors => {
					Future.successful(BadRequest(formWithErrors.errors.mkString(",")))
				},
				boundForm => {
					val (newExpense, maybeGroupId) = boundForm
					maybeGroupId.fold {
						expenseManagementService.createExpense(newExpense).recover {
							case NonFatal(e) => BadRequest(e.getMessage())
						}.map { createdExpense =>
							Ok("created expense! " + createdExpense)
							//To-do: Redirect() to create form if we're making another, or redirect back to the summary for the week of this expense
						}
					} { groupId =>
						expenseManagementService.listExpenseGroups.recover {
							case NonFatal(e) => List.empty[ExpenseGroup]
						}.flatMap { groups =>
							groups.find(_.groupId == groupId).fold(Future.successful(BadRequest(s"Group ${groupId} does not exist"))) { groupToAddTo =>
								expenseManagementService.createExpenseAndAddToGroup(newExpense, groupToAddTo).recover {
									case NonFatal(e) => BadRequest(e.getMessage())
								}.map { _ =>
									Ok("Added expense to group")
								}
							}
						}
					}
				}
			)
		}
	}

}