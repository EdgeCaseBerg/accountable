package dao.exceptions

/** Base Trait for application exceptions which occur from the DAO layer
 *
 *  @param msg A message describing this exception
 *  @param cause The exception which is the underlying cause to this DAOException, null if this is the underlying cause
 */
abstract class DAOException(val msg: String, val cause: Throwable) extends RuntimeException(msg, cause)