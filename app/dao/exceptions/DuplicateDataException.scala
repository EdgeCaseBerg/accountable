package dao.exceptions

case class DuplicateDataException(val msg: String, val cause: Throwable) extends RuntimeException(msg, cause)