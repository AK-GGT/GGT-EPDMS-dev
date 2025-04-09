---

GET Datasets from a Data Stock
==============================

Basic GET operations that return a list of datasets from a specific data
stock. Applies to all dataset types.

Requests
--------

### Syntax

    GET /datastocks/{datastock-uuid}/processes
    GET /datastocks/{datastock-uuid}/flows
    GET /datastocks/{datastock-uuid}/flowproperties
    GET /datastocks/{datastock-uuid}/unitgroups
    GET /datastocks/{datastock-uuid}/sources
    GET /datastocks/{datastock-uuid}/contacts
    GET /datastocks/{datastock-uuid}/lciamethods
                    

### Request Parameters

All request parameters as for the basic GET Datasets operation are
supported. See section [Request Parameters for GET
Datasets](./Service_API_Datasets_GET.md) for the full list.

Responses
---------

### Response Elements

The response returned is a list of dataset overview objects, wrapped in
a dataSetList object. See the section ["DataSetList Response
Elements"](./Service_API_Response_DatasetList.md) for a detailed description.

Examples
--------

### Sample Request

    GET
                        /datastocks/aca74e60-146e-11e2-892e-0800200c9a66/processes
                    

### Sample Response

    HTTP/1.1 200 OK
                        Content-Type: application/xml
                    

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<serviceapi:dataSetList xmlns:serviceapi="http://www.ilcd-network.org/ILCD/ServiceAPI" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:process="http://www.ilcd-network.org/ILCD/ServiceAPI/Process" serviceapi:totalSize="4" serviceapi:startIndex="0" serviceapi:pageSize="500">
    <process:process serviceapi:sourceId="ACME" xlink:href="http://lci.acme.com/DB/processes/0a1b40db-5645-4db8-a887-eb09300b7b74">
        <serviceapi:uuid>0a1b40db-5645-4db8-a887-eb09300b7b74</serviceapi:uuid>
        <serviceapi:permanentUri>http://lca.jrc.ec.europa.eu/lcainfohub/datasets/elcd/processes/0a1b40db-5645-4db8-a887-eb09300b7b74.xml</serviceapi:permanentUri>
        <serviceapi:dataSetVersion>03.00.000</serviceapi:dataSetVersion>
        <serviceapi:name xml:lang="en">Electricity Mix;AC;consumption mix, at consumer;1kV - 60kV</serviceapi:name>
        <serviceapi:classification name="ilcd">
            <serviceapi:class level="0">Energy carriers and technologies</serviceapi:class>
            <serviceapi:class level="1">Electricity</serviceapi:class>
        </serviceapi:classification>
        <process:type>LCI result</process:type>
        <process:location>EU-27</process:location>
        <process:time>
            <process:referenceYear>2002</process:referenceYear>
            <process:validUntil>2010</process:validUntil>
        </process:time>
        <process:parameterized>false</process:parameterized>
        <process:hasResults>false</process:hasResults>
        <process:lciMethodInformation>
            <process:methodPrinciple>Attributional</process:methodPrinciple>
            <process:approach>Allocation - mass</process:approach>
            <process:approach>Allocation - market value</process:approach>
            <process:approach>Allocation - exergetic content</process:approach>
            <process:approach>Allocation - net calorific value</process:approach>
        </process:lciMethodInformation>
        <process:complianceSystem name="ILCD Data Network - Entry-level">
            <serviceapi:reference type="source data set" version="00.00.000" uri="../sources/d92a1a12-2545-49e2-a585-55c259997756.xml"/>
            <process:overallCompliance>Not compliant</process:overallCompliance>
            <process:nomenclatureCompliance>Fully compliant</process:nomenclatureCompliance>
            <process:methodologicalCompliance>Fully compliant</process:methodologicalCompliance>
            <process:reviewCompliance>Not compliant</process:reviewCompliance>
            <process:documentationCompliance>Not compliant</process:documentationCompliance>
            <process:qualityCompliance>Not defined</process:qualityCompliance>
        </process:complianceSystem>
        <process:accessInformation/>
   </process:process>
</serviceapi:dataSetList>
~~~~
