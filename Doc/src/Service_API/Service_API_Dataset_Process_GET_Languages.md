---

GET Languages for Process Datasets
=========================================

Basic GET operation that returns a list of language codes that are present in the
name field in process datasets.

Requests
--------

### Syntax

    GET /processes/languages/

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

    GET /processes/langugages/

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sapi:stringList xmlns:sapi="http://www.ilcd-network.org/ILCD/ServiceAPI">
	<sapi:string>en</sapi:string>
	<sapi:string>de</sapi:string>
	<sapi:string>ru</sapi:string>
</sapi:stringList>
~~~~
