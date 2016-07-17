package dao.exceptions

case class ExpenseDoesNotExistException(val msg: String, val cause: Throwable) extends RuntimeException(msg, cause)