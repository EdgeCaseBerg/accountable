package controller

import play.api.mvc._
import play.filters.csrf._
import javax.inject.Inject
import scala.util.control.NonFatal
import scala.concurrent.{ ExecutionContext, Future }
import service._
import models.domain._
import models.view._
import utils.TimeUtils
import forms.ExpenseForms

class ExpenseController @Inject() (expenseManagementService: ExpenseManagementService, executionContext: ExecutionContext) extends NotifyingController {
	implicit val ec = executionContext

	def listCurrentExpenses = Action.async { implicit request =>
		val listOfCurrentWeeksCurrentExpenses = expenseManagementService.listCurrentWeeksCurrentExpenses()
		listOfCurrentWeeksCurrentExpenses.map { expenses =>
			implicit val notifications = flash2TemplateNotification
			Ok(views.html.currentExpenses(expenses))
		}.recover(withErrorPage("Could not load this weeks expenses"))
	}

	def summarizeWeeksExpenses = Action.async { implicit request =>
		expenseManagementService.listCurrentWeeksCurrentExpensesWithGroup.map { expensesByGroup =>
			implicit val notifications = flash2TemplateNotification
			Ok(expensesByGroup.toString)
		}.recover(withErrorPage("Could not load this weeks expenses"))
	}

	def showCreateExpenseForm = CSRFAddToken {
		Action.async { implicit request =>
			val notificationsFromFlash = flash2TemplateNotification
			val futureExpenseGroups = expenseManagementService.listExpenseGroups
			val defaultForm = ExpenseForms.createExpenseForm.bind(Map("dateOccured" -> TimeUtils.html5Now))
			futureExpenseGroups.map { expenseGroups =>
				implicit val notifications = flash2TemplateNotification
				Ok(views.html.createForm(expenseGroups, defaultForm))
			}.recover {
				case NonFatal(e) => {
					implicit val notifications = List(CommonTemplateNotifications.TMP_GROUP_LOAD_FAIL) ++ notificationsFromFlash
					Ok(views.html.createForm(Nil, defaultForm))
				}
			}
		}
	}

	def createExpense = CSRFCheck {
		Action.async { implicit request =>
			ExpenseForms.createExpenseForm.bindFromRequest.fold(
				formWithErrors => {
					expenseManagementService.listExpenseGroups.map { expenseGroups =>
						BadRequest(views.html.createForm(expenseGroups, formWithErrors))
					}.recover {
						case NonFatal(e) =>
							implicit val notifications = List(CommonTemplateNotifications.TMP_GROUP_LOAD_FAIL)
							BadRequest(views.html.createForm(Nil, formWithErrors))
					}
				},
				boundForm => {
					val (newExpense, maybeGroupId) = boundForm
					maybeGroupId.fold {
						expenseManagementService.createExpense(newExpense).map { createdExpense =>
							Redirect(routes.ExpenseController.listCurrentExpenses()).flashing("info" -> "views.success.expense.create")
						}.recover(withErrorPage("Could not create the expense"))
					} { groupId =>
						expenseManagementService.listExpenseGroups.recover {
							case NonFatal(e) => List.empty[ExpenseGroup]
						}.flatMap { groups =>
							groups.find(_.groupId == groupId).fold(Future.successful(BadRequest(s"Group ${groupId} does not exist"))) { groupToAddTo =>
								expenseManagementService.createExpenseAndAddToGroup(newExpense, groupToAddTo).map { _ =>
									Ok("Added expense to group")
								}.recover {
									withErrorPage("Could not create the expense and add it to the group")
								}
							}
						}
					}
				}
			)
		}
	}

}