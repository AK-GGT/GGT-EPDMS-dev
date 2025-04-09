---

GET Tags For Process Datasets
=========================================

Basic GET operation that returns a list of tags that are associated with the underlying process.

Requests
--------

### Syntax

    GET /processes/{uuid}/tags[?version={version}&format={format}]

### Request Parameters

| Name           | Description                                               |
| :------------: | :-------------------------------------------------------- |
| *version*      | The data set version. (Optional)                          |

#### Format

Data can either be returned in XML (default) or JSON formats, which can be controlled by using a request parameter:

| Name             |Values      | Description                                 |
| :------------:   |:---------- | :-----------------------------------------  |
| *format*         | *XML*,JSON | Returns the result in the specified format. |


Responses
---------

### Response Elements

The response returned is a list of strings, wrapped in a stringList
object.
See the section
["StringList Response Elements"](./Service_API_Response_StringList.md)
for a detailed description.

Examples
--------

### Sample Request

    GET /processes/0c3df782-3bb5-4cbd-b2a1-e33541187041/tags

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <sapi:stringList xmlns:ni="http://www.ilcd-network.org/ILCD/ServiceAPI/NodeInfo" xmlns:c="http://www.ilcd-network.org/ILCD/ServiceAPI/Contact" xmlns:sapi="http://www.ilcd-network.org/ILCD/ServiceAPI" xmlns:xlink="http://www.w3.org/1999/xlink">
        <sapi:string>epd</sapi:string>
        <sapi:string>new</sapi:string>
        <sapi:string>reviewed</sapi:string>
    </sapi:stringList>
