package injection

import com.typesafe.config._
import com.google.inject.Provides
import dao.mysql.MySQLDatabaseParameters

trait MySQLDatabaseParametersProvider {
	@Provides
	def provideMySQLDatabaseParameters(): MySQLDatabaseParameters = {
		val testConf = ConfigFactory.load()
		val databaseUrl = testConf.getString("db.url")
		val databaseUser = testConf.getString("db.user")
		val databasePassword = testConf.getString("db.password")
		MySQLDatabaseParameters(
			databaseUrl,
			databaseUser,
			databasePassword
		)
	}
}
