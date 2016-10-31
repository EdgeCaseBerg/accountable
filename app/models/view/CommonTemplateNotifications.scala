package models.view

/** Template notifications that are a common occurence and are used during recoveries of failures */
object CommonTemplateNotifications {
	/** Notification indicating that expense groups could not be loaded, but the attempt should be tried again
	 *  @note Used in places where a template might be rendered successfully without expense groups present
	 */
	val TMP_GROUP_LOAD_FAIL = TemplateNotification("views.tmp.grouploadfail", InfoNotification)()
}