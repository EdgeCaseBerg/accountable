package support

import java.time.Instant
import play.api.mvc.{ PathBindable, QueryStringBindable }
import utils.TimeUtils._
import scala.language.implicitConversions

object RouteSupport {
	implicit def instantPathBinding(implicit longBinder: PathBindable[Long]): PathBindable[Instant] = new PathBindable[Instant] {
		override def bind(key: String, value: String): Either[String, Instant] = {
			for {
				longNumber <- longBinder.bind(key, value).right
			} yield longNumber.toInstant
		}
		override def unbind(key: String, value: Instant): String = {
			longBinder.unbind(key, value.getEpochSecond())
		}
	}
	implicit def instantQueryBinding(implicit longBinder: QueryStringBindable[Long]): QueryStringBindable[Instant] = new QueryStringBindable[Instant] {
		override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Instant]] = {
			Option {
				longBinder.bind(key, params) match {
					case Some(Right(longNumber)) => Right(longNumber.toInstant)
					case _ => Left("Unable to bind Instant")
				}
			}
		}
		override def unbind(key: String, value: Instant): String = {
			longBinder.unbind(key, value.getEpochSecond())
		}
	}
}