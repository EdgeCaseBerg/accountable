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
import forms.{ ExpenseForms, ExpenseGroupForms }

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
			Ok(views.html.expenseSummary(expensesByGroup))
		}.recover(withErrorPage("Could not load this weeks expenses"))
	}

	def showCreateExpenseForm = CSRFAddToken {
		Action.async { implicit request =>
			val notificationsFromFlash = flash2TemplateNotification
			val futureExpenseGroups = expenseManagementService.listExpenseGroups
			val defaultForm = ExpenseForms.createExpenseForm.bind(Map("dateOccured" -> TimeUtils.html5Now)).discardingErrors
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
							groups.find(_.groupId == groupId).fold {
								implicit val noGroupNotifications = CommonTemplateNotifications.GROUP_ID_NOT_FOUND(groupId)
								val savedFormValues = ExpenseForms.createExpenseForm.fill(boundForm)
								Future.successful(BadRequest(views.html.createForm(groups, savedFormValues)))
							} { groupToAddTo =>
								expenseManagementService.createExpenseAndAddToGroup(newExpense, groupToAddTo).map { _ =>
									Redirect(routes.ExpenseController.listCurrentExpenses()).flashing("info" -> "views.success.expense.create")
								}.recover(withErrorPage("Could not create the expense and add it to the group"))
							}
						}
					}
				}
			)
		}
	}

	def listExpenseGroups = Action.async { implicit request =>
		expenseManagementService.listExpenseGroups().map { expenseGroups =>
			implicit val notifications = flash2TemplateNotification
			Ok(views.html.manageGroups(expenseGroups))
		}.recover(withErrorPage("Could not load the expense groups"))
	}

	def showCreateExpenseGroupForm = CSRFAddToken {
		Action { implicit request =>
			val notificationsFromFlash = flash2TemplateNotification
			val defaultForm = ExpenseGroupForms.createExpenseGroupForm
			Ok(views.html.createExpenseGroupForm(defaultForm))
		}
	}

	def createExpenseGroup = CSRFCheck {
		Action.async { implicit request =>
			ExpenseGroupForms.createExpenseGroupForm.bindFromRequest.fold(
				formWithErrors => Future.successful(BadRequest(views.html.createExpenseGroupForm(formWithErrors))),
				boundForm => {
					expenseManagementService.createNewExpenseGroupWithName(boundForm.name).map { createdExpenseGroup =>
						Redirect(routes.ExpenseController.listExpenseGroups()).flashing("info" -> "views.success.expensegroup.create")
					}.recover {
						case NonFatal(e) => {
							implicit val notifications = throwable2TemplateNotifications(e)
							BadRequest(views.html.createExpenseGroupForm(ExpenseGroupForms.createExpenseGroupForm.fill(boundForm)))
						}
					}
				}
			)
		}
	}

}