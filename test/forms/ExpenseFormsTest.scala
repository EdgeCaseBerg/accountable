package forms

import org.scalatest._

import java.time.Instant

import models.domain._
import ExpenseForms._

class ExpenseFormsTest extends FlatSpec with Matchers with OptionValues {
	val validDefaults = Map(
		"amount" -> "10.00",
		"name" -> "A name",
		"dateOccured" -> "2016-06-28",
		"groupId" -> java.util.UUID.randomUUID().toString
	)

	"The create expense form" should "map fields to a tuple of Expense and None if no groupId is given" in {
		createExpenseForm.bind(validDefaults - "dateOccured").fold(
			formWithErrors => fail(formWithErrors.errors.mkString),
			tuple => {
				val (expense, maybeGroupId) = tuple
				assertResult(Option.empty[UUID])(maybeGroupId.value)
				assertResult(classOf[Expense])(expense.getClass())
			}
		)
	}

	it should "map fields to a tuple of Expense and UUID if a groupId is given" in {
		createExpenseForm.bind(validDefaults).fold(
			formWithErrors => fail(formWithErrors.errors.mkString),
			tuple => {
				val (expense, maybeGroupId) = tuple
				assertResult(validDefaults("groupId"))(maybeGroupId.value)
			}
		)
	}

	Seq("amount", "name", "dateOccured").foreach { emptyField =>
		it should s"reject forms with empty values for ${emptyField}" in {
			assert(createExpenseForm.bind(validDefaults - emptyField).error(emptyField).isDefined)
		}
	}

	it should "parse an amount currency string to an amount in cents" in {
		createExpenseForm.bind(validDefaults).fold(
			formWithErrors => fail(formWithErrors.errors.mkString),
			tuple => {
				val (expense, maybeGroupId) = tuple
				assertResult(100 * 10)(expense.amountInCents)
			}
		)
	}

	it should "parse a non-empty name to the expense" in {
		createExpenseForm.bind(validDefaults).fold(
			formWithErrors => fail(formWithErrors.errors.mkString),
			tuple => {
				val (expense, maybeGroupId) = tuple
				assertResult(validDefaults("name"))(expense.name)
			}
		)
	}

	it should "reject names longer than 512 characters" in {
		createExpenseForm.bind(validDefaults + ("name" -> "A" * 513)).fold(
			formWithErrors => assert(formWithErrors.errors.find(_.key == "name").isDefined),
			tuple => fail("should not have parsed form")
		)
	}

	it should "parse a date string to an epoch second" in {
		createExpenseForm.bind(validDefaults).fold(
			formWithErrors => fail(formWithErrors.errors.mkString),
			tuple => {
				val (expense, maybeGroupId) = tuple
				assertResult(1474675200)(expense.dateOccured)
			}
		)
	}
}