package forms

import models.domain.ExpenseGroup

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._

/** Container object for forms relating to the expense groups
 */
object ExpenseGroupForms {

	/** Form for creating an expense group.
	 *  @note only maps name because groupId is auto-generated
	 */
	val createExpenseGroupForm = Form[ExpenseGroup](
		mapping(
			"name" -> nonEmptyText(maxLength = 64)
		)(name => ExpenseGroup(name = name))(expenseGroup => Option(expenseGroup.name))
	)
}