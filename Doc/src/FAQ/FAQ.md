![ ](images/soda4LCA_logo_sm.png)

# soda4LCA Frequently Asked Questions (FAQ)


## What is soda4LCA?

soda4LCA is a web-based database application designed to store and
retrieve Life Cycle Inventory (LCI) and Life Cycle Impact Assessment
(LCIA) datasets formatted in the ILCD format. It also exposed a RESTful
service interface to communicate directly with other LCA software tools
and/or databases. Multiple soda4LCA nodes can be joined to a network,
where a search operation will query all nodes in the network.

## Do I have to purchase a license?

No. soda4LCA is provided in source and binary form under the [GNU
Affero General Public License (AGPL)](http://www.gnu.org/licenses/)
that gives you legal permission to run, copy, distribute and/or modify 
the software.

## Does soda4LCA include any LCI/LCIA data?

No. soda4LCA is merely a sofware application. One source where you may
obtain ILCD datasets free of charge is the European Commission's
Platform on LCA at <http://eplca.jrc.ec.europa.eu/>.

## What data formats are supported?

Currently, the European Commission's International Reference Life Cycle
Data Format (ILCD Format) version 1.1 is supported.


## What do I need to run a soda4LCA database node?

You need a computer running an operating system that supports both Java
and MySQL. Usually that will be Linux, Mac OS X or Windows. Java
(version 1.6 or newer), a MySQL database (version 5.0 or newer) and
Tomcat (version 6.0 or newer) need to be installed on that machine.


## I want to join multiple nodes to a network. Do I need to setup a registry?

To join multiple soda4LCA nodes to a network, a registry is not
necessarily needed. For every node that is supposed to be searching
other nodes, follow these steps:

1.  Log in to the administration interface.

2.  From the "Network" menu, select "Add Node".

3.  Enter the service URL of the node you want to add to the list of
    network nodes.

4.  Repeat step 2 and 3 for every node that you want to be queried.


## When trying to upgrade my existing soda4LCA instance from a previous version, I'm getting weird 'database schema' errors on start-up. What's wrong?

Either your database schema is indeed broken, or you may have encountered the following scenario.

**Scenario**

If you're setting up the soda4LCA instance in a multi-platform environment or use a pre-existing database
schema, you may encounter this error during start-up:

```
Caused by: java.lang.RuntimeException: FATAL ERROR: database schema is not properly initialized
	at de.iai.ilcd.configuration.ConfigurationService.migrateDatabaseSchema(ConfigurationService.java:414)
	at de.iai.ilcd.configuration.ConfigurationService.<init>(ConfigurationService.java:173)
	at de.iai.ilcd.configuration.ConfigurationService.<clinit>(ConfigurationService.java:50)
	... 74 more
Caused by: com.googlecode.flyway.core.api.FlywayException: Validate failed. Found differences between applied migrations and available migrations: Migration Checksum mismatch for migration V1__Base_version_2.sql: DB=-924103272, Classpath=375880760
	at com.googlecode.flyway.core.Flyway.doValidate(Flyway.java:917)
	at com.googlecode.flyway.core.Flyway.access$300(Flyway.java:59)
	at com.googlecode.flyway.core.Flyway$2.execute(Flyway.java:894)
	at com.googlecode.flyway.core.Flyway$2.execute(Flyway.java:888)
	at com.googlecode.flyway.core.Flyway.execute(Flyway.java:1200)
	at com.googlecode.flyway.core.Flyway.validate(Flyway.java:888)
	at de.iai.ilcd.configuration.ConfigurationService.migrateDatabaseSchema(ConfigurationService.java:411)
	... 76 more
```

**Explanation**

On every start-up soda4LCA validates the integrity of your database schema.

As a part of this procedure, *checksums* for *applied* migrations (database schema) are compared to checksums
generated from *available* migrations (soda4LCA instance). Even if those checksums have been generated on the
same migrations (= being valid) they may differ - e.g. if being generated on *different platforms*
(the algorithm is platform dependent).

**Solution**

If you think the above scenario could match your case (especially when your instance is running on Windows) *and* are sure your database schema is ok,
then you may want to try adding the line
> flyway.validate = false

in your soda4LCA.properties file. But, of course, be advised:

**Please backup your database schema before attempting to bypass validation.**


## When trying to access a process dataset, I'm getting an error and in the logs it says "com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException: Table 'myschemaname.Flow_NAME' doesn't exist". What's wrong?

In case you have been migrating your instance from a Windows machine to an OS with a case-sensitive
file system such as Linux, due to different [platform defaults](https://dev.mysql.com/doc/refman/5.7/en/identifier-case-sensitivity.html)
for MySQL you'll have to manually change the names of some tables. 
Stop the application and issue these statements on your database:

```sql
SET FOREIGN_KEY_CHECKS=0;
ALTER TABLE `contact_description` RENAME TO `Contact_DESCRIPTION`;
ALTER TABLE `contact_name` RENAME TO `Contact_NAME`;
ALTER TABLE `flow_description` RENAME TO `Flow_DESCRIPTION`;
ALTER TABLE `flow_name` RENAME TO `Flow_NAME`;
ALTER TABLE `flowproperty_description` RENAME TO `FlowProperty_DESCRIPTION`;
ALTER TABLE `flowproperty_name` RENAME TO `FlowProperty_NAME`;
ALTER TABLE `lciamethod_description` RENAME TO `LCIAMethod_DESCRIPTION`;
ALTER TABLE `lciamethod_name` RENAME TO `LCIAMethod_NAME`;
ALTER TABLE `process_name` RENAME TO `Process_NAME`;
ALTER TABLE `process_description` RENAME TO `Process_DESCRIPTION`;
ALTER TABLE `source_description` RENAME TO `Source_DESCRIPTION`;
ALTER TABLE `source_name` RENAME TO `Source_NAME`;
ALTER TABLE `unitgroup_description` RENAME TO `UnitGroup_DESCRIPTION`;
ALTER TABLE `unitgroup_name` RENAME TO `UnitGroup_NAME`;
SET FOREIGN_KEY_CHECKS=1;
```

Then start your application again and you should be fine.


## My soda4LCA instance is behind a reverse proxy (e.g. nginx) and I get a blank screen when trying to log in or search.

If this happens and inspection of the browser activity reveals that actually a 403 is returned by the server, you 
probably need to add the public address of your instance to the list of allowed origins in the CORS filter section of
your instance's `web.xml` (Docker setup: `web.xml.template`) like this:

```xml
<filter>
  <filter-name>CorsFilter</filter-name>
  <filter-class>org.apache.catalina.filters.CorsFilter</filter-class>
  <init-param>
      <param-name>cors.allowed.origins</param-name>
      <param-value>https://data.acme.org</param-value>
    </init-param>
</filter>
<filter-mapping>
  <filter-name>CorsFilter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```