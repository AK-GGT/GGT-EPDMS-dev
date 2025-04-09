---

GET Exchanges of a Process Dataset
==================================

GET operation to retrieve the list of exchanges for a specific process
dataset.

Requests
--------

### Syntax

    GET /processes/{uuid}/exchanges

### Request Parameters

| Name           | Description                                               |
| :------------: | :-------------------------------------------------------- |
| *direction*    | The direction of the exchanges to be retrieved.           |
|                | Optional                                                  |
|                | Values: in, out                                           |
|                | Default: None                                             |
| *type*         | The type of the exchanges to be retrieved.                |
|                | Optional                                                  |
|                | Values: Elementary flow, Product flow, Waste flow, Other flow|
|                | Default: None                                             |

Responses
---------

### Response Elements

A datasetList object containing flow objects is returned as response.
See the sections
["DatasetList Response Elements"](./Service_API_Reponse_DatasetList.md)
and
["Flow Response Elements"](./Service_API_Response_Flow.md)
for a detailed description.

Examples
--------

### Sample Request

    GET /processes/0cbf76cc-0192-4617-acd3-0fdb3cecf6c7/exchanges?direction=in

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<serviceapi:dataSetList xmlns:serviceapi="http://www.ilcd-network.org/ILCD/ServiceAPI" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:flow="http://www.ilcd-network.org/ILCD/ServiceAPI/Flow" serviceapi:totalSize="60" serviceapi:startIndex="0" serviceapi:pageSize="60">
    <flow:flow serviceapi:sourceId="ACME" xlink:href="http://localhost:8091/Node/flows/fe0acd60-3ddc-11dd-ae5d-0050c2490048">
        <serviceapi:uuid>fe0acd60-3ddc-11dd-ae5d-0050c2490048</serviceapi:uuid>
        <serviceapi:permanentUri>http://lca.jrc.ec.europa.eu/lcainfohub/datasets/ilcd/flows/fe0acd60-3ddc-11dd-ae5d-0050c2490048_02.00.000.xml</serviceapi:permanentUri>
        <serviceapi:dataSetVersion>02.00.000</serviceapi:dataSetVersion>
        <serviceapi:name xml:lang="en">lead</serviceapi:name>
        <flow:flowCategorization name="ilcd">
            <serviceapi:category level="0">Resources</serviceapi:category>
            <serviceapi:category level="1">Resources from ground</serviceapi:category>
            <serviceapi:category level="2">Non-renewable element resources from ground</serviceapi:category>
        </flow:flowCategorization>
        <flow:type>Elementary flow</flow:type>
        <flow:referenceFlowProperty xlink:href="">
            <flow:name xml:lang="de">Formaldehyd</flow:name>
            <flow:defaultUnit>kg</flow:defaultUnit>
            <serviceapi:reference type="flow property data set" version="02.00.000" uri="../flowproperties/93a60a56-a3c8-11da-a746-0800200b9a66_02.00.000.xml"/>
        </flow:referenceFlowProperty>
    </flow:flow>
    <flow:flow serviceapi:sourceId="ACME" xlink:href="http://localhost:8091/Node/flows/1729ef88-6556-11dd-ad8b-0800200c9a66">
        <serviceapi:uuid>1729ef88-6556-11dd-ad8b-0800200c9a66</serviceapi:uuid>
        <serviceapi:permanentUri>http://lca.jrc.ec.europa.eu/lcainfohub/datasets/ilcd/flows/1729ef88-6556-11dd-ad8b-0800200c9a66_02.00.000.xml</serviceapi:permanentUri>
        <serviceapi:dataSetVersion>02.00.000</serviceapi:dataSetVersion>
        ...
    </flow:flow>
</serviceapi:dataSetList>
~~~~
