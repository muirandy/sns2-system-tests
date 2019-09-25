#!/bin/bash

/usr/bin/ksql http://ksql-server:8088 <<EOF
SET 'auto.offset.reset' = 'earliest';
RUN SCRIPT '/usr/share/ksql-scripts/auditing1.sql';
RUN SCRIPT '/usr/share/ksql-scripts/auditing2.sql';
RUN SCRIPT '/usr/share/ksql-scripts/auditing4.sql';
RUN SCRIPT '/usr/share/ksql-scripts/auditing5.sql';
exit
EOF
