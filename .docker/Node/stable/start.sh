#!/bin/bash

# FOO="${VARIABLE:-default}"  # If variable not set or null, use default.

# MYSQL SETTINGS
export MYSQL_HOST=${MYSQL_HOST:-"mysqld"}
export MYSQL_PORT=${MYSQL_PORT:-"3306"}
export MYSQL_USER=${MYSQL_USER:-"soda-user"}
export MYSQL_PASSWORD=${MYSQL_PASSWORD:-"soda-pw"}
export MYSQL_DATABASE=${MYSQL_DATABASE:-"soda"}

# TOMCAT SETTINGS
export CATALINA_PORT=${CATALINA_PORT:-"8080"}
export ENCODED_SOLIDUS_HANDLING=${ENCODED_SOLIDUS_HANDLING:-"reject"}

# ADDITIONAL CONTEXT PARAMETERS
export CONTEXT_PARAM_1_NAME=${CONTEXT_PARAM_1_NAME:-"custom_context_param_1"}
export CONTEXT_PARAM_1_VALUE=${CONTEXT_PARAM_1_VALUE:-""}
export CONTEXT_PARAM_1_OVERRIDE=${CONTEXT_PARAM_1_OVERRIDE:-"false"}
export CONTEXT_PARAM_2_NAME=${CONTEXT_PARAM_2_NAME:-"custom_context_param_2"}
export CONTEXT_PARAM_2_VALUE=${CONTEXT_PARAM_2_VALUE:-""}
export CONTEXT_PARAM_2_OVERRIDE=${CONTEXT_PARAM_2_OVERRIDE:-"false"}

# MAIL SETTINGS
export SODA_MAIL_SENDER=${SODA_MAIL_SENDER:-"root@localhost"}
export SODA_MAIL_HOST=${SODA_MAIL_HOST:-"127.0.0.1"}
export SODA_MAIL_PORT=${SODA_MAIL_PORT:-"25"}
export SODA_MAIL_SITENAME=${SODA_MAIL_SITENAME:-"@APP-TITLE@"}
export SODA_MAIL_AUTH=${SODA_MAIL_AUTH:-"true"}
export SODA_MAIL_USER=${SODA_MAIL_USER:-"USERMAILPLACEHOLDER"}
export SODA_MAIL_PASSWORD=${SODA_MAIL_PASSWORD:-"PASSMAILPLACEHOLDER"}

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
# OIDC SETTINGS

export OIDC_BASEURI=${OIDC_BASEURI:-""}
export OIDC_REALM=${OIDC_REALM:-""}
export OIDC_DISCOVERYURI=${OIDC_DISCOVERYURI:-""}
export OIDC_CLIENTID=${OIDC_CLIENTID:-""}
export OIDC_SECRET=${OIDC_SECRET:-""}
export OIDC_CLIENTAUTHENTICATIONMETHOD=${OIDC_CLIENTAUTHENTICATIONMETHOD:-"client_secret_basic"}
export OIDC_PREFERREDJWSALGORITHM=${OIDC_PREFERREDJWSALGORITHM:-"RS256"}
export OIDC_SCOPE=${OIDC_SCOPE:-"openid+profile+email"}
export OIDC_CALLBACKURL=${OIDC_CALLBACKURL:-""}
export OIDC_CALLBACKFILTERDEFAULTURL=${OIDC_CALLBACKFILTERDEFAULTURL:-""}
export OIDC_ROLEMAPPINGS=${OIDC_ROLEMAPPINGS:-""}

if [[ -z "${WEB_PRIVATE_CAPTCHA}" || -z "${WEB_PUBLIC_CAPTCHA}" ]]; then
    # wipe out the template if one of the vars is missing
    export WEB_CONTEXT_CAPTCHA=""
    echo "[X] Missing 'WEB_PRIVATE_CAPTCHA' or 'WEB_PRIVATE_CAPTCHA', skipping CAPTCHA configuration..."
fi

# TITLE SETTINGS
export NODE_TITLE=${NODE_TITLE:-"soda4LCA"}
export NODE_SUBTITLE=${NODE_SUBTITLE:-""}

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

envsubst '${NODE_TITLE},${NODE_SUBTITLE},${SODA_MAIL_SENDER},${SODA_MAIL_HOST},${SODA_MAIL_PORT},${SODA_MAIL_SITENAME},${SODA_MAIL_AUTH},${SODA_MAIL_USER},${SODA_MAIL_PASSWORD},${SODA_GLAD_APIKEY},${OIDC_BASEURI},${OIDC_REALM},${OIDC_DISCOVERYURI},${OIDC_CLIENTID},${OIDC_SECRET},${OIDC_CLIENTAUTHENTICATIONMETHOD},${OIDC_PREFERREDJWSALGORITHM},${OIDC_SCOPE},${OIDC_CALLBACKURL},${OIDC_CALLBACKFILTERDEFAULTURL},${OIDC_ROLEMAPPINGS}' </opt/soda/soda4LCA.properties.template >/opt/soda/soda4LCA.properties

envsubst '${MYSQL_HOST},${MYSQL_PORT},${MYSQL_USER},${MYSQL_PASSWORD},${MYSQL_DATABASE},${CATALINA_PORT},${ENCODED_SOLIDUS_HANDLING},${CONTEXT_PARAM_1_NAME},${CONTEXT_PARAM_1_VALUE},${CONTEXT_PARAM_1_OVERRIDE},${CONTEXT_PARAM_2_NAME},${CONTEXT_PARAM_2_VALUE},${CONTEXT_PARAM_2_OVERRIDE}' <${CATALINA_BASE}/conf/server.xml.template >${CATALINA_BASE}/conf/server.xml


# Remove sensitive information from environment AFTER populating the templates
unset MYSQL_USER MYSQL_PASSWORD SODA_MAIL_SENDER SODA_MAIL_HOST SODA_MAIL_USER SODA_MAIL_PASSWORD SODA_MAIL_SENDER SODA_GLAD_APIKEY WEB_CONTEXT_CAPTCHA WEB_PRIVATE_CAPTCHA WEB_PUBLIC_CAPTCHA


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

if [[ -f /opt/soda/robots.txt ]]; then
    cp /opt/soda/robots.txt ${CATALINA_BASE}/webapps/ROOT/
fi

# bring back catalina to the foreground
echo "[*] Bringing back Catalina to the foreground"
fg