/* No package because by default play will grab a Global from the default package */

import modules._
import models.view._
import play.api.mvc.{ RequestHeader, Results }
import dao.mysql.{ MySQLDatabaseParameters, MySQLConnector }
import com.google.inject.Guice
import org.flywaydb.core.Flyway
import play.api.{ GlobalSettings, Application, Logger }
import scala.util.control.NonFatal
import scala.concurrent.Future

object Global extends GlobalSettings {

	lazy val injector = Guice.createInjector(new AccountableModule)

	override def getControllerInstance[A](clazz: Class[A]) = injector.getInstance(clazz)

	def getInstance[A](clazz: Class[A]) = injector.getInstance(clazz)

	override def onStart(app: Application) {
		Logger.info("Starting application")
		try {
			val dbParams = getInstance(classOf[MySQLDatabaseParameters])
			val flyway = new Flyway();
			flyway.setDataSource(dbParams.url, dbParams.user, dbParams.password, "SELECT 1;")
			val totalNumberOfMigrations = flyway.info().all().size
			val numMigrationsToBeApplied = flyway.info().pending().size
			Logger.info(s"$totalNumberOfMigrations total migrations")
			Logger.info(s"$numMigrationsToBeApplied migration(s) to be applied")
			flyway.migrate()
			Logger.info("Application boot finishing")
		} catch {
			case NonFatal(e) =>
				Logger.error("Could not migrate database, starting application with unmigrated database...")
				Logger.error(e.getMessage())
		}
	}

	/** Handle exceptions */
	override def onError(request: RequestHeader, ex: Throwable) = {
		Future.successful {
			implicit val notifications = List(TemplateNotification()(ex.getMessage()))
			Results.InternalServerError(views.html.errorPage("An internal error has occured"))
		}
	}

	/** Handle 404's */
	override def onHandlerNotFound(request: RequestHeader) = {
		implicit val notifications = List.empty[TemplateNotification]
		Future.successful(Results.NotFound(views.html.errorPage(s"Sorry, the page you requested does not exist!")))
	}

	/** Shutdown resources used by the application
	 *  @param app The application being stopped
	 *  @note If I upgrade to 2.4 I can handle this better within the MySQLConnector itself (https://www.playframework.com/documentation/2.4.x/ScalaDependencyInjection#Stopping/cleaning-up)
	 */
	override def onStop(app: Application) {
		Logger.info("Application shutting down...");
		Logger.info("closing database connections")
		val connector = getInstance(classOf[MySQLConnector])
		connector.shutdown
	}

}