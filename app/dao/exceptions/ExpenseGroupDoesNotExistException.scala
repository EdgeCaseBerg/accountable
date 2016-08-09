package dao.exceptions

/** Exception indicating that an action could be performed because a required ExpenseGroup did not exist */
case class ExpenseGroupDoesNotExistException(override val msg: String, override val cause: Throwable) extends DAOException(msg, cause)