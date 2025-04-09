---

Remove Assigned (DELETE) Tags From Process
=============

Basic DELETE operation to remove one or more given tags from a process data set.

Authentication is required and the TAG role needs to be present in order to be
able to carry out this operation.
 

Requests
--------

### Syntax

    DELETE processes/{uuid}/tags[?tag={tagParam1}&tag={tagParam2}&version={version}]


### Request Parameters

| Name           | Description                                                |
| :------------- | :--------------------------------------------------------- |
| *tag*          | Name of a tag that shall be unassigned from given process. |
|                | Multiple tags can be specified here.                       |
| *version (optional)* | Version number to further specify the data set.      |
|                | If none is provided, the newest version is assumed.        |

### Request Headers

| Name            | Description                                               |
| :------------:  | :-------------------------------------------------------- |
| *Authorization* | Bearer token.                                             |

#### Format

Tags that couldn't be found in the database are returned in the form of a StringList either
in XML or JSON format.

| Name             |Values       | Description                                 |
| :------------:   |:----------  | :-----------------------------------------  |
| *format*         | *XML*, JSON | Returns list of the problematic tags in the specified format. |

Responses
---------

If the operation was successful, an HTTP 204 status code is returned.
If the operation was partly successful, an HTTP 200 status code is returned along with a list of
the names of the unprocessed tags.

### Response Elements

The response returned is either empty or contains a list of strings, wrapped in a stringList
object.
See the section
["StringList Response Elements"](./Service_API_Response_StringList.md)
for a detailed description.

Examples
--------

### Sample Request

    DELETE /resource/processes/0c3df782-3bb5-4cbd-b2a1-e33541187041/tags?tag=new&tag=non-existingTag

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<sapi:stringList xmlns:ni="http://www.ilcd-network.org/ILCD/ServiceAPI/NodeInfo" xmlns:c="http://www.ilcd-network.org/ILCD/ServiceAPI/Contact" 		xmlns:sapi="http://www.ilcd-network.org/ILCD/ServiceAPI" xmlns:xlink="http://www.w3.org/1999/xlink">
    	<sapi:string>non-existingTag</sapi:string>
	</sapi:stringList>
