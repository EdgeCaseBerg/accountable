package dao.exceptions

/** Base Trait for application exceptions which occur from the DAO layer
 *
 *
 */
abstract class DAOException(msg: String, cause: Throwable) extends RuntimeException(msg, cause)