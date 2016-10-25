/* No package because by default play will grab a Global from the default package */

import modules._
import models.view._
import play.api.mvc.{ RequestHeader, Results }
import dao.mysql.MySQLDatabaseParameters
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

}