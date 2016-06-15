package models.domain

import java.util.UUID

/** An expense tag is just a way to indicate that an expense is 'tagged' with
 *  a grouping label.
 */
case class ExpenseTag(val name: String, val tagId: UUID = UUID.randomUUID())