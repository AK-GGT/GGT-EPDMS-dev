---

GET Datasets
============

Basic GET operations that return a list of datasets. Applies to all
dataset types.

Requests
--------

### Syntax

    GET /processes
    GET /flows
    GET /flowproperties
    GET /unitgroups
    GET /sources
    GET /contacts
    GET /lciamethods

### Request Parameters

| Name           | Data type | Default | Multiple? | Description                                               |
| :------------: | :-------- | :---- | :-------- | :-------------------------------------------------------- |
| *startIndex*   | Integer   | 0     | no        | As all result sets are paged, this specifies the index of the first item of the entire result set of the operation that shall be included in the response.|
| *pageSize*     | Integer   | 500   | no        | The page size (number of items) for the response.         |
| *search*       | Boolean   | false | no        | Perform a search query that will return results matching the given query parameters.|
| *distributed*  | Boolean   | false | no        | Perform a distributed search across all registered network nodes. |
| *name*         | String    |       | no        | search parameter                                          |
| *description*  | String    |       | no        | search parameter                                          |
| *classId*      | String    |       | yes       | search parameter                                          |
| *lang*         | String (language code)| | yes | Return results that support the specified language. Currently, if an object's name field is present in a certain language, the dataset is seen as supporting that language. Returned results will only expose information in the specified language, all others will be hidden. |
| *langFallback* | Boolean   | false | no        | Return also results that support any of the preferred languages configured on application level.|
| *allVersions*  | Boolean   | false | no        | Return all available versions for each dataset. By default, only the most recent version will be contained in the result. |
| *countOnly*    | Boolean   | false | no        | Return only the total number of datasets for the query. |

#### Format

Data can either be returned in XML (default) or JSON formats, which can be controlled by using a request parameter:

| Name             |Values      | Description                                 |
| :------------:   |:---------- | :-----------------------------------------  |
| *format*         | *XML*,JSON | Returns the result in the specified format. |

Responses
---------

### Response Elements

The response returned is a list of dataset overview objects, wrapped in
a dataSetList object. See the section ["DataSetList Response
Elements"](./Service_API_Response_DatasetList.md) for a detailed description.

Examples
--------

### Sample Request

    GET /processes

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


### Sample Request

    GET /processes?countOnly=true

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<serviceapi:dataSetList xmlns:serviceapi="http://www.ilcd-network.org/ILCD/ServiceAPI" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:process="http://www.ilcd-network.org/ILCD/ServiceAPI/Process" serviceapi:totalSize="4" serviceapi:startIndex="0" serviceapi:pageSize="500">
</serviceapi:dataSetList>
~~~~
