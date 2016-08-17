package testhelpers

import dao.mysql._
import injection._
import service._
import com.google.inject.Guice

trait ServiceTestWithDB extends MigratedAndCleanDatabase {
	lazy val injector = Guice.createInjector(new MySQLDAOModule())

	def withExpenseManagementService[T](code: ExpenseManagementService => T) {
		val expenseManagementService = injector.getInstance(classOf[ExpenseManagementService])
		code(expenseManagementService)
	}

}