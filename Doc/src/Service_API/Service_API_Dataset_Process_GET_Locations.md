---

GET Locations for Process Datasets
=========================================

Basic GET operation that returns a list of location codes that are present in the
location field in process datasets.

Requests
--------

### Syntax

    GET /processes/locations/

### Request Parameters

#### Format

Data can either be returned in XML (default) or JSON formats, which can be controlled by using a request parameter:

| Name             |Values      | Description                                 |
| :------------:   |:---------- | :-----------------------------------------  |
| *format*         | *XML*,JSON | Returns the result in the specified format. |


Responses
---------

### Response Elements

The response returned is a list of strings, wrapped in a stringList
object. See the section
["StringList Response Elements"](./Service_API_Response_StringList.md)
for a detailed description.

Examples
--------

### Sample Request

    GET /processes/locations/

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sapi:stringList xmlns:sapi="http://www.ilcd-network.org/ILCD/ServiceAPI">
	<sapi:string>AT</sapi:string>
	<sapi:string>BE</sapi:string>
	<sapi:string>EG</sapi:string>
	<sapi:string>EU-25</sapi:string>
	<sapi:string>EU-27</sapi:string>
	<sapi:string>RER</sapi:string>
	<sapi:string>FR</sapi:string>
	<sapi:string>DE</sapi:string>
	<sapi:string>GLO</sapi:string>
	<sapi:string>LU</sapi:string>
	<sapi:string>NL</sapi:string>
	<sapi:string>PL</sapi:string>
	<sapi:string>PT</sapi:string>
	<sapi:string>SI</sapi:string>
	<sapi:string>CH</sapi:string>
	<sapi:string>TR</sapi:string>
</sapi:stringList>
~~~~
