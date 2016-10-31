package controller

import play.api.mvc._
import models.view._
import scala.language.implicitConversions
import scala.util.control.NonFatal

/** Controller trait that provides an easy way to convert exceptions into TemplateNotifications
 *
 *  @note If a controller will be passing exception messages to an errorPage that takes an implicit List[TemplateNotification] this controller can remove some boilerplate
 */
trait NotifyingController extends Controller {

	/** Conversion of Throwable's to an error for "views.error" to display
	 *  @tparam T A class type upper bounded by Throwable
	 *  @param throwable An exception to convert into a TemplateNotification
	 *  @return A list of a single TemplateNotification specifying the views.error key with the exception message as msgArgs
	 */
	def throwable2TemplateNotifications[T <: Throwable](throwable: T): List[TemplateNotification] = {
		List(TemplateNotification("views.error", ErrorNotification)(throwable.getMessage()))
	}

	/** Conversion of a Flash context into template notifications
	 *  @note Only info and error keys of the flash scope are checked for conversions
	 *  @param flash The Flash of a request
	 *  @return A list of TemplateNotification's created from the info and error keys of the given flash
	 */
	def flash2TemplateNotification(implicit flash: Flash): List[TemplateNotification] = {
		List(
			flash.get("info").map(key => TemplateNotification(key, InfoNotification)()),
			flash.get("error").map(key => TemplateNotification(key, ErrorNotification)())
		).view.filter(_.isDefined).map(_.get).force.toList
	}

	/** Helper method to quickly display the errorPage and a message
	 *  @param errorMessage The general error message to be displayed
	 *  @return A BadRequest rendering the errorPage template with exceptions converted to template notifications
	 */
	def withErrorPage(errorMessage: String = "A unknown problem occured"): PartialFunction[Throwable, Result] = {
		case NonFatal(e) => {
			implicit val notifications: List[TemplateNotification] = throwable2TemplateNotifications(e)
			BadRequest(views.html.errorPage(errorMessage))
		}
	}

}