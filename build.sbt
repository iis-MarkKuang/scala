import sbt.Keys._
import sbt._

name := "shrfid"

version := "1.0"

val suffix = ""

val libVersion = "6.38.0" + suffix
val utilVersion = "6.37.0" + suffix
//val finagleHttpxVersion = "6.27.0"
val scroogeVersion = "4.10.0" + suffix
val libThriftVersion = "0.5.0-1"
val finatraVersion = "2.4.0"
val guiceVersion = "4.0"
val logbackVersion = "1.0.13"
val twitterServerVersion = "1.23.0"
val mockitoCoreVersion = "1.9.5"
val scalaTestVersion = "2.2.3"
val specs2Version = "2.3.12"
val typesafeConfigVersion = "1.3.0"
val finagleOAuth2Version = "0.1.5"
val jwtScalaVersion = "1.2.2"


val finagleCore = "com.twitter" %% "finagle-core" % libVersion
val finagleHttp = "com.twitter" %% "finagle-http" % libVersion
val finagleRedis = "com.twitter" %% "finagle-redis" % libVersion
val finagleTest = "com.twitter" %% "finagle-test" % libVersion
val finagleMySQL = "com.twitter" %% "finagle-mysql" % libVersion
val finagleStats = "com.twitter" %% "finagle-stats" % libVersion
//val finagleHttpx = "com.twitter" %% "finagle-httpx" % finagleHttpxVersion
val scroogeCore = "com.twitter" %% "scrooge-core" % scroogeVersion
val libThrift = "org.apache.thrift" % "libthrift" % libThriftVersion
val finagleThrift = "com.twitter" %% "finagle-thrift" % utilVersion
val utilCore = "com.twitter" %% "util-core" % utilVersion
val utilCollection = "com.twitter" %% "util-collection" % utilVersion
val twitterServer = "com.twitter" %% "twitter-server" % twitterServerVersion
val finagleOAuth2 = "com.github.finagle" %% "finagle-oauth2" % finagleOAuth2Version

val finatraHttp = "com.twitter" %% "finatra-http" % finatraVersion
val finatraHttpClient = "com.twitter" %% "finatra-httpclient" % finatraVersion
val finatraSlf4j = "com.twitter" %% "finatra-slf4j" % finatraVersion
val finatraInjectThriftClient = "com.twitter" %% "inject-thrift-client" % finatraVersion
val finatraInjectServer = "com.twitter" %% "inject-server" % finatraVersion
val finatraInjectRequestScope = "com.twitter" %% "inject-request-scope" % finatraVersion
val finatraInjectThriftClientHttpMapper = "com.twitter" %% "inject-thrift-client-http-mapper" % finatraVersion
val finatraThrift = "com.twitter" %% "finatra-thrift" % finatraVersion
val injectCore = "com.twitter" %% "inject-core" % finatraVersion
val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion

val typesafeConfig = "com.typesafe" % "config" % typesafeConfigVersion

val jwtScala = "io.really" %% "jwt-scala" % jwtScalaVersion

val finatraHttpTest = "com.twitter" %% "finatra-http" % finatraVersion % "test"
val finatraJacksonTest = "com.twitter" %% "finatra-jackson" % finatraVersion % "test"
val injectServerTest = "com.twitter" %% "inject-server" % finatraVersion % "test"
val injectAppTest = "com.twitter" %% "inject-app" % finatraVersion % "test"
val injectCoreTest = "com.twitter" %% "inject-core" % finatraVersion % "test"
val injectModulesTest = "com.twitter" %% "inject-modules" % finatraVersion % "test"
val guiceTestlib = "com.google.inject.extensions" % "guice-testlib" % guiceVersion % "test"

val fintraHttpTestClassifier = "com.twitter" %% "finatra-http" % finatraVersion % "test" classifier "tests"
val finatraJacksonTestClassifier = "com.twitter" %% "finatra-jackson" % finatraVersion % "test" classifier "tests"
val injectAppTestClassifier = "com.twitter" %% "inject-app" % finatraVersion % "test" classifier "tests"
val injectCoreTestClassifier = "com.twitter" %% "inject-core" % finatraVersion % "test" classifier "tests"
val injectModulesTestClassifier = "com.twitter" %% "inject-modules" % finatraVersion % "test" classifier "tests"
val injectServerTestClassifier = "com.twitter" %% "inject-server" % finatraVersion % "test" classifier "tests"

val mockitoCoreTest = "org.mockito" % "mockito-core" % mockitoCoreVersion % "test"
val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
val spec2Test = "org.specs2" %% "specs2" % specs2Version % "test"



lazy val `shrfid` = (project in file(".")).aggregate(
  `shrfid-api`,
  shrfidCommon)

lazy val shrfidCommon = (project in file("shrfid-common")).settings(commonSettings: _*)
  .settings(
    scroogeThriftDependencies in Compile := Seq(
      "finatra-thrift_2.11"
    ),
    libraryDependencies ++= Seq(
      finatraHttp,
      finatraHttpClient,
      finatraSlf4j,
      finatraThrift,
      finatraInjectThriftClient,
      finatraInjectServer,
      finatraInjectRequestScope,
      finatraInjectThriftClientHttpMapper, injectCore, logbackClassic, utilCollection,
      utilCore,
      finatraHttpTest, finatraJacksonTest, injectServerTest, injectAppTest, injectCoreTest, injectModulesTest, guiceTestlib,
      fintraHttpTestClassifier, finatraJacksonTestClassifier, injectAppTestClassifier, injectCoreTestClassifier,
      injectModulesTestClassifier, injectServerTestClassifier,
      mockitoCoreTest, scalaTest, spec2Test,
      typesafeConfig,
      jwtScala,
      finagleOAuth2,
      finagleRedis,
      finagleMySQL,
      "org.apache.httpcomponents" % "httpclient" % "4.5.2",
      "com.sksamuel.elastic4s" % "elastic4s-core_2.11" % "5.0.0",
      "com.sksamuel.elastic4s" % "elastic4s-json4s_2.11" % "5.0.0",
      "com.sksamuel.elastic4s" % "elastic4s-jackson_2.11" % "5.0.0",
      "com.sksamuel.elastic4s" % "elastic4s-play-json_2.11" % "5.0.1",
      "com.sksamuel.elastic4s" % "elastic4s-circe_2.11" % "5.0.0",
      "com.sksamuel.elastic4s" % "elastic4s-embedded_2.11" % "5.0.0",
      "com.sksamuel.elastic4s" % "elastic4s-streams_2.11" % "5.0.0",
      "com.sksamuel.elastic4s" % "elastic4s-testkit_2.11" % "5.0.0",
      "com.sksamuel.elastic4s" % "elastic4s-core-tests_2.11" % "5.0.0",

      // ftp
      "commons-net" % "commons-net" % "3.6",

      // slick
      "com.typesafe.slick" %% "slick" % "3.1.1",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.1.1" excludeAll ExclusionRule(organization = "com.zaxxer"),
      "com.zaxxer" % "HikariCP" % "2.4.5",
      "mysql" % "mysql-connector-java" % "5.1.37",
      "joda-time" % "joda-time" % "2.9.3",
      "org.joda" % "joda-convert" % "1.8",
      "com.github.tototoshi" %% "slick-joda-mapper" % "2.2.0",
      // twitter async
      "com.github.foursquare" % "twitter-util-async" % "516e77a",

      // scala async
      "org.scala-lang.modules" %% "scala-async" % "0.9.5",


      "com.iheart" %% "ficus" % "1.2.3", // for scala friendly typesafe config

      // reflect
      "org.scala-lang" % "scala-reflect" % "2.11.7",

      // kafka
      "com.github.okapies" %% "finagle-kafka" % "0.2.1",

      // json naming strategy(snake case)
      "com.github.tototoshi" %% "play-json-naming" % "1.1.0",

      //nats.io
      "io.nats" % "jnats" % "0.4.1",

      // marc4j
      "org.tigris" % "marc4j" % "2.4",

      "com.chuusai" %% "shapeless" % "2.3.2"
    )
  )



lazy val `shrfid-api` = (project in file("shrfid-api")).dependsOn(shrfidCommon).settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
    )
  )



lazy val commonSettings = Seq(
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.jcenterRepo,
    Resolver.sonatypeRepo("snapshots"),
    "Finatra Repo" at "http://twitter.github.com/finatra",
    "jitpack" at "https://jitpack.io",
    "Twitter Maven" at "https://maven.twttr.com",
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases/",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases/",
    "Sonatype OSS Releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2",
    "Clojars repository" at "http://clojars.org/repo/"
  ),
  scalaVersion := "2.11.7"
)
val runAll = inputKey[Unit]("Runs all subprojects")

runAll := {
  (run in Compile in `shrfid-api`).evaluated
}

fork in run := true

// enables unlimited amount of resources to be used :-o just for runAll convenience
concurrentRestrictions in Global := Seq(
  Tags.customLimit(_ => true)
)

packAutoSettings

