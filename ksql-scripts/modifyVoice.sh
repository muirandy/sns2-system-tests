#!/usr/bin/env bash

../ksql/bin/ksql http://localhost:8088 <<EOF
RUN SCRIPT './kafka-ksql/scripts/modifyVoice1.sql';
RUN SCRIPT './kafka-ksql/scripts/modifyVoice2.sql';
exit
EOF
