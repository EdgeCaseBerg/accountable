package forms

import models.domain.Expense

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._

/** Container object for forms relating to the expenses
 */
object ExpenseForms {

	/** Private method to convert a mapping form into an Expense + optioanl groupId tuple
	 *  @note This is defined here instead of inline with the form for readability
	 */
	private val unapplyExpenseForm = {
		(amountInCents: Long, name: String, dateOccured: Long, maybeGroupId: Option[UUID]) => (Expense(amountInCents, name, dateOccured), maybeGroupId)
	}

	/** Private method to convert a tuple into a filled Form
	 *  @note This is defined here instead of inline with the form for readability
	 */
	private val applyExpenseForm = { tuple: (Expense, Option[UUID]) =>
		val (expense, maybeGroupId) = tuple
		Option((expense.amountInCents, expense.name, expense.dateOccured, maybeGroupId))
	}

	/** Form for creating an expense and possibly adding it to a group by groupId */
	val createExpenseForm = Form[(Expense, Option[UUID])](
		mapping(
			"amountInCents" -> longNumber,
			"name" -> text,
			"dateOccured" -> longNumber,
			"groupId" -> optional(uuid)
		)(unapplyExpenseForm)(applyExpenseForm)
	)

}