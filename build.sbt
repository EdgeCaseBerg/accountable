import play.PlayImport.PlayKeys.playRunHooks
import scalariform.formatter.preferences._
import com.typesafe.config._

organization := "com.github.edgecaseberg"

name := "accountable-core"

version := "0.0.0"

scalaVersion := "2.11.7"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
	"org.mockito" % "mockito-all" % "1.10.+",
	"org.scalatestplus" %% "play" % "1.2.0" % "test",
	"com.google.inject" % "guice" % "3.0",
	"com.typesafe" % "config" % "1.2.1",
    "com.typesafe.play" %% "anorm" % "2.3.+",
    "com.zaxxer" % "HikariCP" % "2.4.5",
    "org.scalikejdbc" %% "scalikejdbc" % "2.2.+",
    "mysql" % "mysql-connector-java" % "5.1.+",
    "org.flywaydb" % "flyway-core" % "4.0"
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
