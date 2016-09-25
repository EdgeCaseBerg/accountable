package forms

import org.scalatest._

import play.api.data.{ Form, Forms }
import play.api.data.Forms.single
import play.api.data.validation.{ Valid, Invalid }

class PackageTest extends FlatSpec with Matchers with OptionValues {
	"Importing the forms package" should "alias UUID to java.util.UUID" in {
		assertResult(classOf[UUID])(classOf[java.util.UUID])
	}

	it should "provide a uuid constraint that fails on non-uuid strings" in {
		validUUID("bad value") match {
			case Valid => fail("Constraint passed when it should have failed")
			case Invalid(errors) => {
				assert(!errors.isEmpty)
				assert(errors.find(_.message == "forms.invalid.uuid").isDefined)
			}
		}
	}

	it should "provide a uuid constraint that succeeds on uuid strings" in {
		validUUID(java.util.UUID.randomUUID().toString) match {
			case Valid => succeed
			case _ => fail("Constraint failed when it should have passed")
		}
	}

	it should "provide a UUID Mapping that can validate a form" in {
		val testForm = Form(single("u" -> uuid))
		assert(!testForm.bind(Map("u" -> "xxx")).errors.isEmpty)
		assert(testForm.bind(Map("u" -> java.util.UUID.randomUUID().toString)).errors.isEmpty)
	}

	it should "provide a constraint to confirm valid html 5 date strings" in {
		validHtml5Date("2012-09-11") match {
			case Valid => succeed
			case _ => fail("Constraint failed when it should have passed")
		}
	}

	it should "provide a constraint to confirm invalid html 5 date strings" in {
		validHtml5Date("2015-2-22") match {
			case Valid => fail("constraint passed when it should have failed")
			case Invalid(errors) => {
				assert(!errors.isEmpty)
				assert(errors.find(_.message == "forms.invalid.html5Date").isDefined)
			}
		}
	}

	it should "provide a Long Mapping that can validate a form has an html5 date string" in {
		val testForm = Form(single("d" -> htmlDateInputToEpochSecond))
		assert(!testForm.bind(Map("d" -> "xxx")).errors.isEmpty)
		assert(testForm.bind(Map("d" -> "2009-03-01")).errors.isEmpty)
	}

	it should "provide a constraint to confirm currency strings without cents are invalid" in {
		validAmountString("10") match {
			case Valid => fail("constraint passed when it should have failed")
			case Invalid(errors) => {
				assert(!errors.isEmpty)
				assert(errors.find(_.message == "forms.invalid.amount").isDefined)
			}
		}
	}

	it should "provide a constraint to confirm currency strings without any dollars are invalid" in {
		validAmountString(".01") match {
			case Valid => fail("constraint passed when it should have failed")
			case Invalid(errors) => {
				assert(!errors.isEmpty)
				assert(errors.find(_.message == "forms.invalid.amount").isDefined)
			}
		}
	}

	it should "provide a constraint to confirm currency strings with a dollars & cents are valid" in {
		validAmountString("10.23") match {
			case Valid => succeed
			case Invalid(errors) => fail("Constraint failed when it should have passed")
		}
	}

}