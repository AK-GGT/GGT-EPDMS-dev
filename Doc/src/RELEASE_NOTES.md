CHANGES - soda4LCA
==================

7.14.0
------

*Improvements:*

- Dataset detail: add RMI and TMR indicators for sorting.


*Bug fixes:*

- REST API: gracefully handle broken or missing compliance when generating JSON.
- Export: always use slashes for paths in ZIPs regardless of host OS.
- Registry: fix logging and print message to log on startup.


*Docker image:*

- Add support for configuring handling of categories with slashes via encodedSolidusHandling parameter add custom context parameters.


7.13.0
------

*Improvements:*

- Include name of dataset in page title of dataset detail view.


*Bug fixes:*

- Rename "Import_log.xls" to "Validation_log.xls" when validating single datasets.



7.12.1
------

*Bug fixes:*

- Fix issue where new EN15804+A2 EF3.1 methods were not properly handled in sort and CSV export.



7.12.0
------

*Improvements:*

- Add support for "weight per piece" material property.


*Bug fixes:*

- Fix issue where temporary upload directory would not be created if not yet existing when uploading ZIP files via REST 
  API.



7.11.1
------

*Bug fixes:*

  - Fix issue where the export result for a category system in XLS formar with parameter `allLanguages=true` would only 
    contain one translation. Now all configured languages will be present in the result. 



7.11.0
------

*Improvements:*

- Prevent search engines and crawlers from crawling/indexing anything but the landing page (index.xhtml) via
  <meta name="robots" content="noindex, nofollow"/> element in all page headers.
- Cached downloads will now carry the name of the data stock as suffix.
- Cleanup cron job will now delete any unreferenced and thus outdated cached downloads that are older than 24 hours.
- Push now includes life cycle model datasets.


*Bug fixes:*

- Fix issue where push functionality would only include processes+dependencies, but not other dataset types.


*Docker:*

- Add option to include robots.txt in /opt/soda/robots.txt


*Documentation:*

- Update Installation Guide with info on MariaDB 10.6 which can be used as a drop-in replacement for MySQL 5.7.



7.10.3
------

*Bug fixes:*

- Manage data stocks: fix a further issue with manually reordering rows where
  in certain conditions an error would occur.



7.10.2
------

*Bug fixes:*

- Manage data stocks: fix an issue with manually reordering rows where changes
  would not be persisted.
- Fix an issue where error messages about an invalid augmentation configuration
  would be issued during startup, even though the feature is disabled.


*Documentation:*

- Add information on new users and developers mailing lists.



7.10.1
------

*Bug fixes:*

- Fix issues with distributed search via Registry.
- Fix an issue where when publishing a process dataset with dependencies, the
  latter would only become publicly visible after an instance restart.



7.10.0
------

*Bug fixes:*

- Validation: when validating, the dependency mode for the internal export that
  happens before validating has been changed to ALL_FROM_DATASTOCK (was:
  REFERENCE_FLOWS) so that links can be properly validated.
- Display of biogenic carbon content: honor currently active language.
- Manage processes: fix column "assigned to data stocks" in XLS export.


*Docker:*

- Update Tomcat base image to 9-jdk11-temurin.


*Documentation:*

- Update recommended version of Tomcat to 9. 



7.9.9
-----

*Bug fixes:*

- GLAD: adjust log levels back to INFO which mistakenly were still set to TRACE. 



7.9.8
-----

*Bug fixes:*

- GLAD: Fix an issue where the "free" property would be set to *false* for
  datasets with no declared license type. It now defaults to *true* if no
  license type is specified.



7.9.7
-----

*Bug fixes:*

- Fix an issue where the URL of the conversion service would not be correctly called.



7.9.6
-----

*Bug fixes:*

- Filtering for UUID: any extra spaces will be ignored when filtering for UUID using the "uuid:" prefix, e.g.
  "name: fc42aa" will now also work correctly.
- Fix regression for sort order issue for LCIA results with EF 3.1, EN15805+A2 LCIA methods.
- Localization: refine Chinese translation and use correct language code 'zh'.


*Documentation:*

- Add FAQ entry on CORS issue with POST requests behind nginx reverse proxies. 



7.9.5
-----

*Bug fixes:*

- User registration: include both links to password change and profile pages in both activation and password reset
  emails.



7.9.4
-----

*Bug fixes:*

- REST API: Fix issue in JSON extended where the reference flow would not be resolved using the exact version number
  from the reference. In case of no exact match, it will now resolve to the latest version of the flow, indicated by the
  new "resolvedFlowVersion" property.



7.9.3
-----

*Bug fixes:*

- CSV export (ILCD+EPD only): fix issue where the decimal separator would not
  be correctly set depending on the setting of a previous export.



7.9.2
-----

*Bug fixes:*

- CSV export (ILCD+EPD only): correct headers for indicator names for A2.



7.9.1
-----

*Bug fixes:*

- Fix UI issue in validation view where validation messages would not be 
  displayed properly. 



7.9.0
-----

*Improvements:*

- Add proxy support via `https_proxy` and `http_proxy` environment variables.
  See Installation Guide for details.
- The augmentation feature for information under registrationAuthority has been
  reworked from the ground up and is now also supported for distributed 
  queries. See Configuration Options Guide for details.
- Add support for including custom indicators in CSV export which can be
  configured using profiles. See Configuration Options Guide for details.
- Add Chinese localization, thanks to the efforts of the team at Tsinghua 
  University.


*Bug fixes:*

- Fix multiple issues with the Registry component which is now working 
  smoothly again.
- Fix sort order for LCIA results with EF 3.1, EN15805+A2 LCIA methods.
- CSV export (ILCD+EPD only): fix sort order for modules.
- CSV export (ILCD+EPD only): replace semicolons in scenario descriptions for
  consistent CSV structure.



7.8.1
-----

*Bug fixes:*

- Dataset detail page: fix CSS issue which would lead to a slightly distorted logo.



7.8.0
-----

*Improvements:*

- CSV export (ILCD+EPD) now also includes information on scenarios. Any cached
  CSV exports will be automatically invalidated upon upgrade.



7.7.0
-----

*Improvements:*

- CSV export (ILCD+EPD) now also includes information on biogenic carbon in
  product and packaging. Any cached CSV exports will be automatically 
  invalidated upon upgrade.



7.6.1
-----

*Bug fixes:*

- JSON extended view: fix for elementary flow categories
- The language chooser icon is now correctly styled according to the active
  theme.


*Documentation:*

- Correct Configuration guide: the correct name of the property for controlling
  the requiredness is `user.registration.additionalterms.x.requiredMessage` (it
  previously wrongly stated `requireMessage`).



7.6.0
-----

*Improvements:*

- REST API: JSON extended view now provides additional details for non-product 
  flows.  


*Bug fixes:*

- Process detail: fix carbon content being displayed as "ND" if it's actually 0.
- Process detail: gracefully handle null values in content declaration.
- Batch assign: fix issue where the dependency mode was not being honored.


*Documentation:*

- Fix wrong statement about token authentication, tokens are of course valid 
  until they expire and regardless of the number of tokens that is generated. 



7.5.6
-----

*Bug fixes:*

- Fix an issue where trying to reset the password for a user account that has
  not been activated yet would result in the user not being able to log in. 



7.5.5
-----

*Bug fixes:*

- GLAD registration: add EF3.1 to list of supported nomenclature systems, 
  properly check for compliance system UUIDs and prevent false detection of
  ILCD when EF declaration is present.



7.5.4
-----

*Bug fixes:*

- Process dataset detail: fix issue with (multi-language) display of name and 
  comment in the content declaration.



7.5.3
-----

*Bug fixes:*

- Fix issue in batch assign by spreadsheet upload where references to 
  non-existing datasets would lead to an error.



7.5.2
-----

*Bug fixes:*

- Invalidate all download caches to ensure downloads of entire data stocks
  are up-to-date after applying the 7.5.1 update.



7.5.1
-----

*Bug fixes:*

- Fix issue where root data stocks were not being marked as dirty when importing 
  new datasets, leading to outdated cached copies being served when downloading
  the entire data stock as ZIP file.
- Web UI: Scale embedded images in process and source datasets to 100% page 
  width.
- Fix encoding for links that are sent in registration and password reset emails
  where encoding was not being applied in the link text.



7.5.0
-----

*Improvements:*

- Accessibility improvements for dataset detail views including:
  - Hover class added to Back and Close buttons.
  - Tabindex adjustment for buttons.
  - Add language attribute.
  - Adjust page title.
  - Add ARIA labels to sections/accordion tabs.
  - Add alt text for logo and any flow chart images.


*Bug fixes:*

- Fix missing notification when a requested dataset is in another data stock 
  which is accessible to the current user.



7.4.5
-----

*Bug fixes:*

- Fix issue where group permissions were missing for a newly registered user
  after email address confirmation. Now auto-login has been removed and explicit
  login is required after account confirmation.


*Documentation:*

- Update Installation Guide with Java 11 system prerequisite.



7.4.4
-----

*Bug fixes:*

- Fix broken assign by XLS list upload feature.



7.4.3
-----

*Bug fixes:*

- REST API: fix issue #56 where assigning datasets to a data stock would not be
  persisted until instance restart.



- 7.4.2
-----

*Bug fixes:*

- GLAD: fix issue with updating registration of existing datasets.



7.4.1
-----

*Bug fixes:*

- Add time stamp to file name of ZIP/CSV files also for downloads via REST API.



7.4.0
-----

*Features/Improvements:*

- Add time stamp to file name of ZIP/CSV files when downloading entire data stocks.


*Bug fixes:*

- Fix missing captions of material properties in process and flow dataset detail views.



7.3.0
-----

*Features/Improvements:*

- REST API: add material properties information from flows to process JSON extended view.



7.2.2
-----

*Bug fixes:*

- REST API: fix end point `/processes/categories` which would produce a lot of
  duplicates after the 7.2.1 update.



7.2.1
-----

*Bug fixes:*

- Fix issue with import of external docs attached to source datasets inside a
  ZIP archive where external docs would not be imported correctly if only
  linked by their file name without the "../external_docs/" relative path 
  prefix.
- Fix issue where flow datasets that contain material properties would not be
  successfully imported.



7.2.0
-----

*Features/Improvements:*

- Manage datasets: add filter option to column "assigned to data stock".
- Offer registration link on metadata only when registration is activated.
- Rewrite batch delete task for improved performance and reliability. 
- Add new configuration option to set tags on datasets upon import.
- Increase log verbosity for dataset registration in GLAD.
- Performance and reliability improvements.


*Bug fixes:*

- Dataset list XLS export: fix issue that occurred when exporting large tables.



7.1.2
-----

*Bug fixes:*

- Fix import of source with binary attachment via `sources/withBinaries` REST end point - when sending the filename 
  of the attachment as something starting with "../external_docs/", still properly import the binary attachment.



7.1.1
-----

*General:*

- Update Spring to 5.3.18 to address SpringShell RCE vulnerability (CVE-2022-22965).



7.1.0
-----

*Features/Improvements:*

- REST API: Add option to configure data stocks which will automatically be 
  assigned READ and EXPORT permissions when authenticating with JWT tokens.


*Docker:*

- Update Tomcat base image to 8.5.78 (8.5.78-jdk11-temurin-focal).
- Remove access logging from container's logging.



7.0.0
-----

*General:*

- ***This release requires a Java 11 runtime or later and will not run on previous Java versions.***
- It is not possible to downgrade to an earlier release after upgrading to 7.0.0. Make sure you
  backup your database before upgrading.


*Features/Improvements:*

- Add OpenID Connect (OAuth 2.0) support. 
- REST API: Add Single Sign-On (SSO) support for sharing access tokens across a federated network of nodes. 


*Documentation:*

- Add guides for Docker setups and OpenID Connect (OIDC) configuration.



6.12.9
------

*Docker:*

- Update Tomcat base image to 8.5.78 (8.5.78-jdk8-temurin).



6.12.8
------

*Bug fixes:*

- Fix issue with registration which was introduced by the fix from 6.12.7.



6.12.7
------

*Bug fixes:*

- Fix infinite loop upon login when accept privacy policy is enforced.
- Fix issue where flow reference could not be resolved up correctly.
- Process dataset detail: fix missing module headings in results tables.



6.12.6
------

*Bug fixes:*

- Fix an issue where binary attachments of source datasets (images and documents) would not be correctly imported.



6.12.5
------

*Bug fixes:*

- Add some missing database indexes to speed up various operations.



6.12.4
------

*Bug fixes:*

- Fix issue that would prevent new dataset registrations in GLAD.
- Fix appearance of public process list's compliance column for nova themes.



6.12.3
------

*Bug fixes:*

- CSV export: fix issue where values for the EP-freshwater indicator would be wrong
  (the same value as the one for EP-marine would be written to the EP-freshwater
  column).
- Missing translations encountered during CSV export are now handled more gracefully.



6.12.2
------

*Bug fixes:*

- Dataset list XLS export: fix issue that occurred when exporting large tables.
- CSV export: fix issue where the multiplication of the amount of the reference exchange with the amount inside
  the reference flow was missing.



6.12.1
------

*Bug fixes:*

- User registration form: fix an issue where the title for additional terms wouldn't be shown.
- User registration: default `user.registration.spam.protection` config property to false to prevent it causing an
  error if no captcha is configured.



6.12.0
------

*Features/Improvements:*

- Multilanguage UI: add a few more languages and adjust the way to switch the
  active language. For contributing corrections or additional languages, join
  here: https://poeditor.com/join/project?hash=7zjzkVYx4f

*Bug fixes:*

- User registration form: fix a display issue in the  that would not be wide
  enough in some languages.
- User registration form: remove a stray string that didn't belong there.



6.11.0
------

*Features/Improvements:*

- REST API: add new configuration option to add reference products in list 
  output of `/processes` end point.
- Performance improvements for operations involving lookup of reference flows.


*Bug fixes:*

- Distributed search: add null checks for less bloated log output.
- Fix display of scenario names for EPDs.


*Other:*

- Misc. dependency upgrades and security fixes, among others upgrade log4j to
  the latest 2.17.1 release.



6.10.0
------

*Features:*

- REST API: add `compliance` and `complianceMode` parameters to
  `/processes/categories` end point.
- REST API: add `lang` and `langFallback` parameters to
  `/processes/registrationAuthorities` end point.


*Bug fixes:*

- REST API: fix issue with missing data sources in process lists. 



6.9.1
-----

*Bug fixes:*

- REST API: Avoid duplicates on `/processes/registrationAuthorities` in 
  augmentation mode and add support for distributed queries.



6.9.0
-----

*Features:*

- REST API: Add configuration options to augment missing registration 
  authority entries in process list view. 
- REST API: Add missing ordering for registration authority and registration
  number.
- REST API: Add new view mode `extended` for process datasets in JSON format


*Bug fixes:*

- Flow overview: The links for "Processes with this flow as input / output"
  on the flow overview page were previously dynamically enabled/disabled based
  on there actually being any. The query to determine that could be slow on
  large instances, hence this behaviour has been disabled by default so that
  the links are always shown, which is the new default. A new config option 
  `display.flows.overview.in-out-flows.links.preload` has been added to 
  re-enable that if desired.
- Fix null pointer exception for dimensionless units.



6.8.2
-----

*This release solely contains a feature and fixes backported from later
releases in order to be able to run on setups relying on otherwise unsupported
outdated Tomcat 8.0 containers.*

*Features:*

- REST API: add `compliance` and `complianceMode` parameters to
  `/processes/categories` end point.


*Bug fixes:*

- REST API: fix issue with missing data sources in process lists.
- REST API: Add missing ordering for registration authority and registration
  number.
  


6.8.1
-----

*Bug fixes:*

- Honor datastock permissions when displaying notice about and link to the
  superseding process (omit both if superseding process is not visible to the
  user).
- Do not show a superseding dataset if it would point to the same UUID.
- Add configuration option to override default behavior of honoring datastock
  permission when displaying notice about and link to the superseding process.



6.8.0
-----

*Features:*

- REST API: Add new GET parameter for including all available translations in 
  category systems XLS export.  
- Add support for upload and download of ILCD datasets in XLS format with 
  transparent conversion (using an external conversion service).


*Bug fixes:*

- Process detail view: fix broken link to superseding dataset.
- Process detail view: fix tooltip for reference to preceding dataset version.
- Manage views: Fix and improve formatting of multi-value cells when exporting
  dataset lists as XLS.
- User registration/management: Email addresses may now contain all characters
  as specified in the relevant RFC.
- REST API: Return distinct values on `/processes/registrationAuthorities` end
  point.


*Documentation:*

- Update GLAD Guide.



6.7.4
-----

*Bug fixes:*

- Fix an issue where the manage life cycle models view could not be shown due
  to an unimplemented method.



6.7.3
-----

*Bug fixes:*

- Handle absent information in life cycle model datasets more gracefully,
  preventing error messages when displaying the dataset.



6.7.2
-----

*Bug fixes:*

- Add support for sending mails via TLS 1.2 and above, as TLS 1.0 and 1.1 are 
  no longer considered secure and have been disabled by default in recent Java
  runtimes. If sending emails doesn't work anymore on your instance, you're 
  likely affected by this issue.



6.7.1
-----

*Bug fixes:*

- Restore filtering in non-admin dataset list views.
- Restore default sorting in "Manage users" view and add new option to sort
  by order of creation.
- REST API: Fix issue where Elementary flows were missing when calling the
  `/flows` end point.
- Fix an issue where multiple save attempts in the "Edit data stock" view would
  cause an error.
- Fix an issue where deleting datasets with dependencies would not work
  correctly.



6.7.0
-----

*Features:*

- Add configuration option to remove "Close" button from dataset detail views.


*Bug fixes:*

- Fix broken filtering in manage users view.
- Add missing i18n string for tag visibility in manage tags view.


*Documentation:*

- Add more comprehensive documentation for setup in IntelliJ IDEA Ultimate.



6.6.0
-----

*Features:*

- When trying to access a protected resource, nicely ask the user to login and
  then forward them directly to the requested resource.


*Bug fixes:*

- Add missing dependencies when exporting as ZIP/assigning/pushing process 
  datasets.



6.5.0
-----

*Features:*

- The "Manage Users" view now supports sorting and filtering by arbitrary
  columns. 


*Bug fixes:*

- Fix an issue where when trying to delete a selection of datasets, deletion 
  for half of the selection would succeed and the other half would fail.
- Fix an issue where the correct permissions would not be assigned after a
  forced password change.
- Fix an issue where the email body of emails sent by the application would
  not be correctly encoded.



6.4.3
-----

*Bug fixes:*

- Fix an issue where the encoding would not be set in emails sent from the
  application, resulting in some special characters being garbled.



6.4.2
-----

*Bug fixes:*

- Update Mail API dependency to fix an issue with TLSv1.2 connections to SMTP
  servers.

*Documentation:*

- Add documentation of `mail.starttls` configuration property which was missing
  previously.
- Add section to Installation Guide and FAQ about migrating instances from 
  Windows to *nix OSs. 
- Add missing query parameters for Process queries in Service API doc.



6.4.1
-----

*Bug fixes:*

- Resolve an issue which would cause database migration to fail when upgrading
  to 6.4.0 in some environments. This will also re-add a trigger and stored
  procedure in case they have not been exported when dumping the database,
  which by default they're not. Always include the `--routines` switch when
  backing up a database using the `mysqldump` utility in order to make sure 
  these are included. 
  **Please also make sure you're upgrading to the latest v5.x version of 
  MySQL/J Connector [(download MySQL/J Connector 5.1.49 here)](https://downloads.mysql.com/archives/get/p/3/file/mysql-connector-java-5.1.49.zip).**



6.4.0
-----

*Features:*

- Themes: add support for new PrimeFaces built-in themes - choose from
  - luna-green
  - luna-amber
  - luna-blue
  - luna-pink
  - nova-colored
  - nova-dark
  - nova-light
  - omega

  The legacy themes are still supported (but may soon be deprecated).

- Add configuration option to bypass database validation.


*Bug fixes:*

- Fix an issue where exchange amounts and LCIA results in process datasets and
  characterization factors in LCIA method datasets were restricted to six
  digit precision in the dataset detail views. All values for process and LCIA method datasets will be re-read on
  upgrade, causing the migration to take some time. For large databases, expect
  migration times of 30 minutes or more (depending on your hardware and system
  load).
- Fix NullPointerException when retrieving source datasets in JSON format that
  are missing category information.



6.3.1
-----

*Bug fixes:*

- Validation: fix resolution issue with composite profiles.
- REST API/manage processes: fix compliance filtering for OR conjunction.



6.3.0
-----

*Features:*

- Manage processes: add filtering for compliance.
- Manage processes: add column chooser.
- CSV export: add additional columns.


*Bug fixes:*

- REST API: fix compliance filtering for AND conjunction.
- REST API: return HTTP 404 if specified data stock is not found in import.
- UI/user registration: remove empty and meaningless panel.
- UI: fix additional encoding issues in dataset detail captions.



6.2.1
-----

*Bug fixes:*

- Fix an issue where queries were not built correctly when specifying multiple
  arguments for the same criterion, resulting in incorrect results and long
  execution times.
- Fix an issue where Shiro would block requests with non-ASCII characters in 
  the URL path, leading to categories with special characters not working
  anymore (see https://issues.apache.org/jira/browse/SHIRO-801).



6.2.0
-----

*Features:*

- GLAD: allow to execute dataset registration/deregistration with GLAD as 
  background job.
- GLAD: improved support for GLAD's rate limiting.
- Add a visibility flag for tags.
- Process dataset detail: introduce configuration option to hide more min/max 
  value columns.   
- Add more Spanish translations.
- Add more German translations (dataset detail tooltips).


*Bug fixes:*

- Process dataset detail: enable display of location column in exchanges
  section by default.
- Process dataset detail: hide display of some fields when there is nothing to
  display.
- Fix encoding issue with tooltips in dataset detail.
- Distributed search: fix issue where NPE would be thrown when remote nodes are
  not responsive.



*Documentation:*

- Improve documentation by fixing broken links and clarifying doc for POST
  operation with source dataset containing binary attachments.


6.1.0
-----

*Features:*

- Manage data: batch assign by XLS now allows assigning the newest available
  item for each row by not specifying a version number in the XLS document.
- REST API: add support for filtering by metaDataOnly attribute.


*Bug fixes:*

- Manage data: fix issue with batch assign by XLS where an upload would not 
  work correctly in Firefox browsers.  
- Life cycle model visualization: correct hyperlinks to process datasets.
- Distributed search: fix exception when metaDataOnly attribute is absent.
- External files on source dataset: fix an issue where documents with special
  characters such as % in their file name could not be viewed.
- External files on source dataset: fix an issue where flowcharts would not be
  rendered inline anymore in the process dataset detail.



6.0.0
-----

*Features:*

- Add full support for the new life cycle model dataset type (a.k.a. eILCD).
- Add visualization for life cycle models (experimental).
- Add support for exporting process datasets as XLSX (using an external 
  conversion service).
- Add support for EPD extensions v1.2.
- REST API: Add support for tagging individual process datasets.
- Permissions: Add new role TAG for allowing setting and deleting tags.  


*Bug fixes:*

- Adjust width of dataset detail view for process and life cycle model for 
  better readability. 
- Fix issues with batch assign by type/by XLSX.



5.9.3
-----

*Features:*

- Validation: add positive validation mode.
- REST API: add support for metaDataOnly attribute.
- Distributed search cache: make TTL configurable (default: 1 hour).


*Bug fixes:*

- Validation: fix issue where validation profiles would not be registering 
  after an instance restart.
- Categories: fix issue where flow categories would be duplicated after
  editing categories.  


*Docker image:*

- Upgrade Tomcat to 8.5.61.



5.9.2
-----

*Bug fixes:*

- Manage data: fix issue with batch assignment when assigning/detaching with 
  dependencies where the dependency mode would only be applied to the first
  100 items.
- Validation: fix an issue where the application would not start after trying
  to register an invalid validation profile
- Docker image: remove invalid AJP connector configuration from server.xml 
  template



5.9.1
-----

*Bug fixes:*

- Export: fix an issue where elementary flows would not be exported during 
  export of a logical data stock. 



5.9.0
-----

*Features:*

- REST API: add list of datasources to process list view.
- REST API: add end point for requesting the actual PDF document for an EPD 
  (ILCD+EPD only).


*Bug fixes:*

- Fix issue with subtitle where the main page would not load when subtitle was
  set and the application was running in the root context.
- Improve detection of external URLs in sources and prevent them from being
  stored as external files.
- Fix issue where export would fail if a source contains an URL instead of an
  external file that had been stored on the file system due to the issue above. 
- Fix issue where an error would occur while traversing transitive dependencies 
  during export and assign. 
- Fix issue where importing a malformed XLS file would cause an error during
  assign to/detach from data stock by XLS.



5.8.1
-----

*Bug fixes:*

- REST API, search: fix an issue with filtering processes by compliance when 
  combining with another filter criterium.



5.8.0
-----

*Features:*

- Add a warning to the dataset detail and overview views that indicate that a
  newer version of this dataset exists or if a superseded dataset exists.
- Add a new feature to clean all caches manually.  
- Add a new feature that allows regular automatic removal of outdated upload
  and export ZIPs. See the configuration guide for details.
  


*Bug fixes:*

- Process dataset detail view: fix the caption of "Model description".
- Process dataset detail view: fix broken exchanges sorting.
- Make sure external files are included in an export even if they have special
  characters in their file name (which they shouldn't).
- Assign/remove to/from data stock by XLS list: gracefully handle malformed XLS
  lists by checking for empty rows.
- Jobs list: fix the order in which entries would be displayed when filling 
  multiple pages.
- Eliminate AnnotationScanner warnings/errors on console startup.
- Fix duplicate flows in ZIP export in LATEST_ONLY mode.


*Documentation:*

- Add documentation for new assign/remove to/from data stock by XLS list 
  feature.



5.7.0
-----

*Features:*

- Manage data stocks: add new option to assign/remove datasets from/to a data
  stock based on an uploaded XLS list with respective dataset type, UUIDs and
  version numbers



5.6.0
-----

*Features:*

- Add new option to export a process dataset including all its dependencies as
  a ZIP file both via UI and REST API


*Bug fixes:*

- Fix issue breaking validation that was introduced in 5.5.0
- Fix a memory issue with ZIP export 



5.5.0
-----

*Features:*

- Add a language switch to the user interface
- Admin: add a new option to trigger a re-read of the application configuration
  from the soda4LCA.properties file from the admin menu, without need to 
  restart the instance
- REST API: add "name" as secondary sort criterium when retrieving lists of 
  datasets 
- REST API: entries under "compliance" will now be alphabetically sorted 
- Add default sort order for EF3.0 LCIA indicators in custom sort mode
  (`display.sort.indicators = custom` setting)


*Bug fixes:*

- Fix an issue where the migration to database schema version 15.1 would fail
  if some LCIA method dataset were referenced but missing
- Fix an issue where LCIA results would not be displayed
- Manage datasets: fix scrambled column headers in Excel exports 
- Manage datasets: fix missing contents in Excel exports
- Manage data stocks: fix issue with persisting data stock name when edited 
  after initial creation
- Fix an issue where a change of name of a data stock would not be persisted
- REST API: fix an issue where langFallback would not be applied to JSON output
- Fix issue where exchanges would reference wrong flow versions 


*Misc:*

- Speed up export of data stocks as ZIP archives
- The [Docker image](https://hub.docker.com/r/okworx/soda4lca) has been 
  adjusted to host configuration and data files in a dedicated location and is
  now ready for use in production. See the Docker section in the Installation Guide 
  for more details.


*Documentation:*

- Add instructions for deployments using the soda4LCA Docker image   



5.4.1
-----

*Bug fixes:*

- GLAD: add workaround to make the verification of the GLAD URL more robust 
  after unexpected breaking change on GLAD's API 



5.4.0
-----

*Features:*

- Nodes networking: add option to restrict data from remote nodes to a specific
  data stock


*Bug fixes:*

- Nodes networking: optimize parallel queries to remote nodes
- Nodes networking: fix multilanguage issues when combining data from remote
  nodes


*Misc:*

- the application can now be run on Java 11+
- add support for Gitpod - check out the new [Gitpod Guide](https://bitbucket.org/okusche/soda4lca/src/5.4.0/Doc/src/Developer_Guide/Gitpod_Guide.md)
- update developer docs
- misc. dependency upgrades



5.3.1
-----

*Bug fixes:*

- REST API: fix issue where the dataset owner would not show up in the response
  if it is in any other language than English
- Nodes networking: fix an issue where deduplicating results would not occur when
  retrieving results from cache


*Misc:*

- Nodes networking: the default timeout for nodes has been increased to 3 seconds  
- Nodes networking: optimize deduplication in proxy mode



5.3.0
-----

*New features:*

- Manage process datasets: filtering by process dataset type is now possible
  in manage and assign to/detach from data stock views
- Search, filtering, REST API: multiple search terms for name field can be 
  given (separated by spaces) which will be combined using AND   



5.2.1
-----

*Documentation:*

- Update documentation with more precise description of system prerequisites.
  The application currently required Java 8, support for later Java versions 
  will be added in a subsequent release.



5.2.0
-----

*New features:*

- Manage data stocks: add new feature to mass assign/remove datasets from data 
  stocks. For large assignments, the operation can be launched as a job from
  the existing UI. 
- Validation: upgrade validation facility to support the new generation of 
  validation profiles with composition features  


*Bug fixes:*

- process dataset detail: fix display of reference flow property and amount in
  cases where the flow dataset has been imported only after the process dataset 



5.1.1
-----

*Bug fixes:*

- GLAD: supportedNomenclatures are now correctly detected (ILCD, EF1.0-4.0), replace special chars in strings
- GLAD: licensetype "free" now includes all types except "license fee" 
- GLAD: hide GLAD button in admin backend if GLAD is disabled
- process dataset detail: add link to landingpage under logo



5.1.0
-----

*New features:*

- add feature for tags on data stocks
- process dataset detail: add new option for customizable background logo 
- process dataset detail: add new option to show functional unit in inventory and results section headers 
- process dataset detail: add new option to configure active sections 
- process dataset detail: add new options to configure display of columns in inventory and results section headers 
- process dataset detail: add new options for sorting inventory and results 
- process dataset detail: display units for LCIA results
- process dataset detail: columns can be draggable and resizeable
- dataset detail: improve display of title and other cosmetic improvements
- validation: display validation profile descripton 
- validation: reference objects will be ignored by default
- REST API: add datasetGenerator query parameter
- REST API: add support for dataset overview as JSON
- add support for originatingProcess links
- enforce password complexity
- enforce password change after registration or password reset
- add single CSV export option (ILCD+EPD only)
- add new option to allow CSV export only for admins (ILCD+EPD only)
- add LCI indicators to CSV export (ILCD+EPD only)
- manage datasets: hide previous dataset versions by default
- manage datasets: add tooltips to column headers
- improve Docker support support by allowing to generate test and production Docker images (experimental)
- LCDN/EF: change URL schemes of already registered registries to HTTPS


*Bug fixes:*

- fix issue where exchanges would reference wrong flow versions 
- validation: fix behavior of "Validate" button 
- misc. performance improvements
- restore remember me functionality across application restarts
- import: don't complain if @uri attribute is missing
- import: don't throw an error when importing a source dataset containing an external URL
- import: do not throw error when importing duplicate flows
- REST API: suppress empty duplicates elements
- update QA timestamps only if QQA is enabled
- consider all process name components for GLAD registration
- remove disabled delete button from assign view
- correct display of reference flow amount
- manage datasets: fix reset filter bug
- disable GLAD menu item in global configuration when GLAD feature is not enabled



5.0.3
-----

*Bug fixes:*

- Validation: fix issue where registration of older validaton profiles would fail  



5.0.2
-----

*New features:*

- A Docker image with two testing instances will be built upon release at 
  docker.io/okworx/soda4lca-test (experimental, do not use for production). 
  Thanks to Michael Srocka for the original Docker file.  


*Bug fixes:*

- REST API: fix issue where import of entire ZIP archive would fail  



5.0.1
-----

*Bug fixes:*

- additional fix related to the issue where datasets would be missing from an exported datastock if a newer version of the dataset was contained anywhere in the database



5.0.0
-----

*New features:*

- REST API: add feature to export only products
- REST API: add landing page service
- REST API: add exact matchmode for validUntil parameter
- REST API: add functionality to assign datasets to stocks via REST API
- REST API: add support for token based authentication
- REST API: add locationofsupply to flow
- REST: add support for retrieving data sources
- GLAD: add support for database level parameters 
- GLAD: add extended metadata support for dataset level parameters 
- add support for EC number
- add export modes: ALL (all datasets, including any previous versions), LATEST_ONLY (the latest version of all processes, and everything else including any previous versions of non-process datasets), LATEST_ONLY_GLOBAL (only the latest versions of all datasets)
- Added column 'owner' (sortable and filterable) to manage process view.
- add support for validation presets (experimental)


*Bug fixes:*

- fix issue where datasets would be missing from an exported datastock if a newer version of the dataset was contained anywhere in the database
- fix NullPointer issue with flow references
- add "*" to "accept terms" checkbox on registration page when accepttermsrequire=true
- add null checks during process import
- fix headings for "edit user profile" and "change password" pages
- mark stocks as dirty also when deleting a dataset
- REST API: fix issue where incorrect product flows would be returned
- REST: fix JSON serialization for other dataset types than process
- fix referenceYear, validUntil filtering & sorting
- import: don't set version number to 00.00.000 if none is specified
- misc. bug fixes and performance optimizations



4.4.6
-----

*Bug fixes:*

- fix registration issue when text in "dataset purpose" field is too long



4.4.5
-----

*Enhancements:*

- performance improvements for bulk registration of datasets



4.4.4
-----

*Enhancements:*

- sort users in alphabetical order in access permission views


*Bug fixes:*

- restore missing "anonymous" user in access permission views
- fix #31: UI freeze when trying to remove user's permissions



4.4.3
-----

*Bug fixes:*

- offer public link for downloading entire data stock only if proper `EXPORT`
  permission is set on the data stock 



4.4.2
-----

*Bug fixes:*

- enable assigning any users to a group with no organization



4.4.5
-----

*Enhancements:*

- performance improvements for bulk registration of datasets



4.4.4
-----

*Enhancements:*

- sort users in alphabetical order in access permission views


*Bug fixes:*

- restore missing "anonymous" user in access permission views
- fix #31: UI freeze when trying to remove user's permissions



4.4.3
-----

*Bug fixes:*

- offer public link for downloading entire data stock only if proper `EXPORT`
  permission is set on the data stock 



4.4.2
-----

*Bug fixes:*

- enable assigning any users to a group with no organization



4.4.1
-----

*Bug fixes:*

- re-enable missing captcha for user registration



4.4.0
-----

*Enhancements:*

- add option to define custom reports on the data in the instance as either 
  native SQL query or call to a REST API URL (see Configuration Options Guide)
- add possibility for non-privileged users to reset a lost password or retrieve
  their username


*Bug fixes:*

- fix very short session timeout (now set to 60 minutes)
- fix #50: process filtering by name not working
- misc. typo fixes in German i18n
- small cosmetic improvements



4.3.1
-----

*Bug fixes:*

- fix an issue where a link to an external privacy policy would cause an error
  under certain circumstances
- fix broken link behind application title for setups with a context path
- fix issue where confirmation of dataset push dialog would not accept return
  key as "OK" 



4.3.0
-----

*Enhancements:*

- add configurable privacy statement and imprint pages/links
- add configurable option to require registering users to accept the privacy 
  statement
- add configurable option to require registering users to specify one or more 
  industrial sectors
- add configurable option to require registering users to specify their name
- add multilanguage capabilities to static includes for welcome, imprint and 
  privacy statement pages
- add configuration option for SSL-only setups
- GLAD: add option to globally  define a name for the field "data provider" 


*Bug fixes:*

- fix textual error in user activation mail template


*Documentation:*

- add "GDPR Compliance Guide" with instructions for configuring an instance to
  comply with the GDPR (located inside the Installation Guide folder)



4.2.1
-----

*Bug fixes:*

- fix issue where user activation key would not be recognized correctly
  when it has been URLencoded
  


4.2.0
-----

*Enhancements:*

- add feature for pushing datasets to another node (see configuration options)
- add feature to register datasets in GLAD search index (beta)
- REST API: add dataset HEAD request
- REST API: extend XLS categories export with statistics, metadata
- REST API: retrieve list of used languages, also per stock
- REST API: enable multiple lang arguments (OR)
- admin/manage: add option to clear all filters
- admin/validation: support for link validation when validating individual datasets
- enhance CSV export for ILCD+EPD datasets
- user management: add gender option "other"  


*Bug fixes:*

- Remove ugly stacktrace upon write errors for key files
- fix CSV export in manage data stocks view
- REST API: fix issue where imports of source datasets with binary attachments would
  end up in a wrong data stock
- fix communications issue between Node and Registry components


*Documentation:*

- Installation Guide: add connection string example for MySQL 5.7+



4.1.0
-----

*Enhancements:*

- add custom logo in dataset detail views (see configuration options) 
- enable high resolution logos (see configuration options)
- process search: add compliance search option, compliance in search results
- process search: add sorting for search results
- REST API: add compliance filtering & sorting
- REST API: improved JSON output
- user registration: add options for agreement to additional terms (see 
  configuration options)
- dataset registration: add entire data stock registration 
- adjust encoding of CSV export to ISO8859-1 for enhanced compatibility with 
  commonly used spreadsheet applications
- cosmetic improvements


*Bug fixes:*

- fix communication issues (wake and registration) between Node and Registry
  components
- fix distributed search issue when using JSON  
- REST API: fix incorrect URIs for distributed search results
- REST API: fix null datastock id for source w/binaries POST


*Documentation:*

- add documentation for permissions management on data stocks 
- fix documentation of *user.registration​.defaultGroup* configuration option
  (correct wrong case) 



4.0.2
-----

*Documentation:*

- add note about Java 1.7 requirement to release notes 



4.0.1
-----

*Bug fixes:*

- restore missing custom html on index page feature from 3.x



4.0.0
-----

**Note: this release requires Java 1.7 or later.**
**See the Installation Guide for a full list of system requirements.**

*Enhancements:*

- dataset lists views: add filtering by UUID in Name column (using keyword
  "uuid:")
- dataset lists views: improved display of number of filtered items/total items
- admin/manage: introduce options to perform assign/remove to/from stock, 
  delete, move and validate operations on datasets including their dependencies
  (linked secondary datasets) 
- admin/manage: add function to move datasets between root data stocks
- admin/users: add XLS export of list of users
- admin/validation: add sorting by type, message in validation results
- admin/validation: add links to associated processes for flow validation messages
- add auto-login/"remember me" feature
- redirect to requested page after login (allows for bookmarking admin pages)
- add CSV export option for ILCD+EPD datasets
- REST API: add more search criteria for process queries
- REST API: add sorting for process queries
- REST API: improve and extend JSON support


*Bug fixes:*

- admin/validation: clear events list before performing another validation
- admin/validation: fix issue with downloading validation results
- admin/manage: fix import date filtering issue in dataset lists views where 
  some date ranges would be misinterpreted
- admin/manage: fix issue #46 (prevent hidden datastocks from being selected by default) 
- REST API - contacts list: fix issue where the *name* value would be
  wrongly overwritten with the *shortName* value
- REST API - categories: fix issue where the list of categories/subcategories
  returned would be missing items that are present on newer datasets in other
  data stocks 
- admin/QA: fix issue with "unknown error" in QA module when datasets do not declare a
  classification
- admin/import: fix issue where files would be overwritten when importing multiple ZIP
  archives with the same file name
- fix precision issue for CSV export


*Documentation:*

- add section about organizing data using logical and root data stocks to Admin
  Guide
- document Process query parameters in REST API docs


*Component updates:*

- update PrimeFaces to 6.1
- upgrade Selenium to 3.4



3.2.6
-----

*Bug fixes:*

- fix issue where assigning datasets would show strange behavior with multiple
  logical data stocks
- fix issue where the captcha section would not be completely hidden when 
  disabled
- remove misplaced Organisation selector from new user registration page 


*Enhancements:*

- open terms of use in new browser window
- improve registration messages



3.2.5
-----

*Enhancements:*

- allow for including custom HTML content on index page
- enable absolute URLs for welcome page (jump page)



3.2.4
-----

*Bug fixes:*

- fix issue where navigating along inputs and outputs of processes was not 
  working correctly
- fix issue with dataset filtering



3.2.3
-----

*Bug fixes:*

- fix issue where when exporting only the latest versions of datasets which
  is the default, any other versions would still be fetched from the database
  (yet usually overwritten and thus not end up in the ZIP file)



3.2.2
-----

*Enhancements:*

- add filtering for name and other properties in public dataset lists
- gracefully reroute request if a requested dataset is not in the currently 
  selected data stock, but still accessible


*Bug fixes:*

- add missing display of units to exchanges table of process dataset
- add missing display of flow information on exchanges where the flow has been
  imported after the process 



3.2.1
-----

*Enhancements:*

- add "accept license terms" and additional configuration options for user
  registration (see the list of configuration options in the Installation Guide
  for details)
- remove hard-coded captcha key declarations in web.xml (see Installation 
  Guide for details how to set those globally)
- remove restriction for custom themes



3.2.0
-----

*Enhancements:*

- add default group for registered users
- add support for storage and display of numeric DQI values
- store location of LCIA method's CF in relational model
- allow preformatted contents (i.e. show line breaks) in process metadata
- add filtering by subtype and import date to backend
- add application subtitle
- docs: add instructions on how to disable MySQL ONLY_FULL_GROUP_BY mode
- docs: update Installation Guide, Developer Guide
- soda4LCA.properties.template: add properties for email authentication


*Bug fixes:*

- fix issue where source with lower version # would not be updated
- display the correct file associated with each dataset version
- fix issues with P2P networking
- fix display issue for LCIA method dataset detail
- add classification column to CFs table in LCIAMethod view



3.1.0
-----

*Enhancements:*

- add validation feature. Imported datasets and individual data stocks can be
  checked against validation profiles compliant with the ILCD Validation
  library. Developers see the Developer Guide for a note on dependencies. 
- administration backend: allow persistent sorting of data stocks simply by 
  dragging and dropping rows
- administration backend: allow hiding and unhiding of data stocks (default
  behavior - whether to show or hide hidden data stocks - can be controlled
  by a property in soda4LCA.properties - see Installation Guide for details)
- administration backend: add support for larger numbers of datasets in data
  tables
- administration backend: improve export of dataset tables (add stock info, auto column width)
- REST interface: add langFallback GET parameter and adjust language filtering
  behavior
- REST interface: add countOnly GET parameter to datasets query
- REST interface: add JSON support (experimental) 
- add configurable sort feature for exchanges and LCIA results
- add Spanish translations (experimental, some are still missing)
- upgrade to PrimeFaces 6.0


*Bug fixes:*

- return most recent accessible version of dataset via REST API when no
  proper permissions are present for the actually most recent one
- fix issue that would lead to a failure in ROOT context
- fix possible NPE when certain data is not present on an LCIAResult
- fix possible NPE when listing flowproperties where a unit group dataset is
  missing



3.0.6  
-----
January 18, 2017

*Bug fixes:*

- fix an outdated dependency that might cause trouble in some setups 
- fix snapshot dependency for shiro-faces



3.0.5
-----
June 22, 2016

*Enhancements:*

- add XLS export of data tables in backend
- fine-tune column widths for data table in manage views
- improve captions for data stocks (root vs. logical)
- update installation instructions
- make dataset detail page more printer friendly


*Bug fixes:*

- issue #30: improve review display in dataset detail
- issue #37: restrict list of prior versions to accessible ones
- issue #26: automatically inform registered user about activation
- fix bug in css link in dataset detail template
- fix miscellaneous issues with user registration and css
- fix issue with displaying registered datasets



3.0.4
-----
May 19, 2016

*Enhancements:*

- add more filtering options in manage datasets views
- improve captions in manage datasets views ("most recent versions only" is now more precisely "hide older versions")
- improve datastock captions (ID and name instead of name and title)
- remove "contained" column from assignment dialogs (it was always empty)
- issue #24: add user interface for data stock download (configurable link for downloading currently selected data stock)
- add language switch in dataset detail view


*Bug fixes:*

- fix issues with sort order in manage datasets views
- fix exceptions that could occur with automatic category translation
- fix issue to prevent login for non-activated users and typo (thanks to Vilmantas Baranauskas/thinkstep)
- fix NPE in initialization of ActionHandler: Injected field 'i18n' is not available within constructor (thanks to Vilmantas Baranauskas/thinkstep)



3.0.3   
-----
April 25, 2016   (DB schema version 2.4) 

*Enhancements:*

- add category translation feature


*Bug fixes:*

- fix regression caused by PrimeFaces 5.3 upgrade 
- issue #16: list per-datastock permissions for /authenticate/status



3.0.2
-----
February 22, 2016

*Bug fixes:*

- re-add missing soda4LCA.properties template
- improve documentation appearance and generation
- fix registration/synchronization intervals



3.0.1   
-----
February 22, 2016

*Improvements:*

- export of datastocks as a (cached) ZIP file is now possible for 
  non-authenticated/non-admin users via the Service API
- issue #11: convert documentation to markdown
- issue #10: upgrade PrimeFaces to 5.3
- hide p2p mode entries from network menu when running in registries mode
- add general developer guide

*Bug fixes:*

- fix issue with listing registered processes
- fix i18n display issue
- fix display issue when running in root context





3.0.0   
-----
February 04, 2016

PLEASE NOTE: Prior to using this release (when upgrading from 2.x or earlier),
it is necessary to backup your data (using the export feature), manually empty
the database schema (e.g. drop and re-create) and re-import the data via the
application afterwards.

Please see the Installation Guide for detailed instructions on how to upgrade
your soda4LCA installation.

*Improvements:*

- new and improved dataset detail views (JSF based)
- improved data management for administrators
- improved data import
- improved data export & export performance  
- Service API supports navigation of categories, selection of language, return
  only most recent version of a dataset
- quantitative comparison of datasets
- support for EPD datasets (modelled as process dataset)
- add display of timestamp to process dataset overview
- allow underscores in data stock names
- separate handling of product and elementary flows for more efficient data
  management
- REST API supports adding source data sets including their binary digital 
  files
- during dataset import, a timestamp is recorded
- issue #T115: login link is now configurable
- issue #T126: columns to be displayed are now configurable
- issue #T123: add timeout to remote search
- issue #T129: nodes in distributed search result are now hyperlinked
- issue #T139: password change now requires the current password to be entered
- issue #T177: data export optionally covers all versions
- issue #T240: improve Selenium functional tests to run with an embedded
  container from Maven
- issue T#241: extend Selenium functional tests to include Registry application


*Bug fixes:*

- issue #T230: fix P2P networking mode
- issue #T232: Change group's organisation doesn't work in one step
- issue #T243: links to external files in source dataset detail defunct
- issue #T238: fix incorrect ZIP structure when exporting
- issue #T234: Dataset detail view - contact: display logo if available
- issue #9: datastock permissions would not work under certain conditions
- issue #2: dataset link does not work in data stock assign dialog
- issue #7: REST-API: wrong sorting of paged results
- issue #3: dataset QA (compare): versions missing in navigation
- issue #1: Deleting user leaves orphaned data stock permission entries
- issue #5: exporting data with many binary attachments takes very long



2.0.1 
-----
June 13, 2014

*Bug fixes:*

- update JAXB library that would cause a memory leak



2.0.0
-----
November 5, 2013

Please see the Installation Guide for detailed instructions on how to upgrade your soda4LCA installation.

You may upgrade to this release from the previous beta release without erasing the database, however, any LCIA method 
datasets need to be deleted and re-imported after the upgrade.


*Bugfixes:*

- fix issue where display of process datasets would not work under certain circumstances
- fix issue with import of LCIA method datasets
- fix issue where wrong tab would be displayed initially when managing data stocks
- issue #212: read subReferences of GlobalReferences during import 
- fix issue in Flow import where a missing reference flowproperty would result in the dataset silently not being imported


*Enhancements:*

- add "display all rows" option to dataset tables in administration backend




2.0.0 Beta 3
------------
June 26, 2013

Please see the Installation Guide for detailed instructions on how to upgrade your soda4LCA installation.

*Bugfixes:*

- fix issue where editing a user profile would not work
- fix issue where the application would not work with MySQL default lower_case_table_names=0 by adjusting table names
- fix missing i18n strings
- fix delete registry issue
- fix issue with misleading error message when deleting datasets
- fix issue with optional smtp auth settings
- fix issue with optional user.registration.registrationAddress


*Enhancements:*

- improve documentation
- add SMTP port configuration option
- upgrade eclipselink library to 2.5.0




2.0.0 Beta 2
------------
June 19, 2013

Please see the Installation Guide for detailed instructions on how to upgrade your soda4LCA installation.

*Bugfixes:*

- fixed issue where import of process datasets would fail for partial datasets
- fix issue with display of review section for process datasets 


*Enhancements:*

- add support for mail server authentication
- add new fields to user: job position, dataset use purpose



2.0.0 Beta 1
------------
May 6, 2013

Please see the Installation Guide for detailed instructions on how to upgrade your soda4LCA installation.

*Bugfixes:*

- various issues have been fixed, as well as performance enhancements have been applied


*New features:*

- The new data stocks feature has been introduced, where datasets can be logically combined into data stocks, which can
  be assigned fine-grained permissions for controlling access

- The new registry feature has been introduced, allowing a node to join a data network that is controlled by a registry.  
   
- The REST service API supports some more features now (see Service API documentation for details).


*Other:*

- The user interface has been improved throughout various parts of the application.

- Comprehensive documentation in HTML and PDF format has been added.




1.2.0
-----
January 16, 2012 (build 167)

NOTICE: For handling changes to the database schema, the Flyway framework has been introduced. This will make it
		unneccessary to perform manual database upgrades in the future. The downside is, that this release will NOT
		work with 1.1.x databases and requires an empty database schema. That means you may have to backup your data 
		prior to using this release (using the export feature introduced in release 1.1.3), emtpy the database and
		re-import the data afterwards.  
	   
*Bugfixes:*

- issue 166: elementary flow detection bug
- issue 171: export log info - elapsed time displayed is actually in seconds, not milliseconds
- issue 175: export fails if zip file directory does not exist
- issue 178: fail to show source datasets after import


*New features:*

- issue 167: add feature to export entire database, using context path as filename
- issue 102: when retrieving datasets, show only the most recent version of a dataset
- issue 165: add navigation by input/output flows
- issue 144: make paths to external files storage and temporary files configurable
- issue 113: add option to review own node information


*Other:*

- issue 76: rework configuration
- issue 138: introduce Flyway framework for automatic database migration
- issue 110: i18n - restructure lang.properties keys
- issue 83: nodeinfo: if port number is 80, do not include it in generated service URL




1.1.3 
-----
January 2, 2012 (build 144)

*New features:*

- issue 167: add feature to export entire database as ZIP archive




1.1.2 
-----
November 29, 2011 (build 136)

*Bugfixes:*

- issue 160: distributed search fails (unknown property exception)
- issue 146: change meaning of "Compliance" column in process dataset table
- issue 163: process full dataset view: no variables shown for outputs
- issue 156: hide empty section, subsection headers in full dataset HTML view




1.1.1 
-----
November 17, 2011 (build 131)

*Bugfixes:*

- issue 147: xlink:href attribute missing in service output
- issue 148: wrong cascade type on User, UserGroup
- issue 150: create user not working
- issue 153/154: html view: hide null values, show variables/parameters
- issue 155: broken authentication for RESTful service
- issue 158: display variables in process exchanges section
- issue 85: fixed issue with adapter classes for RESTful service which could result in timeouts on client side


*New features:*

- issue 159: add tooltips to links in dataset list view with dataset UUID, version



1.1.0 
-----
September 27, 2011

NOTICE: The database schema has changed slightly from the beta release. You need to either re-initialize the database
	    using the init.db script or apply the upgrade script in src/sql/db_upgrade_1.1beta_to_1.1final as well as the
	    password encryption tool at the same location.
	   
	   
*Bugfixes:*

- fixed an issue where the IMPACTINDICATOR column for LCIA method would hold only 255 characters instead of a maximum of 500 
- fixed some issues where local and remote search would not work correctly
- include soda4LCA.properties.template in distribution
- update Installation Guide to include correct connection string with proper encoding


*New features:*

- passwords for user accounts are now stored encrypted in the database
- show compliance in process list views
