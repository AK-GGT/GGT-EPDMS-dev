---

GET Category Systems
====================

GET operation that returns a list of all category system definitions.

Requests
--------

### Syntax

    GET /categorySystems/

### Request Parameters

None.

Responses
---------

### Response Elements

| Name           | Description                                               |
| :------------: | :-------------------------------------------------------- |
| *categorySystems* | The container element for the list of category system objects. |
|                | Type: Container                                           |
|                | Ancestors: none.                                          |
| *categorySystem* | The container element for the category system object.          |
|    			 | Type: Container                                           |
|                | may occur multiple times                                  |
|                | Ancestors: categorySystems                                  |
| *@name*        | The name of the category system.    |
| 			     | Type: String                                             |
|                | Ancestors: categorySystem                                      |

Examples
--------

### Sample Request

    GET /categorySystems

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sapi:categorySystems xmlns:sapi="http://www.ilcd-network.org/ILCD/ServiceAPI">
	<sapi:categorySystem name="ILCD"/>
	<sapi:categorySystem name="GaBiCategories"/>
</sapi:categorySystems>
~~~~
