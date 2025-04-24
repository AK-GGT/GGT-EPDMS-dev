#!/bin/bash

# FOO="${VARIABLE:-default}"  # If variable not set or null, use default.

# MYSQL SETTINGS
export MYSQL_HOST=${MYSQL_HOST:-"mysqld"}
export MYSQL_PORT=${MYSQL_PORT:-"3306"}
export MYSQL_USER=${MYSQL_USER:-"soda-user"}
export MYSQL_PASSWORD=${MYSQL_PASSWORD:-"soda-pw"}
export MYSQL_DATABASE=${MYSQL_DATABASE:-"soda"}
export MYSQL_DATABASE2=${MYSQL_DATABASE2:-"soda2"}

# TOMCAT SETTINGS
export CATALINA_PORT=${CATALINA_PORT:-"8080"}
export ENCODED_SOLIDUS_HANDLING=${ENCODED_SOLIDUS_HANDLING:-"reject"}

# MAIL SETTINGS
export SODA_MAIL_SENDER=${SODA_MAIL_SENDER:-"root@localhost"}
export SODA_MAIL_HOST=${SODA_MAIL_HOST:-"127.0.0.1"}
export SODA_MAIL_PORT=${SODA_MAIL_PORT:-"25"}
export SODA_MAIL_SITENAME=${SODA_MAIL_SITENAME:-"@APP-TITLE@"}
export SODA_MAIL_AUTH=${SODA_MAIL_AUTH:-"true"}
export SODA_MAIL_USER=${SODA_MAIL_USER:-"<user name of the mail sender>"}
export SODA_MAIL_PASSWORD=${SODA_MAIL_PASSWORD:-"<password of the mail sender>"}

# GLAD SETTINGS
export SODA_GLAD_APIKEY=${SODA_GLAD_APIKEY:-""}

# CAPTCHA SETTINGS
export WEB_CONTEXT_CAPTCHA="""
    <context-param>
        <param-name>primefaces.PRIVATE_CAPTCHA_KEY</param-name>
        <param-value>${WEB_PRIVATE_CAPTCHA}</param-value>
    </context-param>
    <context-param>
        <param-name>primefaces.PUBLIC_CAPTCHA_KEY</param-name>
        <param-value>${WEB_PUBLIC_CAPTCHA}</param-value>
    </context-param>
"""

if [[ -z "${WEB_PRIVATE_CAPTCHA}" || -z "${WEB_PUBLIC_CAPTCHA}" ]]; then
    # wipe out the template if one of the vars is missing
    export WEB_CONTEXT_CAPTCHA=""
    echo "[X] Missing 'WEB_PRIVATE_CAPTCHA' or 'WEB_PRIVATE_CAPTCHA', skipping CAPTCHA configuration..."
fi

# TITLE SETTINGS
export NODE_TITLE=${NODE_TITLE:-"soda4LCA"}
export NODE_SUBTITLE=${NODE_SUBTITLE:-"ILCD Node -- part of ILCD system"}

until mysql --host=$MYSQL_HOST --port=$MYSQL_PORT --user=$MYSQL_USER --password=$MYSQL_PASSWORD $MYSQL_DATABASE -e "select 1;"; do
    echo "[*] Waiting for sql server ..."
    sleep 1
done

if [[ ! -f /opt/soda/soda4LCA.properties.template ]]; then
    cp /soda/soda4LCA.properties.template /opt/soda/soda4LCA.properties.template
    echo "[*] Using default 'soda4LCA.properties.template'. If you have your own custom template, place it in '/opt/soda/soda4LCA.properties.template'"
else 
    echo "[*] Found custom 'soda4LCA.properties.template'. Loading..."
fi

envsubst '${WEB_CONTEXT_CAPTCHA}' <${CATALINA_BASE}/conf/web.xml.template >${CATALINA_BASE}/conf/web.xml

envsubst '${NODE_TITLE},${NODE_SUBTITLE},${SODA_MAIL_SENDER},${SODA_MAIL_HOST},${SODA_MAIL_PORT},${SODA_MAIL_SITENAME},${SODA_MAIL_AUTH},${SODA_MAIL_USER},${SODA_MAIL_PASSWORD},${SODA_GLAD_APIKEY}' </opt/soda/soda4LCA.properties.template >/opt/soda/soda4LCA.properties

envsubst '${MYSQL_HOST},${MYSQL_PORT},${MYSQL_USER},${MYSQL_PASSWORD},${MYSQL_DATABASE},${MYSQL_DATABASE2},${CATALINA_PORT},${ENCODED_SOLIDUS_HANDLING}' <${CATALINA_BASE}/conf/server.xml.template >${CATALINA_BASE}/conf/server.xml


# Keep them for debugging...
# Remove sensitive information from environment AFTER populating the templates
# unset MYSQL_USER MYSQL_PASSWORD SODA_MAIL_SENDER SODA_MAIL_HOST SODA_MAIL_USER SODA_MAIL_PASSWORD SODA_MAIL_SENDER SODA_GLAD_APIKEY WEB_CONTEXT_CAPTCHA WEB_PRIVATE_CAPTCHA WEB_PUBLIC_CAPTCHA

mysqld --daemonize --log-error --user=root --max_connections=1000 --max_allowed_packet=256M
mysql -h localhost -u root --skip-password -e \
 "ALTER USER 'root'@'127.0.0.1' IDENTIFIED WITH mysql_native_password BY 'root';\
 CREATE DATABASE soda;\
 CREATE DATABASE root;\
 CREATE DATABASE root2;\
 CREATE DATABASE soda_test;\
 GRANT ALL PRIVILEGES ON *.* to root@'127.0.0.1';"

echo "[*] Starting Catalina in the background."

# enable job control
set -m
${CATALINA_BASE}/bin/catalina.sh run &

if [[ -v SECRET_CIPHER_KEY && -v SECRET_PRIVATE_KEY && -v SECRET_PUBLIC_KEY ]]; then
    # if all of these vars are set
    # decode and write them to CATALINA_BASE

    echo "[*] Writing SECRET_KEYs into ${CATALINA_BASE}..."

    echo ${SECRET_CIPHER_KEY} | base64 -d > ${CATALINA_BASE}/cipher.key
    echo ${SECRET_PRIVATE_KEY} | base64 -d > ${CATALINA_BASE}/private.key
    echo ${SECRET_PUBLIC_KEY} | base64 -d > ${CATALINA_BASE}/public.key

    echo "[*] Adjust permissions of cipher.key, private.key and public.key"
    
    # find ${CATALINA_BASE}/*.key -exec chmod 640 {}\;
    chmod 640 ${CATALINA_BASE}/cipher.key
    chmod 640 ${CATALINA_BASE}/private.key
    chmod 640 ${CATALINA_BASE}/public.key
    
else
    # if one of these vars are NOT set
    # let tomcat generate them from scratch and
    # we will echo them after x seconds.
    # most probably first startup?

    # wait a catalina to start
    echo "[*] Waiting a 60 seconds before bringing Catalina to the foreground, please hold..."
    sleep 60

    echo "[*] Writing encoded SECRET_KEYs into /soda/keys.env"
    echo -e \
"""# keys.env:
# this file contains a base64-encoded copy of the
# secret keys (cipher, private, public) used by tomcat.
# you can set theses vars in your environment to prevent
# tomcat for generating new keys from scratch.

SECRET_CIPHER_KEY=`base64 -w0 ${CATALINA_BASE}/cipher.key`
SECRET_PRIVATE_KEY=`base64 -w0 ${CATALINA_BASE}/private.key`
SECRET_PUBLIC_KEY=`base64 -w0 ${CATALINA_BASE}/public.key`
""" > /soda/keys.env

    chmod 640 /soda/keys.env
fi

# bring back catalina to the foreground
echo "[*] Bringing back Catalina to the foreground"
fg
