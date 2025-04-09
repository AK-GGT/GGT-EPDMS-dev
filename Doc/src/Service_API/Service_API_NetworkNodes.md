---

Network Nodes Information
=========================

This GET operation retrieves information about the connected network nodes.

Requests
--------

### Syntax

    GET /networknodes/nodeids

### Request Parameters

#### Format

Data can either be returned in XML (default) or JSON formats, which can be controlled by using a request parameter:

| Name             |Values      | Description                                 |
| :------------:   |:---------- | :-----------------------------------------  |
| *format*         | *XML*,JSON | Returns the result in the specified format. |


The response returned is a list of strings, wrapped in a stringList
object. See the section ["StringList Response
Elements"](./Service_API_Response_StringList.md) for a detailed
description.

Examples
--------

### Sample Request

    GET /networknodes/nodeids

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sapi:stringList xmlns:sapi="http://www.ilcd-network.org/ILCD/ServiceAPI">
	<sapi:string>ACME2</sapi:string>
	<sapi:string>ATLANTIS</sapi:string>
	<sapi:string>POMPINIA</sapi:string>
</sapi:stringList>
~~~~
