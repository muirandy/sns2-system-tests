#!/usr/bin/env bash

./setupKafka.sh

./modifyVoice.sh
./enhanceVoip.sh

cd ../sns-repoman-db
./gradlew flywayMigrate -i
./createServicesJdbcConnector.sh
./createSwitchServiceJdbcConnector.sh

cd ../sns2-system-tests
./auditing.sh
./analytics.sh

./grafana.sh
./grafanaDashboard.sh

./kibanaDashboard.sh