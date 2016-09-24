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
}