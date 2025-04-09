---

Export process dataset including dependencies
=============================================

GET operation for process datasets to retrieve a single dataset with all its dependencies

Requests
--------

### Syntax

    GET /processes/{uuid}/zipexport?version={version}

#### Versioning

By default, the most recent version of the process dataset is retrieved. When
specifying the version parameter, that specific version will be returned.

### Request Parameters

| Name           | Description                                               |
| :------------: | :-------------------------------------------------------- |
| *version*      | The version number of the dataset to retrieve. If omitted, always the most recent version is retrieved.|
|                | Type: Version number of the form 00.00.000                |
|                | Default: None                                             |

Responses
---------

### Response Elements

With proper access permissions, the full ILCD-formatted
dataset along with all its dependent secondary datasets which are recursively
collected up to a depth of 2 will be returned in an ILCD ZIP archive.


Examples
--------

### Sample Request

    GET /processes/00000000-0000-0000-0000-000000000000/zipexport?format=xml&view=overview

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/zip

