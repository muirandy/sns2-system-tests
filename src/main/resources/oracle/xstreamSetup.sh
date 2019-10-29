#!/bin/sh

echo 'Create the recovery area for the DB'
docker exec -it faithDB /bin/sh -c "mkdir /home/oracle/app/oracle/oradata/recovery_area"

echo 'Configuring Oracle for XStreams'

docker cp ./src/main/resources/oracle/enableReplicationAndSetupXStream.sql faithDB:/home/oracle/enableReplicationAndSetupXStream.sql
docker cp ./src/main/resources/oracle/createXstreamOutboundServer.sql faithDB:/home/oracle/createXstreamOutboundServer.sql
docker cp ./src/main/resources/oracle/alterXstreamOutboundServer.sql faithDB:/home/oracle/alterXstreamOutboundServer.sql
docker cp ./src/main/resources/oracle/alterForCdc.sql faithDB:/home/oracle/alterForCdc.sql

docker exec -e ORACLE_SID=db1 faithDB sqlplus sys/oracle as SYSDBA @/home/oracle/enableReplicationAndSetupXStream.sql
docker exec -e ORACLE_SID=db1 faithDB sqlplus c##xstrmadmin/xsa@//localhost:1521/db1 @/home/oracle/createXstreamOutboundServer.sql
docker exec -e ORACLE_SID=db1 faithDB sqlplus sys/oracle as SYSDBA @/home/oracle/alterXstreamOutboundServer.sql
docker exec -e ORACLE_SID=db1 faithDB sqlplus sys/oracle as SYSDBA @/home/oracle/alterForCdc.sql
