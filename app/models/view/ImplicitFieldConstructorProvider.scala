package models.view

/** Overrides the default play field constructor when imported into a template */
object ImplicitFieldConstructorProvider {
	import views.html.helper.FieldConstructor
	implicit val myFields = FieldConstructor(views.html.common.fieldConstructor.f)
}