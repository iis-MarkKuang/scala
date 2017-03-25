logLevel := Level.Warn

resolvers ++= Seq(
  Classpaths.sbtPluginSnapshots,
  Classpaths.sbtPluginReleases,
  Resolver.sonatypeRepo("snapshots"),
  "Twitter Maven" at "https://maven.twttr.com"
)

addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "0.8.1")
addSbtPlugin("com.twitter" % "scrooge-sbt-plugin" % "4.14.0")
addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.3")
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.2.2")
addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.7.9")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.8.0")

credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  "thunderbolt397",
  "1qaz2wsx3edc"
)