---

GET Datastocks
==============

GET operation that returns a list of all datastocks.

Requests
--------

### Syntax

    GET /datastocks

### Request Parameters

#### Format

Data can either be returned in XML (default) or JSON formats, which can be controlled by using a request parameter:

| Name             |Values      | Description                                 |
| :------------:   |:---------- | :-----------------------------------------  |
| *format*         | *XML*,JSON | Returns the result in the specified format. |


Responses
---------

### Response Elements

| Name           | Description                                               |
| :------------: | :-------------------------------------------------------- |
| *dataStockList* (datastock) | The container element for the list of data stock objects. |
|                | Type: Container                                           |
|                | Ancestors: none.                                          |
| *dataStock* (datastock)    | The container element for the data stock object.          |
|    			 | Type: Container                                           |
|                | may occur multiple times                                  |
|                | Ancestors: dataStockList                                  |
| *@root* (datastock)     | Indicates whether the data stock is a root data stock.    |
| 			     | Type: Boolean                                             |
|                | Ancestors: dataStock                                      |
| *uuid*         | The UUID of the data stock.                               |
|                | Type: UUID                                                |
|                | Ancestors: dataStock                                      |
| *shortName*    | The short name (handle) of the data stock.                |
|                | Type: String                                              |
|                | Ancestors: dataStock                                      |
| *name*         | The name of the data stock.                               |
|                | Type: String Multilang                                    |
|                | may occur multiple times                                  |
|                | Ancestors: dataStock                                      |
| *description* (datastock)  | A description for the data stock.             |
|    			 | Type: String Multilang                                    |
|                | may occur multiple times                                  |
|                | Ancestors: dataStock                                      |

Examples
--------

### Sample Request

    GET /datastocks

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sapi:dataStockList xmlns:sapi="http://www.ilcd-network.org/ILCD/ServiceAPI">
   <sapi:dataStock sapi:root="true">
        <sapi:uuid>c5b60a72-f031-4b71-bb23-834bfe0aee5a</sapi:uuid>
        <sapi:shortName>default</sapi:shortName>
        <sapi:name xml:lang="en">Default root data stock</sapi:name>
        <sapi:description xml:lang="en">This is the default root data stock</sapi:description>
    </sapi:dataStock>
    <sapi:dataStock sapi:root="false">
        <sapi:uuid>3c2371ae-5328-47c5-b141-272401d36c64</sapi:uuid>
        <sapi:shortName>SOME_DATA</sapi:shortName>
        <sapi:name xml:lang="en">demo logical data stock</sapi:name>
        <sapi:description xml:lang="en">description</sapi:description>
    </sapi:dataStock>
</sapi:dataStockList>
~~~~
