#!/usr/bin/env bash

../ksql/bin/ksql http://localhost:8088 <<EOF
RUN SCRIPT './modifyVoice1.sql';
RUN SCRIPT './modifyVoice2.sql';
exit
EOF
