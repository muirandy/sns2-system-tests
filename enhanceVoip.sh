#!/usr/bin/env bash

fileContents=$(<./enhanceVoipSwitchService1.sql)
../ksql/bin/ksql http://localhost:8088 <<< "$fileContents"
fileContents=$(<./enhanceVoipSwitchService2.sql)
../ksql/bin/ksql http://localhost:8088 <<< "$fileContents"
fileContents=$(<./enhanceVoipSwitchService3.sql)
../ksql/bin/ksql http://localhost:8088 <<< "$fileContents"
fileContents=$(<./enhanceVoipSwitchService4.sql)
../ksql/bin/ksql http://localhost:8088 <<< "$fileContents"
fileContents=$(<./enhanceVoipSwitchService5.sql)
../ksql/bin/ksql http://localhost:8088 <<< "$fileContents"
fileContents=$(<./enhanceVoipSwitchService6.sql)
../ksql/bin/ksql http://localhost:8088 <<< "$fileContents"

exit
