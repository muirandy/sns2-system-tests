name := "sns-system-tests"

version := "0.1"

scalaVersion := "2.12.7"

resolvers += "Maven Repository" at "https://mvnrepository.com/artifact/"
resolvers += "Confluent Maven Repository" at "http://packages.confluent.io/maven/"
resolvers += "Bintray Maven Repository" at "https://dl.bintray.com/palantir/releases"

libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.8" % Test
libraryDependencies += "com.eclipsesource.minimal-json" % "minimal-json" % "0.9.5" % Test
libraryDependencies += "org.apache.activemq" % "activemq-all" % "5.10.1" % Test
libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test"
libraryDependencies += "org.xmlunit" % "xmlunit-core" % "2.6.3" % Test
libraryDependencies += "org.assertj" % "assertj-core" % "3.13.2" % Test
libraryDependencies += "org.xmlunit" % "xmlunit-assertj" % "2.6.3" % Test

//libraryDependencies += "org.apache.kafka" %% "kafka" % "2.1.0"
libraryDependencies += "org.apache.kafka" %% "kafka" % "2.2.0"
// https://mvnrepository.com/artifact/org.apache.kafka/kafka-streams-scala
//libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % "2.1.0"
// https://mvnrepository.com/artifact/org.apache.kafka/kafka-streams
libraryDependencies += "org.apache.kafka" % "kafka-streams" % "2.2.0"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "2.2.0"
libraryDependencies += "io.confluent" % "monitoring-interceptors" % "5.3.0"

libraryDependencies += "com.goyeau" %% "kafka-streams-circe" % "0.5"
libraryDependencies += "com.structurizr" % "structurizr-client" % "1.3.0"

libraryDependencies += "com.dimafeng" %% "testcontainers-scala" % "0.24.0" % "test"
libraryDependencies += "org.testcontainers" % "kafka" % "1.12.1" % Test
libraryDependencies += "org.testcontainers" % "junit-jupiter" % "1.12.1" % Test

//testCompile 'com.palantir.docker.compose:docker-compose-rule-junit4:0.31.1'
libraryDependencies += "com.palantir.docker.compose" % "docker-compose-rule-junit4" % "0.31.1" % Test

libraryDependencies += "net.javacrumbs.json-unit" % "json-unit" % "2.6.1" % Test


