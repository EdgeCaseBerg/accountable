import play.PlayImport.PlayKeys.playRunHooks
import scalariform.formatter.preferences._
import com.typesafe.config._

organization := "com.github.edgecaseberg"

name := "accountable-core"

version := "0.0.0"

scalaVersion := "2.11.8"

autoAPIMappings := true // Allow scaladoc to grab documentation as neccesary

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
	"org.mockito" % "mockito-all" % "1.10.+",
	"org.scalatest" %% "scalatest" % "3.0.0",
	"com.google.inject" % "guice" % "4.1.0",
	"com.google.inject.extensions" % "guice-throwingproviders" % "4.1.0",
	"com.typesafe" % "config" % "1.2.1",
	"com.typesafe.play" %% "anorm" % "2.3.+",
	"com.zaxxer" % "HikariCP" % "2.5.1",
	"org.scalikejdbc" %% "scalikejdbc" % "2.4.2",
	"mysql" % "mysql-connector-java" % "5.1.+",
	"org.flywaydb" % "flyway-core" % "4.0",
	"com.github.pathikrit" %% "better-files" % "2.16.0",
	filters
)

scalariformPreferences := scalariformPreferences.value
	.setPreference(DoubleIndentClassDeclaration, true)
	.setPreference(PreserveDanglingCloseParenthesis, true)
	.setPreference(AlignParameters, false)
	.setPreference(IndentWithTabs, true)
	.setPreference(MultilineScaladocCommentsStartOnFirstLine, true)

val confFileName = System.getProperty("config.file", "conf/application.conf")

val conf = ConfigFactory.parseFile(new File(confFileName)).resolve()

flywayUrl :=  conf.getString("db.url")

flywayUser := conf.getString("db.user")

flywayPassword := conf.getString("db.password")

flywayDriver := "com.mysql.jdbc.Driver"

flywayLocations := Seq("filesystem:conf/db/migration")

javaOptions in Test ++= Seq("-Dconfig.file=conf/test.conf", "-Duser.timezone=UTC")

javaOptions in Runtime += "-Duser.timezone=UTC"

scalacOptions ++= Seq("-feature")

TwirlKeys.templateImports ++= Seq(
	"models.domain._",
	"models.view._",
	"play.api.i18n.{Messages, Lang}"
)

includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"
