---

Export Datastock
================

GET operation that returns a compressed ZIP archive containing the latest versions of all datasets in a data stock.

Requests
--------

### Syntax

    GET /datastocks/{datastock-uuid}/export

### Request Parameters

None.

Responses
---------

The response for `export` is a ZIP archive. 


If proper permissions are missing for exporting the data stock, an HTTP 403 status code is returned instead.


Examples
--------

### Sample Request

    GET /datastocks/9a6a1a8c-045f-41af-ba45-13db07b636b0/export

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/zip

### Sample Request

    GET /datastocks/8fee9e8f-0827-40ac-bf44-e6ddd4b2988b/export

### Sample Response

    HTTP/1.1 403 Forbidden
 
