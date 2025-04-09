---

GET product flow datasets
=========================

GET operation to retrieve product flows that are representing specific
or generic products.

Requests
--------

### Syntax

    GET /flows/products/?[type=specific|generic]&[startIndex=0]&[pageSize=500] 

### Request Parameters

| Name           | Description                                               |
| :------------: | :-------------------------------------------------------- |
| *type*         | In order to filter the type of product datasets returned, this attribute may be used. If omitted, all products will be returned.|
|                | Type: values "specific" or "generic"                      |
| *startIndex*   | parameter for controlling pagination, see [GET Datasets Request Parameters](./Service_API_Datasets_GET.md#Request_Parameters) |
| *pageSize*     | parameter for controlling pagination, see [GET Datasets Request Parameters](./Service_API_Datasets_GET.md#Request_Parameters) |

Responses
---------

### Response Elements

By default (and with proper access permissions), the full ILCD-formatted
dataset is returned as HTML representation (if not otherwise specified).
See the section ["Response Elements"]./Service_API.md#response_elements) for
response elements in overview.

Examples
--------

### Sample Request

     

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
~~~~
