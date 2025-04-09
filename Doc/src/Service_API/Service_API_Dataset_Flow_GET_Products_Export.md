---

Export product flow datasets
============================

GET operation to export a ZIP file with all product flows.

Requests
--------

### Syntax

    GET /flows/products/export 
    
    GET /datastocks/{datastock-uuid}/flows/products/export
    
### Request Parameters

None.

Responses
---------

### Response Elements

The response for `export` is a ZIP archive. 

If proper permissions are missing for exporting, an HTTP 403 status code is returned instead.


Examples
--------

### Sample Request

    GET /flows/products/export

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/zip

### Sample Request

    GET /datastocks/8fee9e8f-0827-40ac-bf44-e6ddd4b2988b/flows/products/export

### Sample Response

    HTTP/1.1 403 Forbidden
 
