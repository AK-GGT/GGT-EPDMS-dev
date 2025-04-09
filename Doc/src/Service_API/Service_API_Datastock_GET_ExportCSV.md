---

Export Datastock
================

GET operation that returns a CSV file containing the latest versions of all process datasets in a data stock.

CSV export is only supported for datasets in ILCD+EPD format.

Requests
--------

### Syntax

    GET /datastocks/{datastock-uuid}/exportCSV

### Request Parameters

| Name               | Description                                      |
| :----------------: | :----------------------------------------------- |
| *decimalSeparator* | Indicate which decimal separator to use in CSV   |
|                    | Values: *dot*, *comma*                           |
|                    | Default: *dot*                                   |


Responses
---------

The response for `exportCSV` is a CSV file with the data from the process datasets. 

If proper permissions are missing for exporting the data stock, an HTTP 403 status code is returned instead.
 
If CSV export is not enabled in the application settings, an HTTP 501 status code is returned for the `exportCSV` resource.


Examples
--------

### Sample Request

    GET /datastocks/9a6a1a8c-045f-41af-ba45-13db07b636b0/exportCSV

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: text/plain


### Sample Request

    GET /datastocks/8fee9e8f-0827-40ac-bf44-e6ddd4b2988b/exportCSV

### Sample Response

    HTTP/1.1 403 Forbidden
 
