alter system set db_recovery_file_dest_size = 5G;
alter system set db_recovery_file_dest = '/home/oracle/app/oracle/oradata/recovery_area' scope=spfile;
alter system set enable_goldengate_replication=true;
shutdown immediate
startup mount
alter database archivelog;
alter database open;

-- Should show "Database log mode: Archive Mode":
archive log list

-- Create XStream admin user in the container database
CREATE TABLESPACE xstream_adm_tbs DATAFILE '/home/oracle/app/oracle/oradata/db1/xstream_adm_tbs.dbf' SIZE 25M REUSE AUTOEXTEND ON MAXSIZE UNLIMITED;
CREATE USER c##xstrmadmin IDENTIFIED BY xsa DEFAULT TABLESPACE xstream_adm_tbs QUOTA UNLIMITED ON xstream_adm_tbs;
GRANT CREATE SESSION TO c##xstrmadmin;
BEGIN
   DBMS_XSTREAM_AUTH.GRANT_ADMIN_PRIVILEGE(
      grantee                 => 'c##xstrmadmin',
      privilege_type          => 'CAPTURE',
      grant_select_privileges => TRUE

   );
END;
/

-- Create test user
CREATE USER debezium IDENTIFIED BY dbz;
GRANT CONNECT TO debezium;
GRANT CREATE SESSION TO debezium;
GRANT CREATE TABLE TO debezium;
GRANT CREATE SEQUENCE TO debezium;
ALTER USER debezium QUOTA 100M ON users;

-- Create XStream user
CREATE TABLESPACE xstream_tbs DATAFILE '/home/oracle/app/oracle/oradata/db1/xstream_tbs.dbf' SIZE 25M REUSE AUTOEXTEND ON MAXSIZE UNLIMITED;
CREATE USER c##xstrm IDENTIFIED BY xs DEFAULT TABLESPACE xstream_tbs QUOTA UNLIMITED ON xstream_tbs;
GRANT CREATE SESSION TO c##xstrm;
GRANT SELECT ON V_$DATABASE to c##xstrm;
GRANT FLASHBACK ANY TABLE TO c##xstrm;
GRANT select_catalog_role to c##xstrm;