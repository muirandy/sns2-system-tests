#!/bin/bash

/usr/bin/ksql http://ksql-server:8088 <<EOF
SET 'auto.offset.reset' = 'earliest';
RUN SCRIPT '/usr/share/ksql-scripts/activeMq1.sql';
RUN SCRIPT '/usr/share/ksql-scripts/activeMq2.sql';
exit
EOF
