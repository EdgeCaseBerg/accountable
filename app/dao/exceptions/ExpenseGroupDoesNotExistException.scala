package dao.exceptions

case class ExpenseGroupDoesNotExistException(val msg: String, val cause: Throwable) extends DAOException(msg, cause)