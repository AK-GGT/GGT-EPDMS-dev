---

Node Information
================

This GET operation retrieves information about the node.

Requests
--------

### Syntax

    GET /nodeinfo

### Request Parameters

None.

Responses
---------

### Response Elements

| Name             | Description                                              |
| :--------------: | :------------------------------------------------------- |
| *nodeInfo*       | Contains the elements with the node information.         |
|                  | Type: Container                                          |
|                  | Ancestors: None.                                         |
| *nodeID*         | The ID of the node.                                      |
|                  | Type: String (no spaces allowed)                         |
|                  | Ancestors: nodeInfo                                      |
| *name*           | The full name of the node                                |
|                  | Type: String                                             |
|                  | Ancestors: nodeInfo                                      |
| *operator*       | The person or entity operating this node                 |
|                  | Type: String                                             |
|                  | Ancestors: nodeInfo                                      |
| *description*    | A description of the node                                |
|                  | Type: String                                             |
|                  | Ancestors: nodeInfo                                      |
| *baseURL*        | The base URL of the node's service interface.            |
|                  | Type: String                                             |
|                  | Ancestors: nodeInfo                                      |
| *administrativeContact* | The element carrying information about the administrative contact for the node. |
|                  | Type: String                                             |
|                  | Ancestors: nodeInfo                                      |
| *centralContactPoint* | The central contact point                                |
|                  | Type: String                                             |
|                  | Ancestors: nodeInfo.centralContactPoint                  |
| *email*          | The email address                                        |
|                  | Type: String                                             |
|                  | Ancestors: nodeInfo.centralContactPoint                  |
| *phone*          | The phone number                                         |
|                  | Type: String                                             |
|                  | Ancestors: nodeInfo.centralContactPoint                  |
| *www*            | The www address                                          |
|                  | Type: String                                             |
|                  | Ancestors: nodeInfo.centralContactPoint                  |

Examples
--------

### Sample Request

    GET /nodeinfo

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<nodeInfo xmlns="http://www.ilcd-network.org/ILCD/ServiceAPI/NodeInfo" xmlns:contact="http://www.ilcd-network.org/ILCD/ServiceAPI/Contact">
    <nodeID>ACME</nodeID>
    <name>ACME Public LCI Database</name>
    <operator>ACME Inc.</operator>
    <description xml:lang="en">Free Text Description</description>
    <baseURL>http://lci.acme.com/DB</baseURL>
    <administrativeContact>
        <contact:centralContactPoint>ACME Inc. Worldwide Headquarters</contact:centralContactPoint>
        <contact:email>info@acme.com</contact:email>
        <contact:phone>+49 721 555 4242</contact:phone>
        <contact:www>www.acme.com</contact:www>
    </administrativeContact>
</nodeInfo>
~~~~
