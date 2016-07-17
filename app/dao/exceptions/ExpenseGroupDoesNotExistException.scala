package dao.exceptions

case class ExpenseGroupDoesNotExistException(val msg: String, val cause: Throwable) extends RuntimeException(msg, cause)