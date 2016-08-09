package dao.exceptions

/** An exception indicating an action that required an existing expense could not be performed because the expense did not exist */
case class ExpenseDoesNotExistException(override val msg: String, override val cause: Throwable) extends DAOException(msg, cause)