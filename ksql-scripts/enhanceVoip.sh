#!/bin/bash

/usr/bin/ksql http://ksql-server:8088 <<EOF
RUN SCRIPT '/usr/share/ksql-scripts/enhanceService1.sql';
RUN SCRIPT '/usr/share/ksql-scripts/enhanceService2.sql';
RUN SCRIPT '/usr/share/ksql-scripts/enhanceService3.sql';
RUN SCRIPT '/usr/share/ksql-scripts/enhanceService4.sql';

RUN SCRIPT '/usr/share/ksql-scripts/enhanceVoipSwitchService1.sql';
RUN SCRIPT '/usr/share/ksql-scripts/enhanceVoipSwitchService4.sql';
RUN SCRIPT '/usr/share/ksql-scripts/enhanceVoipSwitchService5.sql';
RUN SCRIPT '/usr/share/ksql-scripts/enhanceVoipSwitchService6.sql';
exit
EOF

