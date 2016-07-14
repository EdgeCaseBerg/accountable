package dao.exceptions

case class DataTooLargeException(val msg: String, val cause: Throwable) extends RuntimeException(msg, cause)