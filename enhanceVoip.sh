#!/usr/bin/env bash

../ksql/bin/ksql http://localhost:8088 <<EOF
RUN SCRIPT './enhanceVoipSwitchService1.sql';
RUN SCRIPT './enhanceVoipSwitchService2.sql';
RUN SCRIPT './enhanceVoipSwitchService3.sql';
RUN SCRIPT './enhanceVoipSwitchService4.sql';
RUN SCRIPT './enhanceVoipSwitchService5.sql';
RUN SCRIPT './enhanceVoipSwitchService6.sql';
exit
EOF

