#!/usr/bin/env bash

zookeeper=${1:-zookeeper:2181}
kafkaBroker=${2:-broker:29092}
kafkaBrokerContainer=${3:-broker}
ksqlCliContainer=${4:-ksql-cli}
connectServer=${5:-localhost:8083/connectors}
mqUrl=${6:-tcp://localhost:61616}

echo " --- doItAll.sh --- "

echo $zookeeper
echo $kafkaBroker
echo $connectServer
echo $mqUrl


./src/main/resources/kafka/setupKafka.sh $kafkaBrokerContainer

docker exec -it $ksqlCliContainer /usr/share/ksql-scripts/modifyVoice.sh
docker exec -it $ksqlCliContainer /usr/share/ksql-scripts/enhanceVoip.sh

./src/main/resources/kafka-connect/createServicesJdbcConnector.sh
./src/main/resources/kafka-connect/createSwitchServiceJdbcConnector.sh

./src/main/resources/kafka-connect/createActiveMqSourceConnector.sh $kafkaBroker $connectServer $mqUrl
./src/main/resources/kafka-connect/createActiveMqSinkConnector.sh $kafkaBroker $connectServer $mqUrl

docker exec -it $ksqlCliContainer /usr/share/ksql-scripts/activeMq.sh
docker exec -it $ksqlCliContainer /usr/share/ksql-scripts/auditing.sh

./src/main/resources/kafka-connect/analytics.sh

./src/main/resources/kafka/grafana.sh
./src/main/resources/kafka/grafanaDashboard.sh

./src/main/resources/kafka/kibanaDashboard.sh