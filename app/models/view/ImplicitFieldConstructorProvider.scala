package models.view

import play.api.data.Field
import play.api.data.validation.Constraints

/** Overrides the default play field constructor when imported into a template */
object ImplicitFieldConstructorProvider {
	import views.html.helper.FieldConstructor
	implicit val myFields = FieldConstructor(views.html.common.fieldConstructor.f)

	/** Convert commonly found constraints into arguments
	 *  For example, play's min/max settings will be converted into HTML constraints on an input text
	 *  @param field	 The field whose constraints will be examined
	 *  @param args Additional args to add to the map <i>after</i> the field's constraints are converted
	 *  @note Commonly found constraints are those <a href="https://playframework.com/documentation/2.3.x/api/scala/index.html#play.api.data.validation.Constraints$">provided by play</a>
	 *  @return a Map containing arguments that can be passed to input helpers
	 */
	def constraintsToArgs(field: Field, args: (Symbol, Any)*): Seq[(Symbol, Any)] = {
		/** @note We have to assign these to val's because you'll get exceptions like 'stable identifier required, but play.api.data.validation.Constraints.nonEmpty.name.get found.'
		 */
		val nonEmptyName = Constraints.nonEmpty.name.get
		val minName = Constraints.min(0).name.get
		val minStrictName = Constraints.min(0, true).name.get
		val maxName = Constraints.max(0).name.get
		val maxStrictName = Constraints.max(0).name.get
		val maxLengthName = Constraints.maxLength(0).name.get
		val minLengthName = Constraints.minLength(0).name.get
		val pattern = Constraints.pattern(".*".r).name.get

		val helperForMinMaxes = { w: Seq[Any] =>
			w.headOption.getOrElse(0)
		}

		field.constraints.map {
			case (constraintName, wrappedArray) =>
				constraintName match {
					case `nonEmptyName` => Option(('minLength -> 1))
					case `minName` | `minStrictName` => {
						Option(('min -> helperForMinMaxes(wrappedArray)))
					}
					case `maxName` | `maxStrictName` => {
						Option(('max -> helperForMinMaxes(wrappedArray)))
					}
					case `minLengthName` => {
						Option(('minLength -> helperForMinMaxes(wrappedArray)))
					}
					case `maxLengthName` => {
						Option(('maxLength -> helperForMinMaxes(wrappedArray)))
					}
					case `pattern` => {
						Option(('pattern -> wrappedArray.headOption.getOrElse(".*")))
					}
					case _ => Option.empty[(Symbol, Any)]
				}
		}.filter(_.isDefined).map(_.get).++(args)
	}
}