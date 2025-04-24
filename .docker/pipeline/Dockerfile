FROM markhobson/maven-chrome:jdk-11

LABEL maintainer="soda4lca-request@lists.kit.edu"

RUN set -ex; \
# gpg: key 3A79BD29: public key "MySQL Release Engineering <mysql-build@oss.oracle.com>" imported
	key='bca43417c3b485dd128ec6d4b7b3b788a8d3785c'; \
	export GNUPGHOME="$(mktemp -d)"; \
	gpg --batch --keyserver keyserver.ubuntu.com --recv-keys "$key"; \
	gpg --batch --export "$key" > /etc/apt/trusted.gpg.d/mysql.gpg; \
	gpgconf --kill all; \
	rm -rf "$GNUPGHOME"; \
	apt-key list > /dev/null

ENV MYSQL_MAJOR 8.0
ENV MYSQL_VERSION 8.0.\*

RUN echo "deb http://repo.mysql.com/apt/ubuntu/ jammy mysql-${MYSQL_MAJOR}" > /etc/apt/sources.list.d/mysql.list

RUN apt-get update; \
	apt-get install -y mysql-client=${MYSQL_VERSION} zip

# the "/var/lib/mysql" stuff here is because the mysql-server postinst doesn't have an explicit way to disable the mysql_install_db codepath besides having a database already "configured" (ie, stuff in /var/lib/mysql/mysql)
# also, we set debconf keys to make APT a little quieter
RUN { \
		echo mysql-community-server mysql-community-server/data-dir select ''; \
		echo mysql-community-server mysql-community-server/root-pass password ''; \
		echo mysql-community-server mysql-community-server/re-root-pass password ''; \
		echo mysql-community-server mysql-community-server/remove-test-db select false; \
	} | debconf-set-selections \
    && export DEBIAN_FRONTEND=noninteractive \
	&& apt-get install -y mysql-community-server="${MYSQL_VERSION}" && rm -rf /var/lib/apt/lists/* \
	&& rm -rf /var/lib/mysql && mkdir -p /var/lib/mysql /var/run/mysqld \
	&& chown -R mysql:mysql /var/lib/mysql /var/run/mysqld \
# ensure that /var/run/mysqld (used for socket and lock files) is writable regardless of the UID our mysqld instance ends up having at runtime
	&& chmod 1777 /var/run/mysqld /var/lib/mysql \
# comment out a few problematic configuration values
	&& find /etc/mysql/ -name '*.cnf' -print0 \
		| xargs -0 grep -lZE '^(bind-address|log)' \
		| xargs -rt -0 sed -Ei 's/^(bind-address|log)/#&/' \
# don't reverse lookup hostnames, they are usually another container
	&& echo '[mysqld]\nskip-host-cache\nskip-name-resolve' > /etc/mysql/conf.d/docker.cnf


COPY --from=markhobson/maven-chrome:jdk-11 /opt/google/chrome/google-chrome /opt/google/chrome/google-chrome

RUN mysqld --initialize-insecure
# mysqld --daemonize --log-error
# mysql -u root --skip-password -e "ALTER USER 'root'@'127.0.0.1' IDENTIFIED BY 'root';CREATE DATABASE soda; CREATE DATABASE root;CREATE DATABASE root2; GRANT ALL ON *.* to root@'127.0.0.1' IDENTIFIED BY 'root';"