package actors

import models.domain._
import akka.actor.ActorRef

import java.io.File

/** The job to import from BGI involves a ManagerWorkerProtocol and a few actors
 *
 *  The manager is responsible for announcing when there is work to do
 *
 *
 *             ~> (Worker)
 *   (Manager) ~> (Worker)
 *             ~> (Worker)
 *
 *
 *  The messages sent to the workers inform the workers to start to pull data
 *  from the manager who holds onto the list of data that is to be done.
 *
 *  The first step is done by the manager, which is to read the directory being
 *  imported and construct a list of accounts to be made.
 *
 *  (Manager) ~> ReadDirectory ~> (Manager)
 *
 *  Once this is done, if an accounts file is found then the manager reads it and
 *  then goes through the process of constructing which expense groups need to be
 *  made. Once this list is created, work is announced
 *
 *
 *  Each worker is responsible for the creation of an expense group if it needs
 *  to be done, and then the creation of the expenses belonging to that group.
 *
 *   (Worker) ~> RequestExpenseGroup ~> (Manager) ~> ImportExpenseGroup ~> (Worker)
 *
 *  Once the expense group is determined to exist, the expenses can then be imported
 *  with the work being pulled
 *
 */
package object imports {
	object ManagerWorkerProtocol {

		/** Message sent from outside the system to the manager to start an import */
		case class BeginImport(directory: File)

		/** Message sent from the manager to the outside to indicate that the import has started */
		case object ImportStarted

		/** Message sent from the manager to the outside to indicate an import has completed with a given status
		 *  @param status the status of the import, success or failure.
		 */
		case class ImportFinished(status: String)

		// TODO: Determine structure for status updates

		/** Sealed trait to be extended by messages intended to pass from manager to worker */
		sealed trait ManagerToWorkerMessage

		/** Message used by the manager to indicate to workers that accounts data is available for pulling */
		case object AnnounceWorkAvailable extends ManagerToWorkerMessage

		/** Message used by the manager to provide a worker with part of the string data from an accounts file */
		case class ImportExpenseGroup(expenseGroup: ExpenseGroup, dataFile: File) extends ManagerToWorkerMessage

		/** Sealed trait to be extended by messages that the manager uses internally to manage its affairs */
		sealed trait InternalManagerMessage

		/** Message used by the manager to begin reading the directory being imported */
		case object ReadDirectory extends InternalManagerMessage

		/** Message used by the manager to begin reading data from an accounts file */
		case class ReadAccountsFile(file: File) extends InternalManagerMessage

		/** Message used by the manager to indicate the account names found in the accounts index */
		case class AccountsFileData(accountsToBeMade: List[ExpenseGroup]) extends InternalManagerMessage

		/** Sealed trait to be extended by messages that the worker sends to the manager */
		sealed trait WorkerToManagerMessage

		/** Message used by the worker to pull account data from the manager */
		case class RequestDataToImport(workerRef: ActorRef) extends WorkerToManagerMessage

		/** Sealed trait to be extended by messages that the worker uses internally to manage its affairs */
		sealed trait InternalWorkerMessage

		case class CreatedExpenseGroup(expenseGroup: ExpenseGroup) extends InternalWorkerMessage

		case class CreatedExpense(expense: Expense) extends InternalWorkerMessage

		// TODO: flush out the rest of the messages

	}
}