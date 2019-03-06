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
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 1 --topic service.events
```

##List the topics
bin/kafka-topics.sh --list --zookeeper localhost:2181 

##View the messages on the topics:
```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic incoming.op.msgs --from-beginning
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic modify.op.msgs --from-beginning
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic services --from-beginning
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic enriched.modification.instructions --from-beginning
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic switch.modification.instructions --from-beginning
```

##Write directly to a topic:
```
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic incoming.op.msgs
```

##Reset topic(s) for a specific app:
```
bin/kafka-streams-application-reset.sh --application-id sns-modify-enricher --bootstrap-servers localhost:9092,localhost:9093,localhost:9094 --input-topics services,modify.op.msgs
```

#KSQL - General Info
##Install KSQL
```
cd <kafka_installation_dir>
cd ..
git clone https://github.com/confluentinc/ksql.git
cd ksql
mvn clean compile install -DskipTests
```
 
##Start KSQL server
```
cd config
../bin/ksql-server-start ksql-server.properties
```

##Start KSQL client
```
cd bin
ksql http://localhost:8088
```

##Show and update the KSQL properties:
```
ksql> SHOW PROPERTIES;
ksql> SET 'property name' = 'value';
SET 'auto.offset.reset' = 'earliest';
```

##Show the topics on kafka:
```
ksql> SHOW TOPICS;
```

##Show the content of a topic:
```
ksql>  PRINT 'services' FROM BEGINNING;
```

##Creating STREAMs:
```
ksql> CREATE STREAM pageviews (viewtime BIGINT, user_id VARCHAR, page_id VARCHAR) WITH (KAFKA_TOPIC='pageviews-topic', VALUE_FORMAT='JSON');
```

##Creating TABLEs:
```
ksql> CREATE TABLE users (user_id VARCHAR, gender VARCHAR, region_id VARCHAR) WITH (KAFKA_TOPIC='pageviews-topic', KEY='user_id', VALUE_FORMAT='JSON');
```
(KEY must match the key in the kafka message, and also (!??!) a field in the JSON)
```
ksql> CREATE TABLE customers2 AS SELECT EXTRACTJSONFIELD(payload, '$.page') AS page, EXTRACTJSONFIELD(payload,'$.customer_id') AS CUSTOMER_ID FROM CUSTOMERS;
```
(this was indicated it may be having CUSTOMER as another TABLE)

##View information about a STREAM or TABLE:
```
ksql> DESCRIBE <TABLE/STREAM>
ksql> DESCRIBE EXTENDED <TABLE/STREAM>
ksql> SHOW <TABLES/STREAMS>
```

#KSQL - SNS Info
##From the KSQL prompt, ensure we see data already present in topics:
```
SET 'auto.offset.reset' = 'earliest';
```

##Create stream to read the generic Json
```
CREATE STREAM INSTRUCTIONS_STREAM_1 (transaction STRUCT <\
  operatorId VARCHAR,\
  instruction STRUCT <\
    order STRUCT <\
      operatorOrderId VARCHAR,\
      orderId VARCHAR\
    >,\
    modifyFeaturesInstruction STRUCT <\
      serviceId VARCHAR,\
      features STRUCT<\
        feature array<STRUCT<\
          code VARCHAR\
        >>\
      >\
    >\
  >\
>)\
WITH (KAFKA_TOPIC='modify.op.msgs', VALUE_FORMAT='JSON');
```

##Create Stream with extracted data, ready for enhancement  
```
CREATE STREAM "modify.voice.feature.msgs" as SELECT transaction->operatorId AS "operatorId", transaction->instruction->order->operatorOrderId AS "operatorOrderId", transaction->instruction->order->orderId AS "orderId", transaction->instruction->modifyFeaturesInstruction->serviceId AS "serviceId", transaction->instruction->modifyFeaturesInstruction->features AS "features" FROM INSTRUCTIONS_STREAM_1 WITH (KAFKA_TOPIC='modify.voice.feature.msgs', VALUE_FORMAT='JSON');
CREATE STREAM "test2.modify.voice.feature.msgs" as SELECT transaction->operatorId AS "operatorId", transaction->instruction->order->operatorOrderId AS "operatorOrderId", transaction->instruction->order->orderId AS "orderId", transaction->instruction->modifyFeaturesInstruction->serviceId AS "serviceId", transaction->instruction->modifyFeaturesInstruction->features AS "features" FROM INSTRUCTIONS_STREAM_1 WITH (KAFKA_TOPIC='test2.modify.voice.feature.msgs', VALUE_FORMAT='JSON');
```

#Links:
Create a new scala-maven project: https://www.ivankrizsan.se/2016/03/27/creating-a-scala-project-with-maven-dependency-management-for-gatling-testing-in-intellij-idea/

Build a Docker scala kafka microservice: https://saumitra.me/blog/deploying-kafka-dependent-scala-microservices-with-docker-and-sbt/


Friday 30/11/2018
Kafka Consumer:
https://github.com/smallnest/kafka-example-in-scala/blob/master/src/main/scala/com/colobu/kafka/ScalaConsumerExample.scala

Sample project reading / writing to kafka topics: https://github.com/schmiegelow/iwomm-kafka



