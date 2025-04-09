---

GET ValidUntil Years for Process Datasets
=========================================

Basic GET operation that returns a list of years that are present in the
validUntil field in process datasets.

Requests
--------

### Syntax

    GET /processes/validuntilyears/

### Request Parameters

#### Format

Data can either be returned in XML (default) or JSON formats, which can be controlled by using a request parameter:

| Name             |Values      | Description                                 |
| :------------:   |:---------- | :-----------------------------------------  |
| *format*         | *XML*,JSON | Returns the result in the specified format. |


Responses
---------

### Response Elements

The response returned is a list of integers, wrapped in a integerList
object. See the section
["IntegerList Response Elements"](./Service_API_Response_IntegerList.md)
for a detailed description.

Examples
--------

### Sample Request

    GET /processes/validuntilyears/

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sapi:integerList xmlns:sapi="http://www.ilcd-network.org/ILCD/ServiceAPI">
<sapi:integer>2010</sapi:integer>
<sapi:integer>2011</sapi:integer>
<sapi:integer>2012</sapi:integer>
<sapi:integer>2013</sapi:integer>
<sapi:integer>2014</sapi:integer>
<sapi:integer>2015</sapi:integer>
</sapi:integerList>
~~~~
