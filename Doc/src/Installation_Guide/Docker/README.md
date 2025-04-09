# soda4LCA on Docker


![docker-logo](https://www.docker.com/wp-content/uploads/2022/03/horizontal-logo-monochromatic-white.png)

## docker

please refer to [Configuration_Options_Guide.md](../Configuration_Options_Guide.md) for the full list of configurable attributes.

```bash
$ docker run -e 'MYSQL_HOST=mysql' \
             -e 'MYSQL_USER=user'
             -e 'MYSQL_PASSWORD=pass'\
             -e 'MYSQL_DATABASE=soda' \
             ...
             -p 8080:8080 okworx/soda4lca:[version]
```


or run on the host network


```bash
$ docker run -e 'MYSQL_HOST=mysql' \
             -e 'MYSQL_USER=user'
             -e 'MYSQL_PASSWORD=pass'\
             -e 'MYSQL_DATABASE=soda' \
             ...
             --net='host' okworx/soda4lca:[version] 
```


passing environments from environment file (`--env-file`)

```bash
$ docker run --env-file sample.env -p 8080:8080 okworx/soda4lca:[version]
```


## bind mounts for persistent information

In order to persist contents such as external files, validation profiles and cached download ZIPs
across container upgrades, bind mount the following paths to respective folders on the host:

```bash
/opt/soda/data/datafiles
/opt/soda/data/validation_profiles
/opt/soda/data/tmp/zips
```

for example with

```bash
$ docker run --env-file sample.env -p 8080:8080  \
    --mount type=bind,source=/path/to/my/datafiles,target=/opt/soda/data/datafiles \
    --mount type=bind,source=/path/to/my/validation_profiles,target=/opt/soda/data/validation_profiles \
    --mount type=bind,source=/path/to/my/zips,target=/opt/soda/data/tmp/zips \
    okworx/soda4lca:[version]
```


If you need to modify more properties in your soda4LCA.properties than are covered by the 
available environment variables, simply bind mount your `soda4LCA.properties` on the host to
`/opt/soda/soda4LCA.properties.template` on the container using the optional `mount` parameter: 

```bash
--mount type=bind,source=/path/to/my/soda4LCA.properties,target=/opt/soda/soda4LCA.properties.template
```


## docker + customizations


use official soda4LCA image as a base image for your custom container:


```Dockerfile
FROM okworx/soda4lca-mirror:${STABLE_TAG}

LABEL \
	Maintainer "ok*worx <info@okworx.com>" \
	MailingList "soda4LCA@lists.kit.edu" \
	License "GNU Affero General Public License (AGPL) 3.0"

ENV CATALINA_HOME /usr/local/tomcat

# COPY theme/mytheme.jar ${CATALINA_HOME}/lib/
# COPY descriptors/web.xml.template ${CATALINA_HOME}/conf/
COPY descriptors/static.xml ${CATALINA_HOME}/conf/Catalina/localhost/
COPY contents/ /opt/soda/static/
COPY descriptors/soda4LCA.properties.template /opt/soda/soda4LCA.properties.template

CMD [ "/soda/start.sh" ]

```

```bash
$ cd [project home base directory]
$ touch Dockerfile_custom
$ docker build -f Dockerfile_custom -t soda4lca:custom .
```

then spin up a container using one of the commands above.


## docker-compose


```bash
$ touch docker-compose.yml
$ docker-compose -f docker-compose.yml up
```


```yml
version: '3'
services:
  soda_node1:
    container_name: "soda_node1"
    hostname: "soda_node1"
    image: okworx/soda4lca:[version]
    restart: on-failure
    env_file:
      - sample.env
    volumes:
      - [directory on host machine]/data:/opt/soda/data
    networks:
      soda4LCA_network:

networks:
  soda4LCA_network:
```



with database:


```yml
version: '3'
services:
  soda_node1:
    container_name: "soda_node1"
    hostname: "soda_node1"
    image: okworx/soda4lca:[version]
    restart: on-failure
    ports:
      - 8080:8080
    env_file:
      - sample.env
    volumes:
      - [directory on host machine]/data:/opt/soda/data
    networks:
      soda4LCA_network:

  mysql57:
    image: mysql:5.7
    command: --max_connections=1000 --max_allowed_packet=256M --character-set-server=utf8 --collation-server=utf8_general_ci --tls-version='TLSv1.2'
    container_name: mysql57
    hostname: mysql57
    restart: always
    environment:
      MYSQL_ROOT_HOST: '%'
      MYSQL_ROOT_PASSWORD: 'rootpasswordgoeshere'
      MYSQL_DATABASE: 'soda4LCA'
    volumes:
      - [directory on host]mysql/varlib:/var/lib/mysql
    networks:
        soda4LCA_network:

networks:
  soda4LCA_network:
```

## Allow slashes or other non-URL characters in category names

Slashes or other non-URL characters in category names do not work by default due to
the default settings of Shiro's invalidRequest filter. To configure custom shiro.ini
with adjusted settings for invalidRequest filter, the following env variables can be
used:

```
ENCODED_SOLIDUS_HANDLING=decode
CONTEXT_PARAM_1_NAME=shiroConfigLocations
CONTEXT_PARAM_1_VALUE=file:/usr/local/tomcat/conf/shiro.ini
CONTEXT_PARAM_1_OVERRIDE=false
```

while adding additional lines to a copy of shiro.ini in the custom location: 

```
invalidRequest.enabled = true
invalidRequest.blockEncodedForwardSlash = false
#invalidRequest.blockEncodedPeriod = false
#invalidRequest.blockNonAscii = false
#invalidRequest.blockTraversal = false
#invalidRequest.blockSemicolon = false
#invalidRequest.blockBackslash = false
```

The original `shiro.ini` can be found under `src/main/resources/shiro.ini` (sources) or `WEB-INF/classes/shiro.ini` 
(in the WAR). Make sure you  keep the rest of this file in sync with the release version when updating your instance.
