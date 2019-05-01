#!/usr/bin/env bash

../ksql/bin/ksql http://localhost:8088 <<EOF
RUN SCRIPT './ksql-scripts/modifyVoice1.sql';
RUN SCRIPT './ksql-scripts/modifyVoice2.sql';
exit
EOF
