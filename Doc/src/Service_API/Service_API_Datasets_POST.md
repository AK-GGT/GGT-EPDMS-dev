---

POST Datasets
=============

Basic POST operation to import one or more datasets into the database.
Applies to all dataset types. A single dataset can be POSTed as well as
a ZIP archive containing an arbitrary number of datasets.

Requests
--------

### Syntax

    POST /processes
    POST /flows
    POST /flowproperties
    POST /unitgroups
    POST /sources
    POST /contacts
    POST /lciamethods

The content can be POSTed as `application/xml` (single dataset) or
`multipart/form-data` (single dataset or ZIP archive). For the former
case, the UUID of the data stock the data is to be stored in on the
server side can optionally be specified as header parameter. For the
latter, it can be transmitted either as header or form parameter. If
both header and form parameter specifying a target data stock are 
present, the header parameter takes precedence.

### Form Parameters

| Name           | Description                                               |
| :------------- | :-------------------------------------------------------- |
| *file*         | The file input stream. Applies only for POSTing `multipart/form-data`. |
|                | Type: InputStream                                         |
|                | Default: None (optional)                                  |
| *zipFileName (optional)* | When POSTing an entire ZIP archive, this parameter needs to be present with the file name of the ZIP archive. Applies only for POSTing `multipart/form-data`. |
|                | Type: String                                              |
|                | Default: None                                             |
| *stock (optional)* | The UUID of the root data stock to store the dataset(s) in. Applies only for POSTing `multipart/form-data`. |
|                | Type: String                                              |
|                | Default: None                                             |

### Request Headers

| Name           | Description                                               |
| :------------: | :-------------------------------------------------------- |
| *stock (optional)* | The UUID of the root data stock to store the dataset(s) in |
|                | Type: String                                              |
|                | Default: None                                             |

Responses
---------

Single dataset: If the import was successful, an HTTP 200 status code is returned along
with a single datastock object that represents the datastock the
imported dataset(s) has/have been stored in.

ZIP import: If the import was successfully submitted, an HTTP 202 status code is returned.


### Response Elements

See [Datastock Response Elements](./Service_API_Response_Datastocks.md).

Examples
--------

### Sample Request

    POST /processes
    stock: 7c6fdb08-902e-48de-bb94-9600bf317331

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <dataStock xmlns="http://www.ilcd-network.org/ILCD/ServiceAPI" root="true">
        <uuid>7c6fdb08-902e-48de-bb94-9600bf317331</uuid>
        <shortName>default</shortName>
        <name xml:lang="en">Default root data stock</name>
        <description xml:lang="en">This is the default root data stock</description>
    </dataStock>
