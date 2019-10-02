# End to End System tests: https://github.com/muirandy/sns2-system-tests

# Welcome

This is a good entry point to the codebase. 

This project holds:
1) End to end test
2) Docker scripts to spin up kafka
3) Useful scripts & instructions!

# New round these parts? Some resources:
## Book bundle from Confluent:
https://www.confluent.io/apache-kafka-stream-processing-book-bundle
## Apache Kafka Quickstart:
https://kafka.apache.org/quickstart



# Instructions
The first thing you need is an installation of Apache Kafka. We are using the Confluent platform here.
Its by far easiest to run it from docker-compose. That's what we do here!

We need the following Kafka components:
* Zookeeper
* Kafka servers (1 - allowing us to use the "Developer" license)
* Kafka connect server
* KSQL Server
* Schema Registry
* Confluent Control Centre
* REST Proxy
* KSQL Client (Runs in the docker network and can connect to the ksql-server)

And also:
* An H2 database
* ElasticSearch
* Kibana
* Grafana
* Zipkin


# Kafka in Docker
The docker-compose file gives us all the components you need!

To spin it up (on OSX), you need Docker installed.

## Edit hosts file
Add the following entries to your hosts file:
```
127.0.0.1	elasticsearch
127.0.0.1	kabana
127.0.0.1	grafana
127.0.0.1	zipkin
```

## Start it all up
```
docker-compose up
```
Once the brokers have calmed down after starting (ie logs are a bit stable!), run this script:
```
./doItall.sh
```

## Run the microservices
Follow the instructions on these projects:
[Generic XML->JSON Converter](https://github.com/muirandy/sns-incoming-operator-messages-converter)
[Enrich with ServiceId](https://github.com/muirandy/sns-modify-enricher)
[Convert to Knitware XML](https://github.com/muirandy/sns-knitware-converter)

## Run the System test
In this project, run ModifyFeatureSpec. It should go green!
By inspecting the projects, you should be able to tell what topics are involved.
The system test will show you the start and end points.


# Useful Commands for Kafka
## Create a topic if necessary (note that this is taken care of by either the setupKafka.sh or doItAll.sh scripts):
```
docker exec -it broker kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 4 --topic NEW_TOPIC_NAME
```

## List the topics
```
docker exec -it broker kafka-topics --list --zookeeper zookeeper:2181 
```

## View the messages on the topics:
```
docker exec -it broker kafka-console-consumer --bootstrap-server broker:9092 --property print.key=true --topic incoming.activemq --from-beginning
docker exec -it broker kafka-console-consumer --bootstrap-server broker:9092 --property print.key=true --topic INCOMING_OP_MSGS --from-beginning
docker exec -it broker kafka-console-consumer --bootstrap-server broker:9092 --property print.key=true --topic modify.op.msgs --from-beginning
docker exec -it broker kafka-console-consumer --bootstrap-server broker:9092 --property print.key=true --topic services --from-beginning
docker exec -it broker kafka-console-consumer --bootstrap-server broker:9092 --property print.key=true --topic voip-switch-services --from-beginning
docker exec -it broker kafka-console-consumer --bootstrap-server broker:9092 --property print.key=true --topic RAW_VOIP_INSTRUCTIONS --from-beginning
docker exec -it broker kafka-console-consumer --bootstrap-server broker:9092 --property print.key=true --topic enriched.modification.instructions --from-beginning
docker exec -it broker kafka-console-consumer --bootstrap-server broker:9092 --property print.key=true --topic enriched.modification.instructions.with.dn --from-beginning
docker exec -it broker kafka-console-consumer --bootstrap-server broker:9092 --property print.key=true --topic SINK_MODIFY_VOIP_INSTRUCTIONS_WITH_SWITCH_ID --from-beginning
docker exec -it broker kafka-console-consumer --bootstrap-server broker:9092 --property print.key=true --topic AUDIT --from-beginning
docker exec -it broker kafka-console-consumer --bootstrap-server broker:9092 --property print.key=true --topic switch.modification.instructions --from-beginning
```

## Write directly to a topic:
```
docker exec -it broker kafka-console-producer --broker-list broker:9092 --topic INCOMING_OP_MSGS
```

## Write to XSLT topic:
```
docker exec -it <containerId> kafka-console-producer --broker-list broker:9092 --topic XSLT --property "parse.key=true" --property "key.separator=:"
```

## Reset topic(s) for a specific app:
```
docker exec -it broker kafka-streams-application-reset --application-id sns-modify-enricher --bootstrap-servers broker:9092 --input-topics services,RAW_VOIP_INSTRUCTIONS
```

## Examine a topic setup:
```
docker exec -it broker kafka-topics --zookeeper zookeeper:2181 --topic AUDIT --describe
```

## Change the partitions on a topic:
```
docker exec -it broker kafka-topics --alter --zookeeper zookeeper:2181 --topic AUDIT --partitions 4
```

## Change the replication factor of a topic:
First, create a json file describing what you want, eg:
```json
{"version":1,
 "partitions":[
    {"topic":"AUDIT",
     "partition":0,
     "replicas":[0,1,2]
    }
  ]
}
```
Note that the "replicas" value should hold the ids of the brokers you want the topic replicated on.

Now, change the replication factor:
```
docker exec -it broker kafka-reassign-partitions --zookeeper zookeeper:2181 --reassignment-json-file repl.json --execute
```
Check that it has completed:
```
docker exec -it broker kafka-reassign-partitions --zookeeper zookeeper:2181 --reassignment-json-file repl.json --verify
```

# Kafka Connect
See the connectors:
```
curl localhost:8083/connectors
```

Check the status of a connector:
```
curl localhost:8083/connectors/services-connector/status
```

# KSQL - General Info
You can interact with KSQL via the Confluent Control Centre (localhost:9021).
To interact with it on the command line, run the following:
```
docker exec -it ksql-cli /usr/bin/ksql http://ksql-server:8088
``` 

## Show and update the KSQL properties:
```
ksql> SHOW PROPERTIES;
ksql> SET 'property name' = 'value';
SET 'auto.offset.reset' = 'earliest';
```

## Show the topics on kafka:
```
ksql> SHOW TOPICS;
```

## Show the content of a topic:
```
ksql>  PRINT 'services' FROM BEGINNING;
```

## Creating STREAMs:
```
ksql> CREATE STREAM pageviews (viewtime BIGINT, user_id VARCHAR, page_id VARCHAR) WITH (KAFKA_TOPIC='pageviews-topic', VALUE_FORMAT='JSON');
```

## Creating TABLEs:
```
ksql> CREATE TABLE users (user_id VARCHAR, gender VARCHAR, region_id VARCHAR) WITH (KAFKA_TOPIC='pageviews-topic', KEY='user_id', VALUE_FORMAT='JSON');
```
(KEY must match the key in the kafka message, and also (!??!) a field in the JSON)
```
ksql> CREATE TABLE customers2 AS SELECT EXTRACTJSONFIELD(payload, '$.page') AS page, EXTRACTJSONFIELD(payload,'$.customer_id') AS CUSTOMER_ID FROM CUSTOMERS;
```
(this was indicated it may be having CUSTOMER as another TABLE)

## View information about a STREAM or TABLE:
```
ksql> DESCRIBE <TABLE/STREAM>
ksql> DESCRIBE EXTENDED <TABLE/STREAM>
ksql> SHOW <TABLES/STREAMS>
```

# KSQL - SNS Info

## The Quick Way:
With Kafka and KSQL running:
1) Ensure the topics exist:
```
./setupKafka.sh
```
2) Create the streams through KSQL:
```
./modifyVoice.sh
```

## The Slow Way:

### From the KSQL prompt, ensure we see data already present in topics:
```
SET 'auto.offset.reset' = 'earliest';
```

### Create stream to read the generic Json
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

### Create Stream with extracted data, ready for enhancement  
```
CREATE STREAM "modify.voice.feature.msgs" as SELECT transaction->operatorId AS "operatorId", transaction->instruction->order->operatorOrderId AS "operatorOrderId", transaction->instruction->order->orderId AS "orderId", transaction->instruction->modifyFeaturesInstruction->serviceId AS "serviceId", transaction->instruction->modifyFeaturesInstruction->features AS "features" FROM INSTRUCTIONS_STREAM_1 WITH (KAFKA_TOPIC='modify.voice.feature.msgs', VALUE_FORMAT='JSON');
```


# Elasticsearch

## List all indices:
```
curl -X GET "localhost:9200/_cat/indices?v"
```

## Show the types on an index
```
curl -X GET "localhost:9200/audit/_mappings?pretty"
```

## Show the index content
```
curl -X GET "localhost:9200/audit/_search?q=*&pretty"
```

## To search for the order id 2370787351871460436:
```
curl -X GET "localhost:9200/audit/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": { "match": { "ORDER_ID": "2370787351871460436" } }
}
'
curl -X GET "localhost:9200/audit/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": { "match": { "TRACE_ID": "3cf7b04557e1ccc9" } }
}
'
curl -X GET "localhost:9200/enriched.modification.instructions.with.dn/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": { "match": { "traceId": "ab6377a863c2e7a4" } }
}
'
```

## To delete elasticsearch connector:
```
curl -X DELETE http://localhost:8083/connectors/elasticsearch-sink
```

## Delete an ES index
```
curl -X DELETE "localhost:9200/audit?pretty"
```

## Delete a ES template
```
curl -X DELETE "localhost:9200/_template/template_1"
```



# Links:
Create a new scala-maven project: https://www.ivankrizsan.se/2016/03/27/creating-a-scala-project-with-maven-dependency-management-for-gatling-testing-in-intellij-idea/

Build a Docker scala kafka microservice: https://saumitra.me/blog/deploying-kafka-dependent-scala-microservices-with-docker-and-sbt/


Friday 30/11/2018
Kafka Consumer:
https://github.com/smallnest/kafka-example-in-scala/blob/master/src/main/scala/com/colobu/kafka/ScalaConsumerExample.scala

Sample project reading / writing to kafka topics: https://github.com/schmiegelow/iwomm-kafka



