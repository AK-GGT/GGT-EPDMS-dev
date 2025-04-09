![ ](images/soda4LCA_logo_sm.png)

# soda4LCA Developer Guide #


## Integration Tests ##

### Introduction ###

The integration tests are a way to ensure the functionality of all components
of soda4LCA. Because the application consists of several modules (*Node* and 
*Registry* being the two which are actual web applications), after code changes 
it has to be ensured that everything is still working fine, especially 
interactions between the *Node* and *Registry* applications.

For that purpose, a comprehensive suite of integration tests has been created 
that can be run in any environment without requiring prerequisites like a 
servlet container or local database. 


### Prerequisites ###

In addition to the requirement by the application, *Perl* needs to be installed
on the machine where the integration tests are executed. 

On Linux, *libaio1* is required in addition:

`$ sudo apt-get install libaio1`


### Running Integration Tests from Maven ###

#### Node Application ####

For running the integration tests for the *Node* application, just execute the 
Maven goal `verify`, for example by calling

    mvn verify

on the command line. 

The tests will run in an embedded servlet container with an embedded database.


### Running Integration Tests from IDE ###

For running tests in Eclipse against a locally running soda4LCA instance, the 
`persistence.xml` from `src/test/resources/META-INF` needs to be copied to 
`src/main/resources/META-INF` in order to disable JPA caching. 

Run the `testng.xml` file in `src/test/resources` with TestNG. 


### Profiles ###

For headless execution, use the `headless` profile (-Pheadless argument).


### Docker images ###

To build the docker image, run

    mvn clean package -pl Node -am -Pdocker_image

and

    cd Node 
    mvn dockerfile:push@docker
    
to push it. 


To build the docker image for a test instance, run

    mvn pre-integration-test -Pdocker_image_test

    cd Node 
    mvn dockerfile:build@docker-test
    mvn dockerfile:tag@docker-test

and

    mvn dockerfile:push@docker-test
    
to push it. 


## Source Code ##

### Node Component ###

Node packages:

- `configuration`

- `model.*`

- `model.dao`

- `persistence`

- `security`

- `services`

- `util`

- `webgui`

- `xml.read`


## Releases


### Maven releases

The procedure for a major and minor release is as follows:

1. Prepare the documentation (release notes and README) in an extra branch, e.g. `doc/6.7.0`. 

2. Create a release branch under `releases`, e.g. `releases/6.7.0` from the main branch (usually `6.x-branch`). 
   
3. Merge the doc branch (e.g. `doc/6.7.0`) into the release branch (e.g. `releases/6.7.0`) via PR.

4. Run the release pipeline on the release branch (e.g. `releases/6.7.0`), setting `VERSION` to the desired version 
   (e.g. `6.7.0`) and `NEXT_DEV_VERSION` to a higher minor version (e.g. `6.7.1`), ***without*** the `SNAPSHOT` suffix.
   
5. After the release pipeline finished successfully, merge the release branch (e.g. `releases/6.7.0`) into the main 
   branch (usually `6.x-branch`) via PR.


For a bugfix release (e.g. `6.7.1`), it's basically the same, except that you start with the already existing release 
branch (e.g. `releases/6.7.0`):

1. Merge the bugfix branch(es) onto the already existing release branch (e.g. `releases/6.7.0`). Make sure the bugfix
   branches are not set to be closed automatically if they haven't yet been merged to the main branch 
   (usually `6.x-branch`).
   
2. Prepare the documentation (release notes and README) in an extra branch, e.g. `doc/6.7.1`.

3. Merge the doc branch (e.g. `doc/6.7.1`) into the release branch (e.g. `releases/6.7.0`) via PR.

4. Run the release pipeline on the release branch (e.g. `releases/6.7.0`), setting `VERSION` to the desired version
   (e.g. `6.7.0`) and `NEXT_DEV_VERSION` to a higher minor version (e.g. `6.7.1`), ***without*** the `SNAPSHOT` suffix.

5. After the release pipeline finished successfully, merge the doc branch (e.g. `doc/6.7.1`) into the main
   branch (usually `6.x-branch`) via PR.


### Docker releases

dockerhub is configured using *Automated Builds* to build based on `Dockerfile_stable` and push a new image to:

1.`okworx/soda4LCA` for **every tagged commit**.

1.`okworx/soda4LCA-mirror` for **every commit in every branch**.



#### Rational behind `okworx/soda4LCA-mirror`

1. Each feature or bugfix has a corresponding image. This facilitates the ability to deploy quick hot fix **without** going through the maven release cycle.
1. Avoid polluting the public `okworx/soda4LCA` with feature & bugfixes branches.


The following snippet is the head of dockerfile used to build custom soda4LCA image in production:

```bash
$ cd deployment_configs/xyz/production
$ head Dockerfile

# STABLE_TAG can be a version number like '6.2.0' or 'hot-branch'
ARG STABLE_TAG
FROM okworx/soda4lca-mirror:${STABLE_TAG}
...
```

In order to release a hotfix or test-out how `feature/abc` from soda4LCA git repo would behave in a custom configuration setup. you can build the `deployment_configs/xyz/production/Dockerfile` with build argument `STABLE_TAG=feature-abc`:

```bash
$ cd deployment_configs/xyz/production/
$ docker build --build-arg STABLE_TAG=feature-abc --tag okworx/xyz:quick-feature-test .
$ docker push okworx/xyz:quick-feature-test
```

or configure the `STABLE_TAG` in the `.env` file if you are using `Makefile` setup.


*please refer to [Docker Strategy](https://bitbucket.org/okusche/soda4lca/wiki/Docker%20Strategy) in the soda4LCA's wiki for a detailed overview.*