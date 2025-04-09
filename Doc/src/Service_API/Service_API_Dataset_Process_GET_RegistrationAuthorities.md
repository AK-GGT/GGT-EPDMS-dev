---

GET Registration Authorities for Process Datasets
=========================================

Basic GET operation that returns a list of contact data sets that are present in the
registrationAuthority field in process data sets.

Requests
--------

### Syntax

    GET /processes/registrationAuthorities?format={format}

### Request Parameters
| Name             |Values      | Description                                 |
| :------------:   |:---------- | :-----------------------------------------  |
| *distributed*    | Boolean (default: false) | Propagates the query to all remote nodes which are configured for this instance. |
| *lang*           | String                   | Returns the entity names in the requested language. |
| *langFallback*   | Boolean (default: false) | If the name of the entity is not present in the requested or the default language, fall back to the next available language. It is recommended to always set this to *true* in multi-language distributed setups. |



#### Format

Data can either be returned in XML (default) or JSON formats, which can be controlled by using a request parameter:

| Name             |Values      | Description                                 |
| :------------:   |:---------- | :-----------------------------------------  |
| *format*         | *XML*, JSON | Returns the result in the specified format. |


Responses
---------

### Response Elements

The response returned is a list of contact objects. See the section
["Contact Response Elements"](./Service_API_Response_Contact.md)
for a more detailed description.

Examples
--------

### Sample Request

    GET /processes/registrationAuthorities?format=JSON

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/json

~~~~
{"startIndex":0,"pageSize":1,"totalCount":1,"data":[{"name":"The EPD Program","uuid":"bee77d31-1837-404b-abdf-ef271c83e5a7","version":"00.00.002","classific":"Organisations / Non-governmental organisations","dsType":"Contact"}]}
~~~~
