package dao.exceptions

case class DataTooLargeException(val msg: String, val cause: Throwable) extends DAOException(msg, cause)