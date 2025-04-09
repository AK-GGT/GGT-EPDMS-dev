@ECHO OFF
mysqld.exe --daemonize --log-error --user=root --max_connections=1000 --max_allowed_packet=256M
mysql.exe -u root --skip-password -e ^
 "ALTER USER 'root'@'127.0.0.1' IDENTIFIED WITH mysql_native_password BY 'root';"^
 "CREATE DATABASE soda;"^
 "CREATE DATABASE root;"^
 "CREATE DATABASE root2;"^
 "CREATE DATABASE soda_test;"^
 "GRANT ALL PRIVILEGES ON *.* to root@'127.0.0.1';"
COPY maven_settings.xml "%USERPROFILE%/.m2/settings.xml"
SET MAVEN_OPTS=-XX:+UseG1GC
mvn.exe org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=$(find Node/lib/primefaces/ -name '*.jar')