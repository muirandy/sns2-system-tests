#End to End System tests: https://github.com/muirandy/sns2-system-tests

#Welcome

This is a good entry point to the codebase. 

This project holds:
1) End to end test
2) Docker scripts to spin up kafka
3) Useful scripts & instructions!

#Instructions
The first thing you need is an installation of Apache Kafka. We're not using the Confluent platform here.
You can install Kafka natively onto your OS, or you an run it within Docker. The latter is preferred as its
much quicker to get up and running!

We need the following Kafka components:
* Zookeeper
* Kafka servers (3)
* Kafka connect
* KSQL
* An H2 database

#Install KSQL in your working folder (as a sibling of other kafka projects)
```
git clone https://github.com/confluentinc/ksql.git
cd ksql
git checkout tags/v5.1.2
mvn clean compile install -DskipTests
```

#Kafka in Docker
The docker-compose file gives us all the components you need!

To spin it up (on OSX), you need Docker installed.

##Edit hosts file
Add the following entries to your hosts file:
```
127.0.0.1	kafka01.internal-service
127.0.0.1	kafka02.internal-service
127.0.0.1	kafka03.internal-service
127.0.0.1	zookeeper.internal-service
```

##Start it all up
```
docker-compose up
```
Once the brokers have calmed down after starting (ie logs are a bit stable!), run this script:
```
./doItall.sh
```

##Run the microservices
Follow the instructions on these projects:
[Generic XML->JSON Converter](https://github.com/muirandy/sns-incoming-operator-messages-converter)
[Enrich with ServiceId](https://github.com/muirandy/sns-modify-enricher)
[Convert to Knitware XML](https://github.com/muirandy/sns-knitware-converter)

##Run the System test
In this project, run ModifyFeatureSpec. It should go green!
By inspecting the projects, you should be able to tell what topics are involved.
The system test will show you the start and end points.


#Kafka on your native OS
If spinning things up in Docker isn't for you, then you've got the rest of this README to get through. Its not too bad!

##Install Kafka
This should be done in the usual way (see the Kafka website)!
However, note the branch of code ought to be the same tag as the version in the docker-compose file.
The same is important when you are installing KSQL(later on in this file).
At the time of writing, it was 5.1.2.

##Update the config
Replace the config directory of Kafka with https://github.com/muirandy/sns2-kafka-config

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
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 3 --partitions 1 --topic RAW_VOIP_INSTRUCTIONS
```

##List the topics
bin/kafka-topics.sh --list --zookeeper localhost:2181 

##View the messages on the topics:
```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --property print.key=true --topic incoming.op.msgs --from-beginning
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --property print.key=true --topic modify.op.msgs --from-beginning
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --property print.key=true --topic services --from-beginning
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --property print.key=true --topic RAW_VOIP_INSTRUCTIONS --from-beginning
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --property print.key=true --topic enriched.modification.instructions --from-beginning
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --property print.key=true --topic enriched.modification.instructions.with.service --from-beginning
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --property print.key=true --topic SINK_MODIFY_VOIP_INSTRUCTIONS_WITH_SWITCH_ID --from-beginning
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --property print.key=true --topic AUDIT --from-beginning
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --property print.key=true --topic switch.modification.instructions --from-beginning
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
./ksql http://localhost:8088
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

##The Quick Way:
With Kafka and KSQL running:
1) Ensure the topics exist:
```
./setupKafka.sh
```
2) Create the streams through KSQL:
```
./modifyVoice.sh
```

##The Slow Way:

###From the KSQL prompt, ensure we see data already present in topics:
```
SET 'auto.offset.reset' = 'earliest';
```

###Create stream to read the generic Json
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

###Create Stream with extracted data, ready for enhancement  
```
CREATE STREAM "modify.voice.feature.msgs" as SELECT transaction->operatorId AS "operatorId", transaction->instruction->order->operatorOrderId AS "operatorOrderId", transaction->instruction->order->orderId AS "orderId", transaction->instruction->modifyFeaturesInstruction->serviceId AS "serviceId", transaction->instruction->modifyFeaturesInstruction->features AS "features" FROM INSTRUCTIONS_STREAM_1 WITH (KAFKA_TOPIC='modify.voice.feature.msgs', VALUE_FORMAT='JSON');
```


#Elasticsearch

##List all indices:
```
curl -X GET "localhost:9200/_cat/indices?v"
```

##Show the types on an index
```
curl -X GET "localhost:9200/audit/_mappings?pretty"
```

##Show the index content
```
curl -X GET "localhost:9200/audit/_search?q=*&pretty"
```

##To search for the order id 2370787351871460436:
```
curl -X GET "localhost:9200/audit/_search" -H 'Content-Type: application/json' -d'
{
  "query": { "match": { "ORDER_ID": "2370787351871460436" } }
}
'
```

##To delete elasticsearch connector:
```
curl -X DELETE http://localhost:8083/connectors/elasticsearch-sink
```

##Delete an ES index
```
curl -X DELETE "localhost:9200/audit?pretty"
```

##Delete a ES template
```
curl -X DELETE "localhost:9200/_template/template_1"
```



#Links:
Create a new scala-maven project: https://www.ivankrizsan.se/2016/03/27/creating-a-scala-project-with-maven-dependency-management-for-gatling-testing-in-intellij-idea/

Build a Docker scala kafka microservice: https://saumitra.me/blog/deploying-kafka-dependent-scala-microservices-with-docker-and-sbt/


Friday 30/11/2018
Kafka Consumer:
https://github.com/smallnest/kafka-example-in-scala/blob/master/src/main/scala/com/colobu/kafka/ScalaConsumerExample.scala

Sample project reading / writing to kafka topics: https://github.com/schmiegelow/iwomm-kafka


###Old Docker versions for mac:
https://docs.docker.com/docker-for-mac/release-notes/#docker-community-edition-17120-ce-mac49-2018-01-19

