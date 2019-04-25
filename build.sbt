name := "sns-system-tests"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test"
//libraryDependencies += "org.apache.kafka" %% "kafka" % "2.1.0"
libraryDependencies += "org.apache.kafka" %% "kafka" % "2.1.0"
// https://mvnrepository.com/artifact/org.apache.kafka/kafka-streams-scala
//libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % "2.1.0"
// https://mvnrepository.com/artifact/org.apache.kafka/kafka-streams
libraryDependencies += "org.apache.kafka" % "kafka-streams" % "2.1.0"
libraryDependencies += "com.goyeau" %% "kafka-streams-circe" % "0.5"
libraryDependencies += "com.structurizr" % "structurizr-client" % "1.3.0"

libraryDependencies += "com.dimafeng" %% "testcontainers-scala" % "0.24.0" % "test"
libraryDependencies += "org.testcontainers" % "kafka" % "1.11.1" % Test
libraryDependencies += "org.testcontainers" % "junit-jupiter" % "1.11.1" % Test
libraryDependencies += "net.javacrumbs.json-unit" % "json-unit" % "2.6.1" % Test
