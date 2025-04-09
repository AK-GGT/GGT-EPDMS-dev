# OIDC demo with ![keycloak](https://www.keycloak.org/resources/images/keycloak_logo_200px.svg)

## Step 1: Update your hosts file

To ensure that the redirects are working when using the host's browser to interact with the containers; please update your hosts file `/etc/hosts` as follows: 

```hosts
# Static table lookup for hostnames.
# See hosts(5) for details.
127.0.0.1 localhost keycloak blinky pinky inky clyde
```

## Step 2: Run docker-compose

```bash
$ docker-compose -f docker-compose-oidc.yml up
```

Spins up 6 containers:

* Keycloak on http://keycloak:8080
* MySQL on tcp://mysql57_oidc:3306 
* soda4LCA instance on http://blinky:8081
* soda4LCA instance on http://pinky:8082
* soda4LCA instance on http://inky:8083
* soda4LCA instance on http://clyde:8084

### realm-export.json

Contains the soda-relm and soda-client settings and credentials for keycloak (sans Users).

**Automatically imported, no manual intervention required.**

### oidc_schema.sql

Init 4 database schemas for the 4 soda4LCA instances

```sql
CREATE DATABASE `blinky_db` CHARACTER SET utf8 COLLATE utf8_general_ci;
CREATE DATABASE `pinky_db` CHARACTER SET utf8 COLLATE utf8_general_ci;
CREATE DATABASE `inky_db` CHARACTER SET utf8 COLLATE utf8_general_ci;
CREATE DATABASE `clyde_db` CHARACTER SET utf8 COLLATE utf8_general_ci;
```

**Automatically imported, no manual intervention required.**
