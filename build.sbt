// *****************************************************************************
// Build settings
// *****************************************************************************

inThisBuild(
  Seq(
    organization     := "example.com",
    organizationName := "ksilin",
    startYear        := Some(2022),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalaVersion := "2.13.8",
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-encoding",
      "UTF-8",
      "-Ywarn-unused:imports",
    ),
    scalafmtOnCompile := true,
    dynverSeparator   := "_", // the default `+` is not compatible with docker tags
    resolvers ++= Seq(
      "confluent" at "https://packages.confluent.io/maven",
      "ksqlDb" at "https://ksqldb-maven.s3.amazonaws.com/maven",
      Resolver.sonatypeRepo("releases"),
      Resolver.bintrayRepo("wolfendale", "maven"),
      Resolver.mavenLocal,
      Resolver.mavenCentral,
      "jitpack" at "https://jitpack.io",
      "jFrog" at "https://confluent.jfrog.io/artifactory/maven-public"
    ),
  )
)

// *****************************************************************************
// Projects
// *****************************************************************************

lazy val cpapi =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.clients,
        library.airframeLog,
        library.logback,
        library.jacksonDatabind,
        library.jacksonScala,
        library.testcontainers   % Test,
        library.scalatest        % Test,
        library.restAssured      % Test,
        library.restAssuredScala % Test,
        library.rbacApiServer    % Test,
      ),
      libraryDependencies ~= { _.map(_.exclude("org.slf4j", "slf4j-log4j12")) }
    )

// *****************************************************************************
// Project settings
// *****************************************************************************

lazy val commonSettings =
  Seq(
    // Also (automatically) format build definition together with sources
    Compile / scalafmt := {
      val _ = (Compile / scalafmtSbt).value
      (Compile / scalafmt).value
    },
  )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val kafka          = "3.0.0"
      val airframeLog    = "21.12.1"
      val logback        = "1.2.10"
      val jackson        = "2.1.2"
      val jacksonScala        = "2.13.2"
      val scalatest      = "3.2.10"
      val testContainers = "0.2.1"
      val restAssured    = "5.1.1"
    }
    val clients         = "org.apache.kafka"           % "kafka-clients"    % Version.kafka
    val airframeLog     = "org.wvlet.airframe"        %% "airframe-log"     % Version.airframeLog
    val logback         = "ch.qos.logback"             % "logback-core"     % Version.logback
    val jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % Version.jackson
    val jacksonScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % Version.jacksonScala

    val testcontainers =
      "com.github.christophschubert" % "cp-testcontainers" % Version.testContainers
    val restAssured      = "io.rest-assured" % "rest-assured"    % Version.restAssured
    val restAssuredScala = "io.rest-assured" % "scala-support"   % Version.restAssured
    val rbacApiServer    = "io.confluent"    % "rbac-api-server" % "7.1.2-174"
    val scalatest        = "org.scalatest"  %% "scalatest"       % Version.scalatest
  }
