#!/usr/bin/env bash

fileContents=$(<./modifyVoice1.sql)
../ksql/bin/ksql http://localhost:8088 <<< "$fileContents"
fileContents=$(<./modifyVoice2.sql)
../ksql/bin/ksql http://localhost:8088 <<< "$fileContents"
exit
