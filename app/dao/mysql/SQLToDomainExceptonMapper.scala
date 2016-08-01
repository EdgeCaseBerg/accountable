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

	/**
	 */
	val failedConstraintFieldRegEx = """.*FOREIGN KEY \(.([^`]+).\) REFERENCES.*""".r

	/** for some reason this isn't working even though in the console it does:
	 *  scala> val s = """CANNOT ADD OR UPDATE A CHILD ROW: A FOREIGN KEY CONSTRAINT FAILS (`ACCOUNTABLE_TEST`.`EXPENSEGROUPTOEXPENSE`, CONSTRAINT `EXPENSEGROUPTOEXPENSE_IBFK_1` FOREIGN KEY (`GROUPID`) REFERENCES `EXPENSEGROUPS` (`GROUPID`))"""
	 *  s: String = CANNOT ADD OR UPDATE A CHILD ROW: A FOREIGN KEY CONSTRAINT FAILS (`ACCOUNTABLE_TEST`.`EXPENSEGROUPTOEXPENSE`, CONSTRAINT `EXPENSEGROUPTOEXPENSE_IBFK_1` FOREIGN KEY (`GROUPID`) REFERENCES `EXPENSEGROUPS` (`GROUPID`))
	 *
	 *  scala> val failedConstraintFieldRegEx = """.*FOREIGN KEY \(.([^`]+).\) REFERENCES.*""".r
	 *  failedConstraintFieldRegEx: scala.util.matching.Regex = .*FOREIGN KEY \(.([^`]+).\) REFERENCES.*
	 *
	 *  scala> s match {
	 *  | case failedConstraintFieldRegEx(x) => println(x)
	 *  | }
	 *  GROUPID
	 *
	 */
	def determineExceptionForForeignConstraint(exception: SQLIntegrityConstraintViolationException): Throwable = {
		println("determineExceptionForForeignConstraint")
		val msg = exception.getMessage().toUpperCase()
		println(failedConstraintFieldRegEx.findFirstMatchIn(msg))
		if (msg.contains("CANNOT ADD OR UPDATE A CHILD ROW")) {
			println("going to match it...")
			msg.toString match {
				case failedConstraintFieldRegEx(fieldStr: String) if fieldStr == "EXPENSEID" => ExpenseDoesNotExistException("The expense does not exist!", exception)
				case failedConstraintFieldRegEx(fieldStr: String) if fieldStr == "GROUPID" => ExpenseGroupDoesNotExistException("The group does not exist!", exception)
				case x => {
					println("nope")
					exception
				}
			}
		} else {
			println("nopee")
			exception
		}
	}
}
