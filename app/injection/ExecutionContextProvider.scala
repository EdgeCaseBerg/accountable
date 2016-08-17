package injection

import com.google.inject.Provides
import scala.concurrent.ExecutionContext

trait ExecutionContextProvider {
	@Provides
	def provideExecutionContext(): ExecutionContext = {
		play.api.libs.concurrent.Execution.Implicits.defaultContext
	}
}
