package models.domain

import java.util.UUID

/** A label for grouping expenses together
 *
 *  @note The relationship between an expense and a group will be Many to One
 *  @param name A human friendly name to read the group as
 *  @param groupId A machine friendly name to identify this group uniquely
 */
case class ExpenseGroup(val name: String, val groupId: UUID = UUID.randomUUID())