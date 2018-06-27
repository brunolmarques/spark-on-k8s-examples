name := "spark-service-examples"

organization := "ch.cern"

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

version := "0.2.0"

isSnapshot := true

scalaVersion := "2.11.8"

spIgnoreProvided := true

sparkVersion := "2.3.0"

sparkComponents := Seq("sql", "hive", "mllib")

resolvers += Resolver.mavenLocal

unmanagedBase <<= baseDirectory { base => base / "libs" }

//libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"

libraryDependencies += "org.diana-hep" % "spark-root_2.11" % "0.1.16"

libraryDependencies += "org.diana-hep" % "histogrammar-sparksql_2.11" % "1.0.3"

libraryDependencies += "org.json4s" % "json4s-native_2.11" % "3.2.11"

libraryDependencies += "org.scalanlp" %% "breeze" % "0.12"

libraryDependencies += "org.scalanlp" %% "breeze-natives" % "0.12"

libraryDependencies += "org.scalanlp" %% "breeze-viz" % "0.12"

libraryDependencies += "org.sameersingh.scalaplot" % "scalaplot" % "0.0.4"

//libraryDependencies += "org.diana-hep" % "histogrammar-bokeh_2.11" % "1.0.3"
