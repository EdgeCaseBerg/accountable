package testhelpers

import com.typesafe.config._
import org.scalatest._
import org.flywaydb.core.Flyway
import java.io.File

trait MigratedAndCleanDatabase extends FlatSpec with Matchers with BeforeAndAfterAll {

	lazy val testConf = ConfigFactory.load()
	lazy val databaseUrl = testConf.getString("db.url")
	lazy val databaseUser = testConf.getString("db.user")
	lazy val databasePassword = testConf.getString("db.password")

	/** Override this if you need to load a file for a test for fixtures */
	val flywayLocations = Seq[String]("filesystem:conf/db/migration")

	lazy private val flyway = new Flyway();

	/** If you need to do your own beforeAll work after mixing this in be sure to call
	 *  {{super.beforeAll}}
	 *  from your override.
	 */
	override def beforeAll {
		super.beforeAll
		flyway.setDataSource(databaseUrl, databaseUser, databasePassword, "SELECT 1;")
		flyway.setLocations(flywayLocations: _*)
		flyway.clean()
		val totalNumberOfMigrations = flyway.info().all().size
		assertResult(totalNumberOfMigrations, "The Database was not migrated properly! Please verify flyway files") {
			flyway.migrate()
		}
		info("Test Database Ready for use")
	}

	/** If you need to do your own afterAll work after mixing this in be sure to call
	 *  {{super.afterAll}}
	 *  from your override.
	 */
	override def afterAll {
		super.afterAll
		flyway.clean()
		info("Test Database Reset for next use")
	}

}