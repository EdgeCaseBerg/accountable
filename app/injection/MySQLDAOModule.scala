package injection

import com.google.inject.AbstractModule
import dao._
import mysql._

class MySQLDAOModule extends AbstractModule with MySQLDatabaseParametersProvider {
	override protected def configure() {
		bind(classOf[ExpenseGroupsDAO]).to(classOf[MySQLExpenseGroupsDAO])
		bind(classOf[ExpensesDAO]).to(classOf[MySQLExpensesDAO])
	}
}