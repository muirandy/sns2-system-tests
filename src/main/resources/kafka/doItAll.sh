#!/usr/bin/env bash

zookeeper=${1:-zookeeper:2181}
kafkaBroker=${2:-broker:29092}
kafkaBrokerContainer=${3:-broker}
ksqlCliContainer=${4:-ksql-cli}
connectServer=${5:-localhost:8083/connectors}
mqUrl=${6:-tcp://localhost:61616}
elasticSearchInternalNetworkUrl=${7:-elasticsearch:9200}

echo " --- doItAll.sh --- "

echo $zookeeper
echo $kafkaBroker
echo $kafkaBrokerContainer
echo $ksqlCliContainer
echo $connectServer
echo $mqUrl

# Create the topics
./src/main/resources/kafka/setupKafka.sh $kafkaBrokerContainer

# KSQL for "Parser" and merging in Switch Service Id
docker exec $ksqlCliContainer /usr/share/ksql-scripts/modifyVoice.sh
docker exec $ksqlCliContainer /usr/share/ksql-scripts/enhanceVoip.sh

# Connectors for DB
#./src/main/resources/kafka-connect/createServicesJdbcConnector.sh
#./src/main/resources/kafka-connect/createSwitchServiceJdbcConnector.sh

# Connectors for AMQ
./src/main/resources/kafka-connect/createActiveMqSourceConnector.sh $kafkaBroker $connectServer $mqUrl
./src/main/resources/kafka-connect/createConfluentActiveMqSinkConnector.sh $kafkaBroker $connectServer $mqUrl

# KSQL to ingest AMQ
docker exec $ksqlCliContainer /usr/share/ksql-scripts/activeMq.sh

# Audit
docker exec $ksqlCliContainer /usr/share/ksql-scripts/auditing.sh

# Analytics
./src/main/resources/kafka-connect/analytics.sh $connectServer

./src/main/resources/kafka/grafana.sh
./src/main/resources/kafka/grafanaDashboard.sh

./src/main/resources/kafka/kibanaDashboard.sh