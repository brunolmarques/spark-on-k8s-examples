name := "spark-k8s-examples"

version := "1.1"

scalaVersion := "2.11.8"

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

unmanagedBase <<= baseDirectory { base => base / "libs" }

// Dependencies required for this project
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.4.1" % "provided",
  "org.apache.spark" %% "spark-sql" % "2.4.1" % "provided",
  "org.apache.spark" %% "spark-streaming" % "2.4.1" % "provided",
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.1",
  "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.1",
  // Diana HEP dependencies
  "org.diana-hep" % "spark-root_2.11" % "0.1.16",
  "org.diana-hep" % "histogrammar-sparksql_2.11" % "1.0.3",
  // JSON serialization
  "org.json4s" %% "json4s-native" % "3.2.10"
)

// Remove stub classes
assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

// Exclude the Scala runtime jars
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

resolvers ++= Seq(
  "Spray Repository" at "http://repo.spray.cc/",
  "Cloudera Repository" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Second Typesafe repo" at "http://repo.typesafe.com/typesafe/maven-releases/",
  "Mesosphere Public Repository" at "http://downloads.mesosphere.io/maven",
  Resolver.sonatypeRepo("public")
)

resolvers += Resolver.url("bintray-sbt-plugins", url("http://dl.bintray.com/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
