package dao.mysql

import dao.exceptions._
import java.sql.{ SQLException, SQLIntegrityConstraintViolationException }
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
		case e: SQLIntegrityConstraintViolationException if e.getMessage().toUpperCase.startsWith("DUPLICATE ENTRY") => {
			DuplicateDataException(e.getMessage(), e)
		}
		case e: SQLIntegrityConstraintViolationException if e.getMessage().toUpperCase.contains("A FOREIGN KEY CONSTRAINT FAILS") => {
			determineExceptionForForeignConstraint(e)
		}
		// case throwable: SQLException if throwable.getErrorCode() == 23505 => 	Unique Key Violation
		case _ => {
			//throwable.printStackTrace()
			throwable
		}
	}

	/** Regular expression to find a foreign key field string in a SQLIntegrityConstraintViolationException
	 */
	val failedConstraintFieldRegEx = """.*FOREIGN KEY \(.([^`]+).\) REFERENCES.*""".r

	/** Convert an exception for a foreign key constraint failing into a domain specific error
	 */
	def determineExceptionForForeignConstraint(exception: SQLIntegrityConstraintViolationException): Throwable = {
		val msg = exception.getMessage().toUpperCase()
		if (msg.contains("CANNOT ADD OR UPDATE A CHILD ROW")) {
			msg.toString match {
				case failedConstraintFieldRegEx(fieldStr: String) if fieldStr == "EXPENSEID" => ExpenseDoesNotExistException("The expense does not exist!", exception)
				case failedConstraintFieldRegEx(fieldStr: String) if fieldStr == "GROUPID" => ExpenseGroupDoesNotExistException("The group does not exist!", exception)
				case x => {
					exception
				}
			}
		} else {
			exception
		}
	}
}
