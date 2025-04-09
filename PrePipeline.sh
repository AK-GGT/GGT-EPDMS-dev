#!/bin/bash
mysqld --daemonize --log-error --user=root --max_connections=1000 --max_allowed_packet=256M
mysql -u root --skip-password -e \
 "CREATE USER 'root'@'127.0.0.1' IDENTIFIED WITH mysql_native_password BY 'root';\
 CREATE DATABASE soda;\
 CREATE DATABASE root;\
 CREATE DATABASE root2;\
 CREATE DATABASE soda_test;\
 GRANT ALL PRIVILEGES ON *.* to root@'127.0.0.1';"
cp maven_settings.xml $HOME/.m2/settings.xml
export MAVEN_OPTS='-XX:+UseG1GC -Xmx1500M'