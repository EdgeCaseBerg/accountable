package dao.mysql

import java.sql.SQLException
import com.mysql.jdbc.MysqlDataTruncation

/** Maps SQL Exceptions from the database layer to a domain specific exception
 */
class SQLToDomainExceptonMapper() {

	/** Maps an exception to domain exception if it can, otherwise returns the given throwable
	 *
	 *  @param throwable The Throwable to convert into a different exception
	 *  @return Throwable that has been mapped to a domain exception if possible, or the given throwable otherwise
	 */
	def map(throwable: Throwable): Throwable = throwable match {
		// case _: MysqlDataTruncation => truncated data because a field was out of range
		// case throwable: SQLException if throwable.getErrorCode() == 23505 => 	Unique Key Violation
		case _ => throwable
	}
}
