package dao.mysql

/** Parameters object to configure a connection to a database
 *
 *  @param url The database url
 *  @param user The database user who will connect to the database
 *  @param password The password for the database user
 */
case class MySQLDatabaseParameters(
	val url: String,
	val user: String,
	@transient val password: String
)