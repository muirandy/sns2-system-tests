#End to End System tests: https://github.com/muirandy/sns2-system-tests
1. Install Kafka in the usual way
2. Replace the config directory of Kafka with https://github.com/muirandy/sns2-kafka-config

##How to start Kafka by hand:
```
cd kafka_2.11-2.1.0
bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties
bin/kafka-server-start.sh config/server-1.properties
bin/kafka-server-start.sh config/server-2.properties
```
##Create the topics if necessary:
```
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 1 --topic incoming.op.msgs
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 1 --topic modify.op.msgs
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 1 --topic enriched.modification.instructions
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 1 --topic switch.modification.instructions
```

##View the messages on the topics:
```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic incoming.op.msgs --from-beginning
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic switch.modification.instructions --from-beginning
```


Msg-Validator microservice
https://github.com/muirandy/sns2-op-msg-validator.git


##Links:
Create a new scala-maven project: https://www.ivankrizsan.se/2016/03/27/creating-a-scala-project-with-maven-dependency-management-for-gatling-testing-in-intellij-idea/

Build a Docker scala kafka microservice: https://saumitra.me/blog/deploying-kafka-dependent-scala-microservices-with-docker-and-sbt/


Friday 30/11/2018
Kafka Consumer:
https://github.com/smallnest/kafka-example-in-scala/blob/master/src/main/scala/com/colobu/kafka/ScalaConsumerExample.scala

Sample project reading / writing to kafka topics: https://github.com/schmiegelow/iwomm-kafka



ToDo:
- Lots!
- Dockerise Kafka
-