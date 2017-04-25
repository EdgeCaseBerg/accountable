package actors.imports

import akka.actor._
import models.domain._
import utils.ImportUtils
import java.io.{ File, FilenameFilter }
import scala.io.Source
import ManagerWorkerProtocol._

class AccountsFileFilter extends FilenameFilter {
	def accept(dir: File, name: String): Boolean = {
		name == "accounts"
	}
}

class ValidLineItemFileFilter(accountNames: List[String]) extends FilenameFilter {
	/** Expected names for BGI account files have spaces replaced with -
	 *  @see {@link https://github.com/EdgeCaseBerg/BGI/blob/master/src/util/ctl.c#L12 Method used to generate account file names from the file}
	 */
	private val expectedAccountFileNames = accountNames.map(ImportUtils.nameToBGIFileName).toSet

	def accept(dir: File, name: String): Boolean = {
		expectedAccountFileNames.contains(name)
	}
}

class ManagerActor(workersSelection: ActorSelection) extends Actor {
	var accountsToImport: Set[(ExpenseGroup, File)] = Set.empty
	var importInProgress: Map[ExpenseGroup, (ActorRef, String)] = Map.empty
	var groupsFailedToBeMade: Map[ExpenseGroup, String] = Map.empty
	var groupsSuccessfullyMade: Map[ExpenseGroup, String] = Map.empty
	var currentOverallStatus = "Idle" 
	var jobStatus: JobStatus = Idle

	def hasNoMoreWork = {
		importInProgress.isEmpty && accountsToImport.isEmpty
	}

	def currentStatus = {
		CurrentStatus(
			todo = accountsToImport.map(_._1.name).toList,
			inProgress = importInProgress.map {
				case (expenseGroup, (ref, status)) => expenseGroup.name -> status
			},
			failed = groupsFailedToBeMade.map {
				case (expenseGroup, status) => expenseGroup.name -> status
			},
			success = groupsSuccessfullyMade.map {
				case (expenseGroup, status) => expenseGroup.name -> status
			},
			overallStatus = currentOverallStatus,
			jobStatus = jobStatus
		)
	}

	/** When we are idle, we're ready to start a job or return a status */
	def idle: Receive = {
		case BeginImport(directory) =>
			sender() ! ImportStarted
			if (directory.isDirectory()) {
				currentOverallStatus = "Reading directory..."
				self ! ReadDirectory
				jobStatus = InProgress
				context.become(readingDirectory(directory))
			} else {
				currentOverallStatus = "Failed to start job, File given as import directory is not a directory!"
			}
		case RequestStatus => sender() ! currentStatus
	}

	/** When we are reading a directory, we're preparing the data in a directory for import */
	def readingDirectory(directory: File): Receive = {
		case ReadDirectory =>
			val accountsFileInList = directory.listFiles(new AccountsFileFilter)
			if (accountsFileInList.isEmpty) {
				currentOverallStatus = "Cannot import, directory does not contain an accounts index"
				jobStatus = Error
				context.unbecome() // switch back to idle
			} else {
				val accountsFile = accountsFileInList.head
				currentOverallStatus = "Reading accounts file..."
				self ! ReadAccountsFile(accountsFile)
			}
		case ReadAccountsFile(accountsFile) =>
			val accountsFileSource = Source.fromFile(accountsFile)
			accountsFileSource.withClose { () =>
				val linesInFile = accountsFileSource.getLines()
				val eithers = linesInFile.map(ImportUtils.expenseGroupFromBGIString(_)).toList
				val accountsFileValid = !eithers.map(_.isRight).exists(_ == false)
				if (accountsFileValid) {
					currentOverallStatus = "Preparing to begin account creation"
					self ! AccountsFileData(eithers.map(_.right.get))
				} else {
					currentOverallStatus = "Invalid accounts file, aborting job!"
					jobStatus = Error
					context.unbecome() // switch back to idle
				}
				()
			}
		case AccountsFileData(expenseGroupsToMake) =>
			val accountFiles = directory.listFiles(new ValidLineItemFileFilter(expenseGroupsToMake.map(_.name)))
			/* Now we store each file with the corresponding expense group to be made */
			expenseGroupsToMake
				.groupBy(eg => ImportUtils.nameToBGIFileName(eg.name))
				.mapValues(_.head) // Turn this from a Map[String, List[ExpenseGroup]] to Map[String, ExpenseGroup]
				.map { // Then, turn it into Map[ExpenseGroup, Option[File]]
					case (fileName, expenseGroup) =>
						expenseGroup -> accountFiles.find(_.getName() == fileName)
				}
				.foreach { // Lastly, put each group and file into the appropriate bin
					case (expenseGroup, Some(file)) => accountsToImport += expenseGroup -> file
					case (expenseGroup, None) => groupsFailedToBeMade += expenseGroup -> "Data file not found in directory"
				}
			if (accountsToImport.isEmpty) {
				currentOverallStatus = "No accounts to import!"
				jobStatus = Idle
				context.unbecome()
			} else {
				currentOverallStatus = "Accounts data read, informing workers..."
				workersSelection ! AnnounceWorkAvailable
				context.become(readyForWorkers)
			}
		case RequestStatus => sender() ! currentStatus
	}

	def readyForWorkers: Receive = {
		case RequestDataToImport(workerRef) =>
			currentOverallStatus = "Handing work to workers"
			/* Take something from the todo list and give it to the worker */
			if (accountsToImport.nonEmpty) {
				val (expenseGroup, file) = accountsToImport.head
				accountsToImport = accountsToImport.tail
				/* Begin watching the worker in case it dies during the import and we need to move an inProgress back to todo */
				context.watch(workerRef)
				workerRef ! ImportExpenseGroup(expenseGroup, file)
				importInProgress += (expenseGroup -> (workerRef, "Starting import..."))

			} else {
				jobStatus = Error
				context.unbecome() // transition back to idle
			}
		case Terminated(subject) =>
			/* In the case of a termination of a worker, transition that work to failed. */
			context.unwatch(subject) // Probably don't need to do this, but it probably doesn't hurt either
			importInProgress.find {
				case (expenseGroup, (workerRef, file)) => workerRef == subject
			}.foreach {
				case (expenseGroup, _) =>
					groupsFailedToBeMade += (expenseGroup -> "Worker importing group unexpectedly terminated")
					importInProgress -= expenseGroup
			}
		case RequestStatus => sender() ! currentStatus
		case InformManagerOfUpdate(expenseGroup, newStatus) =>
			importInProgress.get(expenseGroup).map {
				case (workerRef, _) =>
					importInProgress += expenseGroup -> (workerRef, newStatus)
			}
		case InformManagerOfFailure(expenseGroup, reason) =>
			importInProgress.get(expenseGroup).map {
				case (workerRef, _) =>
					context.unwatch(workerRef)
					importInProgress -= expenseGroup
			}
			groupsFailedToBeMade += expenseGroup -> reason
			if (hasNoMoreWork) {
				jobStatus = Idle
				context.unbecome()
			}
		case InformManagerOfSuccess(expenseGroup, newStatus) =>
			importInProgress.get(expenseGroup).map {
				case (workerRef, _) =>
					context.unwatch(workerRef)
					importInProgress -= expenseGroup
			}
			groupsSuccessfullyMade += expenseGroup -> newStatus
			if (hasNoMoreWork) {
				jobStatus = Idle
				context.unbecome()
			}

	}

	def receive = idle
}