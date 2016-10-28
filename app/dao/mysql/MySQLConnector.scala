package dao.mysql

import java.sql.{ Connection, SQLException }
import javax.sql.DataSource;
import javax.inject.{ Inject, Singleton }
import scalikejdbc._

import com.zaxxer.hikari.{ HikariDataSource, HikariConfig }

/** Connection pool management for MySQL. Provides access to DB connections
 *
 *  Constructor Annotated with @Inject for dependency injection
 *
 *  @note annotated as Singleton to avoid using too many resources when this class is pulled from a DI framework.
 *
 *  @param mySQLDatabaseParameters Database parameters for the underlying connection
 *  @param sQLToDomainExceptonMapper Mapping class to convert database errors into application errors if possible
 */
@Singleton
class MySQLConnector @Inject() (
		@transient val mySQLDatabaseParameters: MySQLDatabaseParameters,
		val sQLToDomainExceptonMapper: SQLToDomainExceptonMapper
) {

	/* Ensure that the Driver is loaded properly. */
	Class.forName("com.mysql.jdbc.Driver")

	def getHikariConfig() = {
		val hikariConfig = new HikariConfig();

		hikariConfig.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource")
		hikariConfig.addDataSourceProperty("url", mySQLDatabaseParameters.url)
		hikariConfig.addDataSourceProperty("user", mySQLDatabaseParameters.user)
		hikariConfig.addDataSourceProperty("password", mySQLDatabaseParameters.password)

		/** HikariCP recommended MySQL configuration
		 *
		 *  @see https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
		 */
		hikariConfig.addDataSourceProperty("cachePrepStmts", "true")
		hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250")
		hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
		hikariConfig.addDataSourceProperty("useServerPrepStmts", "false")

		/** We set this to false in order to have the application start even if the DB is down for maintenance
		 */
		hikariConfig.setInitializationFailFast(false)

		hikariConfig.setConnectionTestQuery("SELECT 1")
		hikariConfig
	}

	val dataSource: DataSource = {
		new HikariDataSource(getHikariConfig())
	}

	/** Close the underlying datasource and clean up connections
	 */
	def shutdown = dataSource.asInstanceOf[HikariDataSource].close()

	ConnectionPool.singleton(new DataSourceConnectionPool(dataSource))

	/** Execute a block of code, in the scope of a JDBC connection.
	 *  The connection and all created statements are automatically released.
	 *  The connection is automatically committed, unless an exception occurs.
	 *
	 *  @param block Code block to execute with an implicit connection in scope
	 */
	def withConnection[A](block: Connection => A): A = {
		implicit val connection: Connection = ConnectionPool.borrow()
		try {
			block(connection)
		} catch {
			case e: Throwable => throw sQLToDomainExceptonMapper.map(e)
		} finally {
			connection.close()
		}
	}

	/** Execute a block of code, in the scope of a readOnly JDBC connection.
	 *  The connection is automatically released.
	 *
	 *  @note Will cause java.sql.SQLException if you attempt to insert/update while using this method
	 *  @note This method should be preffered over withConnection when performing reads for security and additional protection against injection attacks
	 *
	 *  @param block Code block to execute with an implicit connection in scope
	 */
	def withReadOnlyConnection[A](block: Connection => A): A = {
		implicit val connection: Connection = ConnectionPool.borrow()
		try {
			connection.setAutoCommit(false)
			connection.setReadOnly(true)
			block(connection)
		} catch {
			case e: Throwable => throw sQLToDomainExceptonMapper.map(e)
		} finally {
			connection.setReadOnly(false)
			connection.close()
		}
	}

	/** Execute a block of code, in the scope of a JDBC transaction.
	 *  The connection and all created statements are automatically released.
	 *  The transaction is automatically committed, unless an exception occurs.
	 *
	 *  @param block Code block to execute with an implicit connection in scope
	 */
	def withTransaction[A](block: Connection => A): A = {
		withConnection { implicit connection =>
			try {
				connection.setAutoCommit(false)
				connection.setReadOnly(false)
				val r = block(connection)
				connection.commit()
				r
			} catch {
				case e: Throwable => connection.rollback(); throw sQLToDomainExceptonMapper.map(e)
			} finally {
				connection.close()
			}
		}
	}
}