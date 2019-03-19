#!/usr/bin/env bash

./setupKafka.sh

./modifyVoice.sh
./enhanceVoip.sh

cd ../sns-repoman-db
./gradlew flywayMigrate -i
./createServicesJdbcConnector.sh
./createSwitchServiceJdbcConnector.sh

cd ../sns2-system-tests
