---

GET Reference Years for Process Datasets
========================================

Basic GET operation that returns a list of reference years that are
present for process datasets.

Requests
--------

### Syntax

    GET /processes/referenceyears/

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

    GET /processes/referenceyears/

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sapi:integerList xmlns:sapi="http://www.ilcd-network.org/ILCD/ServiceAPI">
<sapi:integer>1996</sapi:integer>
<sapi:integer>1997</sapi:integer>
<sapi:integer>1999</sapi:integer>
<sapi:integer>2000</sapi:integer>
<sapi:integer>2001</sapi:integer>
<sapi:integer>2002</sapi:integer>
<sapi:integer>2004</sapi:integer>
<sapi:integer>2005</sapi:integer>
<sapi:integer>2006</sapi:integer>
<sapi:integer>2007</sapi:integer>
<sapi:integer>2008</sapi:integer>
<sapi:integer>2009</sapi:integer>
<sapi:integer>2010</sapi:integer>
</sapi:integerList>
~~~~
