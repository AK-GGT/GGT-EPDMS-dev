---

GET Original EPD of a Source Dataset
====================================

GET operation to retrieve the original EPD pdf attachment for a specific
process dataset.

Requests
--------

### Syntax

    GET /processes/{uuid}/epd?version={version}

### Request Parameters

| Name           | Description                                               |
| :------------: | :-------------------------------------------------------- |
| *version*      | The version number of the data set whose original EPD is to be retrieved. If omitted the most recent version of the data set is considered.|
|                | Type: Version number of the form 00.00.000                |
|                | Default: None                                             |

Responses
---------

The PDF document.

The returned MIME type will be `application/pdf`.

Examples
--------

### Sample Request

    GET /sources/0a34866e-ce75-48c8-82e6-0080739e7154/epd?version=02.00.010

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/pdf

(the PDF document)
