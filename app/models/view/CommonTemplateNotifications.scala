package models.view

/** Template notifications that are a common occurence and are used during recoveries of failures */
object CommonTemplateNotifications {
	/** Notification indicating that expense groups could not be loaded, but the attempt should be tried again
	 *  @note Used in places where a template might be rendered successfully without expense groups present
	 */
	val TMP_GROUP_LOAD_FAIL = TemplateNotification("views.tmp.grouploadfail", InfoNotification)()

	/** Notification indicating that an expense group was not found, takes an argument of groupId
	 *  @param groupId the id that will be displayed as not found in the notification
	 */
	def GROUP_ID_NOT_FOUND(groupId: Any) = TemplateNotification("views.error.groupNotFound", ErrorNotification)(groupId)
}