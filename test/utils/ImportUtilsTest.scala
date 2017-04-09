package utils

import org.scalatest._
import models.domain._

class ImportUtilsTest extends FlatSpec with Matchers {
	val validBGILineItem = "1407714019 movie 7.75 44.486519 -73.212314"
	val validBGIAccount = "1 food 16805.560000"

	"ImportUtils#expenseFromLineItemBGIString" should "parse a valid BGI line item into an expense" in {
		ImportUtils.expenseFromLineItemBGIString(validBGILineItem) match {
			case Right(expense) =>
				assertResult(775)(expense.amountInCents)
				assertResult("movie")(expense.name)
				assertResult(1407714019)(expense.dateOccured)
			case Left(ex) => fail(s"should have parsed, instead got ${ex.getMessage()}")
		}

	}

	it should "parse an invalid string into an exception" in {
		ImportUtils.expenseFromLineItemBGIString("crap") match {
			case Right(_) => fail("should not have parsed")
			case Left(ex) =>
		}

	}

	"ImportUtils#expenseGroupFromBGIString" should "parse a valid BGI account into an expense group" in {
		ImportUtils.expenseGroupFromBGIString(validBGIAccount) match {
			case Right(expenseGroup) => assertResult("food")(expenseGroup.name)
			case Left(ex) => fail(s"should have parsed, instead got ${ex.getMessage()}")
		}

	}

	it should "parse an invalid string into an exception" in {
		ImportUtils.expenseGroupFromBGIString("crap") match {
			case Right(_) => fail("should not have parsed!")
			case Left(ex) =>
		}

	}
}