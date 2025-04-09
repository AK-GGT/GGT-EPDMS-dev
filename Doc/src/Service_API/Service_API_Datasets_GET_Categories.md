---

GET Categories/Subcategories, GET Datasets in Categories
========================================================

Basic GET operations that return a list of datasets that match a
specific classification, or retrieve lists of categories for datasets.

This applies to all dataset types.

Requests
--------

### Syntax

    GET /processes/categories/?[catSystem=x]&[sort=id|name]
    GET /processes/categories/{category}/{subcategory}/.../?[catSystem=x]&[sort=id|name]
    GET /processes/categories/{category}/{subcategory}/.../subcategories/?[catSystem=x]&[sort=id|name]
    GET /processes/categorysystems/

### Request Parameters

| Name           | Description                                               |
| :------------: | :-------------------------------------------------------- |
| *catSystem*    | The category system to use. If none is specified, the default category system configured in the application properties will be used.    |
|                | Type: String                                              |
|                | Default: The default category system configured in the application properties.|
| *sort*         | Indicates the sort order of the result.                   |
|                | Values: id - sort by class ID                             |
|                | name - sort by name                                       |
|                | Default: name                                             |
| *lang*         | Return only results that support the specified language. Currently, if an object's name field is present in a certain language, the dataset is seen as supporting that language. |                 |
|                | Type: String                                              |
|                | Default: None                                             |
| *allVersions*  | Return all available versions for each dataset. By default, only the most recent version will be contained in the result.|
|                | Type: Boolean                                             |
|                | Default: false                                            |

For process datasets, additional filter parameters `compliance`, `complianceMode`, `dataSource` and `dataSourceMode` are
supported as described in ["GET Process Datasets (Query)"](./Service_API_Datasets_Process_Query.md).


#### Format

Data can either be returned in XML (default) or JSON formats, which can be controlled by using a request parameter:

| Name             |Values      | Description                                 |
| :------------:   |:---------- | :-----------------------------------------  |
| *format*         | *XML*,JSON | Returns the result in the specified format. |


Responses
---------

### Response Elements

If called without any additional parameters, with the "subcategories" or
the "categorysystems" parameter, the response returned is a list of
categories, wrapped in a categoryList object. See the section
["CategoryList Response
Elements"](./Service_API_Response_CategoryList.md) for a detailed
description.

If called with classes as path parameters, the response returned is a
list of dataset overview objects, wrapped in a dataSetList object. See
the section ["DataSetList Response
Elements"](./Service_API_Response_DatasetList.md) for a detailed
description.

Examples
--------

### Sample Request

    GET /processes/categories/

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sapi:categoryList xmlns:sapi="http://www.ilcd-network.org/ILCD/ServiceAPI">
  <sapi:category>Energy carriers and technologies</sapi:category>
  <sapi:category>Materials production</sapi:category>
  <sapi:category>Transport services</sapi:category>
</sapi:categoryList>
~~~~

### Sample Request

    GET /processes/categories/Energy%20carriers%20and%20technologies/subcategories/

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sapi:categoryList xmlns:sapi="http://www.ilcd-network.org/ILCD/ServiceAPI">
  <sapi:category>Electricity</sapi:category>
  <sapi:category>Heat and steam</sapi:category>
</sapi:categoryList>
~~~~

### Sample Request

    GET /processes/categories/Energy%20carriers%20and%20technologies/Electricity/

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

    GET /processes/categorysystems/

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<serviceapi:categoryList xmlns:serviceapi="http://www.ilcd-network.org/ILCD/ServiceAPI">
    <serviceapi:category>ILCD</serviceapi:category>
    <serviceapi:category>oekobau.dat</serviceapi:category>
    <serviceapi:category>GaBiCategories</serviceapi:category>
    <serviceapi:category>Other Clasification</serviceapi:category>
</serviceapi:categoryList>
~~~~
