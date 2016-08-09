package dao.exceptions

/** Exception indicating that an error occured because of an attempt to duplicate data in the system */
case class DuplicateDataException(override val msg: String, override val cause: Throwable) extends DAOException(msg, cause)