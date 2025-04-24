# Tailored for Gitpod - Java 8.0.272 + Maven 3.6.3 + MySQL 5.7.32 + ChromeDriver 86.0.4240.22

FROM buildpack-deps:focal

### base ###
RUN yes | unminimize \
    && apt-get install -yq \
        zip \
        unzip \
        bash-completion \
        build-essential \
        htop \
        jq \
        less \
        locales \
        man-db \
        nano \
        software-properties-common \
        sudo \
        time \
        vim \
        multitail \
        lsof \
    && locale-gen en_US.UTF-8 \
    && mkdir /var/lib/apt/dazzle-marks \
    && apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/*

ENV LANG=en_US.UTF-8

### Git ###
RUN add-apt-repository -y ppa:git-core/ppa \
    && apt-get install -yq git \
    && rm -rf /var/lib/apt/lists/*

### Gitpod user ###
# '-l': see https://docs.docker.com/develop/develop-images/dockerfile_best-practices/#user
RUN useradd -l -u 33333 -G sudo -md /home/gitpod -s /bin/bash -p gitpod gitpod \
    # passwordless sudo for users in the 'sudo' group
    && sed -i.bkp -e 's/%sudo\s\+ALL=(ALL\(:ALL\)\?)\s\+ALL/%sudo ALL=NOPASSWD:ALL/g' /etc/sudoers
ENV HOME=/home/gitpod
WORKDIR $HOME
# custom Bash prompt
RUN { echo && echo "PS1='\[\e]0;\u \w\a\]\[\033[01;32m\]\u\[\033[00m\] \[\033[01;34m\]\w\[\033[00m\] \\\$ '" ; } >> .bashrc

### Gitpod user (2) ###
USER gitpod
# use sudo so that user does not get sudo usage info on (the first) login
RUN sudo echo "Running 'sudo' for Gitpod: success" && \
    # create .bashrc.d folder and source it in the bashrc
    mkdir /home/gitpod/.bashrc.d && \
    (echo; echo "for i in \$(ls \$HOME/.bashrc.d/*); do source \$i; done"; echo) >> /home/gitpod/.bashrc

### Java ###
## Place 'm2-repository' in /workspace because (1) that's a fast volume, (2) it survives workspace-restarts and (3) it can be warmed-up by pre-builds.
LABEL dazzle/layer=lang-java
LABEL dazzle/test=tests/lang-java.yaml
USER gitpod
RUN curl -fsSL "https://get.sdkman.io" | bash \
 && bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh \
             && sdk install java 8.0.282.j9-adpt \
             && sdk install maven \
             && sdk flush archives \
             && sdk flush temp \
             && mkdir /home/gitpod/.m2 \
             && printf '<settings>\n  <localRepository>/workspace/m2-repository/</localRepository>\n</settings>\n' > /home/gitpod/.m2/settings.xml \
             && echo 'export SDKMAN_DIR=\"/home/gitpod/.sdkman\"' >> /home/gitpod/.bashrc.d/99-java \
             && echo '[[ -s \"/home/gitpod/.sdkman/bin/sdkman-init.sh\" ]] && source \"/home/gitpod/.sdkman/bin/sdkman-init.sh\"' >> /home/gitpod/.bashrc.d/99-java"
# above, we are adding the sdkman init to .bashrc (executing sdkman-init.sh does that), because one is executed on interactive shells, the other for non-interactive shells (e.g. plugin-host)

### Prologue (built across all layers) ###
LABEL dazzle/layer=dazzle-prologue
LABEL dazzle/test=tests/prologue.yaml
USER root
RUN curl -o /usr/bin/dazzle-util -L https://github.com/csweichel/dazzle/releases/download/v0.0.3/dazzle-util_0.0.3_Linux_x86_64 \
    && chmod +x /usr/bin/dazzle-util
# merge dpkg status files
RUN cp /var/lib/dpkg/status /tmp/dpkg-status \
    && for i in $(ls /var/lib/apt/dazzle-marks/*.status); do /usr/bin/dazzle-util debian dpkg-status-merge /tmp/dpkg-status $i > /tmp/dpkg-status; done \
    && cp -f /var/lib/dpkg/status /var/lib/dpkg/status-old \
    && cp -f /tmp/dpkg-status /var/lib/dpkg/status
# copy tests to enable the self-test of this image
COPY tests /var/lib/dazzle/tests

### MYSQL ###

RUN set -ex; \
# gpg: key 3A79BD29: public key "MySQL Release Engineering <mysql-build@oss.oracle.com>" imported
	key='bca43417c3b485dd128ec6d4b7b3b788a8d3785c'; \
	export GNUPGHOME="$(mktemp -d)"; \
	gpg --batch --keyserver keyserver.ubuntu.com --recv-keys "$key"; \
	gpg --batch --export "$key" > /etc/apt/trusted.gpg.d/mysql.gpg; \
	gpgconf --kill all; \
	rm -rf "$GNUPGHOME"; \
	apt-key list > /dev/null



RUN apt-get install -y --no-install-recommends \
# for MYSQL_RANDOM_ROOT_PASSWORD
		# pwgen \
# for mysql_ssl_rsa_setup
		openssl \
# FATAL ERROR: please install the following Perl modules before executing /usr/local/mysql/scripts/mysql_install_db:
# File::Basename
# File::Copy
# Sys::Hostname
# Data::Dumper
		perl \
# install "xz-utils" for .sql.xz docker-entrypoint-initdb.d files
		xz-utils \
	&& rm -rf /var/lib/apt/lists/*

ENV MYSQL_MAJOR 5.7
ENV MYSQL_VERSION 5.7.\*

RUN echo "deb http://repo.mysql.com/apt/ubuntu/ bionic mysql-${MYSQL_MAJOR}" > /etc/apt/sources.list.d/mysql.list

RUN apt-get update; \
	apt-get install -y mysql-client=${MYSQL_VERSION}

# the "/var/lib/mysql" stuff here is because the mysql-server postinst doesn't have an explicit way to disable the mysql_install_db codepath besides having a database already "configured" (ie, stuff in /var/lib/mysql/mysql)
# also, we set debconf keys to make APT a little quieter
RUN { \
		echo mysql-community-server mysql-community-server/data-dir select ''; \
		echo mysql-community-server mysql-community-server/root-pass password ''; \
		echo mysql-community-server mysql-community-server/re-root-pass password ''; \
		echo mysql-community-server mysql-community-server/remove-test-db select false; \
	} | debconf-set-selections \
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

# Install our own MySQL config
COPY mysql.cnf /etc/mysql/mysql.conf.d/mysqld.cnf

# Install default-login for MySQL clients
COPY client.cnf /etc/mysql/mysql.conf.d/client.cnf

COPY mysql-bashrc-launch.sh /etc/mysql/mysql-bashrc-launch.sh

RUN mkdir -p /workspace/mysql/ && chmod 777 /workspace/mysql/
RUN chmod -R 777 /var/log/mysql


# Google Chrome
COPY --from=markhobson/maven-chrome /opt/google/chrome/google-chrome /opt/google/chrome/google-chrome

# ChromeDriver
COPY --from=markhobson/maven-chrome /opt/chromedriver* /usr/bin/chromedriver


EXPOSE 3306 33060
EXPOSE 8080

# Install maven dependencies away from /workspace
RUN printf '<settings>\n  <localRepository>/home/gitpod/m2-repository/</localRepository>\n</settings>\n' > /home/gitpod/.m2/settings.xml
USER gitpod
ENV JAVA_HOME /home/gitpod/.sdkman/candidates/java/current
RUN git clone https://bitbucket.org/okusche/soda4lca/src/6.x-branch/ stable
WORKDIR $HOME/stable
RUN /home/gitpod/.sdkman/candidates/maven/current/bin/mvn --no-transfer-progress org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=$(find $HOME/stable/Node/lib/primefaces/ -name '*.jar')
RUN /home/gitpod/.sdkman/candidates/maven/current/bin/mvn --no-transfer-progress install -DskipTests -DskipITs
WORKDIR $HOME

# Place 'm2-repository' in /workspace because (1) that's a fast volume, (2) it survives workspace-restarts and (3) it can be warmed-up by pre-builds.
RUN printf '<settings>\n  <localRepository>/workspace/m2-repository/</localRepository>\n</settings>\n' > /home/gitpod/.m2/settings.xml

# make sure to run the following on init:
# cp -rf /home/gitpod/m2-repository/ /workspace/

# okworx/gitpod:1.0