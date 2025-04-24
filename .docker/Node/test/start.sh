#!/bin/bash

if [[ -z $MYSQL_HOST ]]
then
    export MYSQL_HOST="localhost"
fi
if [[ -z $MYSQL_PORT ]]
then
    export MYSQL_PORT="3306"
fi
if [[ -z $MYSQL_USER ]]
then
    export MYSQL_USER="root"
fi
if [[ -z $MYSQL_PASSWORD ]]
then
    export MYSQL_PASSWORD="root"
fi
if [[ -z $MYSQL_DATABASE ]]
then
    export MYSQL_DATABASE="root"
fi

if [[ -z $MYSQL_DATABASE2 ]]
then
    export MYSQL_DATABASE2="root2"
fi

if [[ -z $CATALINA_PORT ]]
then
    export CATALINA_PORT="8080"
fi

envsubst '${MYSQL_HOST},${MYSQL_PORT},${MYSQL_USER},${MYSQL_PASSWORD},${MYSQL_DATABASE},${MYSQL_DATABASE2},${CATALINA_PORT}' < ${CATALINA_BASE}/conf/server.xml.template > ${CATALINA_BASE}/conf/server.xml

mysqld --daemonize --log-error --user=root --max_connections=1000 --max_allowed_packet=256M
mysql -u root --skip-password -e \
 "ALTER USER 'root'@'127.0.0.1' IDENTIFIED WITH mysql_native_password BY 'root';\
 CREATE DATABASE soda;\
 CREATE DATABASE root;\
 CREATE DATABASE root2;\
 CREATE DATABASE soda_test;\
 GRANT ALL PRIVILEGES ON *.* to root@'127.0.0.1';"

${CATALINA_BASE}/bin/catalina.sh run
