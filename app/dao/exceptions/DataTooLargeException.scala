package dao.exceptions

/** Exception indicating that data present in the system was outside the expected bounds for the given context */
case class DataTooLargeException(override val msg: String, override val cause: Throwable) extends DAOException(msg, cause)