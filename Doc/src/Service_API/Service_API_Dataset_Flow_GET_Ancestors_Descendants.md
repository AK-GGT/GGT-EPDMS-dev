---

GET related datasets for a (Product) Flow
=========================================

GET operation to retrieve product flows that are specific or generic for
a given product flow.

Requests
--------

### Syntax

    flows/{uuid}/descendants?[depth=n]
    flows/{uuid}/ancestors?[depth=n]
    flows/{uuid}/ancestor?[productType=specific|generic]

### Request Parameters

| Name           | Description                                               |
| :------------: | :-------------------------------------------------------- |
| *depth*        | Specifies how many levels of ancestry/descent should be traversed. If 0, all ancestors/descendants will be returned. |
|                | Type: Integer                                             |
|                | Default: 0 (all)                                          |
| *productType*  | In order to filter the type of product datasets returned, this attribute may be used. If omitted, all products will be returned. |
|                | Type: values "specific" or "generic"                      |

Responses
---------

### Response Elements

The response returned is a list of flow overview objects, wrapped in a
dataSetList object. See the section
["DataSetList ResponseElements"](Service_API_Response_DatasetList.md)
for a detailed
description.

Examples
--------

### Sample Request

     

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
~~~~
