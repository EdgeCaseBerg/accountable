package models.view

/** Sealed trait for types of notifications that can be displayed to the user */
sealed trait TemplateNotificationsType

/** Indicates that the notification type is an error */
case object ErrorNotification extends TemplateNotificationsType

/** Indicates that the notification type is informational */
case object InfoNotification extends TemplateNotificationsType