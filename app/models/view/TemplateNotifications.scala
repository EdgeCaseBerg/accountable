package models.view

/** Holds notifications which should be rendered to templates
 *  @note While Play does have the flash scope, you can't use that without redirecting. So this class exists as a general notification
 *  @param msgKey The key to a message stored in play's i18n Message object
 *  @param notificationType The type of notification this is, affects how the notification is rendered
 *  @param msgArgs Arguments that will be passed to the Messages object that are needed for the given msgKey
 */
case class TemplateNotification(val msgKey: String = "views.error", val notificationType: TemplateNotificationsType = ErrorNotification)(val msgArgs: Any*)