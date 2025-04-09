---

GET Producers or Consumers of a Flow
====================================

GET operation to identify processes that produce or consume a specific
flow.

Requests
--------

### Syntax

    GET /flows/{uuid}?version={version}/producers/
    GET /flows/{uuid}?version={version}/consumers/

### Request Parameters

| Name           | Description                                               |
| :------------: | :-------------------------------------------------------- |
| **             | Type:                                                     |
|                | Default:                                                  |

Responses
---------

### Response Elements

By default (and with proper access permissions), the full ILCD-formatted
dataset is returned as HTML representation (if not otherwise specified).
See the section ["Response Elements"](./Service_API.md#Response_Elements) for response
elements in overview.

Examples
--------

### Sample Request

     

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
~~~~
