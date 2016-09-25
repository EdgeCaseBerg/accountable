import play.api.data._
import play.api.data.Forms._
import play.api.data.validation._

import java.util.UUID
import java.time.LocalDate

import models.domain._
import utils.TimeUtils

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

	/** Constraint defining a valid HTML5 date inputs value
	 *  @note To set the error message use the Messages file and set forms.invalid.html5Date
	 */
	val validHtml5Date = Constraint[String]("forms.invalid.html5Date") { str =>
		Try(LocalDate.parse(str, TimeUtils.html5DateFormat)) match {
			case Success(localDate) => Valid
			case Failure(e) => Invalid(ValidationError("forms.invalid.html5Date", str))
		}
	}

	/** Mapping to be used when constructing forms that use html5 date inputs */
	def htmlDateInputToEpochSecond: Mapping[Long] = {
		text.verifying(validHtml5Date).transform(
			TimeUtils.html5StringToEpochSecond(_),
			TimeUtils.epochSecondsToHtml5DateString _
		)
	}

}