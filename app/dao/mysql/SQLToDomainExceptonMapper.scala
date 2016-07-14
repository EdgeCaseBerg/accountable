package dao.mysql

import dao.exceptions._
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
		case _: MysqlDataTruncation => {
			DataTooLargeException(s"Looks like one of the values you submited was too big for it's britches!", throwable)
		}
		// case throwable: SQLException if throwable.getErrorCode() == 23505 => 	Unique Key Violation
		case _ => throwable
	}
}
