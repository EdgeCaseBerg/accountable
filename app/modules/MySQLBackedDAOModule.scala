package modules

import dao._
import dao.mysql._

import com.google.inject.{ AbstractModule, Provides }
import com.typesafe.config.{ ConfigException, ConfigFactory }

import play.api.Play

import scala.concurrent.ExecutionContext

abstract class MySQLBackedDAOModule extends AbstractModule {
	override def configure() {
		bind(classOf[ExpensesDAO]).to(classOf[MySQLExpensesDAO])
		bind(classOf[ExpenseGroupsDAO]).to(classOf[MySQLExpenseGroupsDAO])
	}

	@Provides
	@throws(classOf[ConfigException])
	def provideMySqlDatabaseParameters() = {
		val config = Play.maybeApplication.fold(ConfigFactory.load()) { application =>
			application.configuration.underlying
		}
		val dbUrl = config.getString("db.url")
		val user = config.getString("db.user")
		val password = config.getString("db.password")
		MySQLDatabaseParameters(dbUrl, user, password)
	}

	@Provides
	def provideExecutionContext() = {
		play.api.libs.concurrent.Execution.defaultContext
	}
}

