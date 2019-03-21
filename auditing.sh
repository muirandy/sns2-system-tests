#!/usr/bin/env bash

../ksql/bin/ksql http://localhost:8088 <<EOF
SET 'auto.offset.reset' = 'earliest';
RUN SCRIPT './auditing1.sql';
RUN SCRIPT './auditing2.sql';
RUN SCRIPT './auditing3.sql';
RUN SCRIPT './auditing4.sql';
RUN SCRIPT './auditing5.sql';
exit
EOF
