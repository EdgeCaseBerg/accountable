import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._

import java.util.UUID

import models.domain._

import scala.util.{ Try, Success, Failure }

package object forms {

	/** Type Alias to avoid imports within this package */
	type UUID = java.util.UUID

	/** Constrant to define what a valid UUID is when parsing forms
	 *  @note To set the error message, use the Messages file and set forms.invalid.uuid
	 */
	val validUUID = Constraint[String]("forms.invalid.uuid") { str =>
		Try(UUID.fromString(str)) match {
			case Success(uuid) => Valid
			case Failure(e) => Invalid(ValidationError("forms.invalid.uuid", str))
		}
	}

	/** Mapping to be used when constructing forms that take UUID's */
	def uuid: Mapping[UUID] = {
		text.verifying(validUUID).transform(UUID.fromString _, _.toString)
	}

}