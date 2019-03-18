#!/usr/bin/env bash

./setupKafka.sh
cd ../sns-repoman-db
gradle flywayMigrate -i
./createServicesJdbcConnector.sh
./createSwitchServiceJdbcConnector.sh
cd ../sns2-system-tests
./modifyVoice.sh
./enhanceVoip.sh
