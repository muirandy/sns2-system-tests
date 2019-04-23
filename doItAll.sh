#!/usr/bin/env bash

./setupKafka.sh

docker exec -it ksql-cli /usr/share/ksql-scripts/modifyVoice.sh
docker exec -it ksql-cli /usr/share/ksql-scripts/enhanceVoip.sh

cd ../sns-repoman-db
./gradlew flywayMigrate -i

cd ../sns2-system-tests
./kafka-connect/createServicesJdbcConnector.sh
./kafka-connect/createSwitchServiceJdbcConnector.sh
./kafka-connect/createActiveMqSourceConnector.sh

docker exec -it ksql-cli /usr/share/ksql-scripts/activeMq.sh
docker exec -it ksql-cli /usr/share/ksql-scripts/auditing.sh

./kafka-connect/analytics.sh

./grafana.sh
./grafanaDashboard.sh

./kibanaDashboard.sh