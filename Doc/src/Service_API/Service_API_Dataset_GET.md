---

GET Dataset
===========

Basic GET operations for all dataset types to retrieve a single dataset

Requests
--------

### Syntax

    GET /processes/{uuid}?version={version}
    GET /flows/{uuid}?version={version}
    GET /flowproperties/{uuid}?version={version}
    GET /unitgroups/{uuid}?version={version}
    GET /sources/{uuid}?version={version}
    GET /contacts/{uuid}?version={version}
    GET /lciamethods/{uuid}?version={version}

#### Versioning

By default, the most recent version of a dataset is retrieved. When
specifying the version parameter, that specific version will be
returned.

### Request Parameters

| Name           | Description                                               |
| :------------: | :-------------------------------------------------------- |
| *version*      | The version number of the dataset to retrieve. If omitted, always the most recent version is retrieved.|
|                | Type: Version number of the form 00.00.000                |
|                | Default: None                                             |
| *format*       | Specifies the format of the response.                     |
|                | Values: `XML`, `JSON`, `HTML`                             |
|                | Default: `HTML`                                           |
| *view*         | Specifies whether the response should be summary of the dataset, the entire dataset or just the metadata section.  |
|                | Values: `overview`, `detail`, `metadata`, `extended` (the value `full` as the equivalent for `detail` in previous versions has been deprecated) |
|                | The `extended` view will return an object where the flow and flow property links have been resolved and the corresponding information is included inline. It is currently only available for the process dataset and the JSON format. |
|                | Default: `detail`                                         |

Responses
---------

### Response Elements

By default (and with proper access permissions), the full ILCD-formatted
dataset is returned as HTML representation (if not otherwise specified).
See the section ["Response Elements"](./Service_API.md#Response_Elements) for response
elements in overview.

Examples
--------

### Sample Request

    GET /processes/00000000-0000-0000-0000-000000000000?format=xml&view=overview

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<process xmlns:serviceapi="http://www.ilcd-network.org/ILCD/ServiceAPI" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns="http://www.ilcd-network.org/ILCD/ServiceAPI/Process" xmlns:flow="http://www.ilcd-network.org/ILCD/ServiceAPI/Flow" xmlns:flowProperty="http://www.ilcd-network.org/ILCD/ServiceAPI/FlowProperty" xmlns:unitGroup="http://www.ilcd-network.org/ILCD/ServiceAPI/UnitGroup" xmlns:lciamethod="http://www.ilcd-network.org/ILCD/ServiceAPI/LCIAMethod" xmlns:source="http://www.ilcd-network.org/ILCD/ServiceAPI/Source" xmlns:contact="http://www.ilcd-network.org/ILCD/ServiceAPI/Contact" serviceapi:accessRestricted="true">
    <serviceapi:uuid>00000000-0000-0000-0000-000000000000</serviceapi:uuid>
    <serviceapi:permanentUri>http://db.ilcd-network.org/data/processes/processtest</serviceapi:permanentUri>
    <serviceapi:dataSetVersion>01.00.000</serviceapi:dataSetVersion>
    <serviceapi:name xml:lang="en">Foo unit process</serviceapi:name>
    <serviceapi:classification>
        <serviceapi:class level="0">Energy systems</serviceapi:class>
        <serviceapi:class level="1">Foo energy systems</serviceapi:class>
    </serviceapi:classification>
    <serviceapi:generalComment xml:lang="en">foo bar</serviceapi:generalComment>
    <serviceapi:synonyms xml:lang="en">Foobar</serviceapi:synonyms>
    <serviceapi:synonyms xml:lang="de">Fubar</serviceapi:synonyms>
    <type>Unit process, single operation</type>
    <quantitativeReference>
        <referenceFlow>
            <name xml:lang="en">electricity mix</name>
            <meanValue>0.0</meanValue>
            <serviceapi:reference type="flow data set" refObjectId="00000000-0000-0000-0000-000000000000">
                <serviceapi:shortDescription xml:lang="en">foo flow</serviceapi:shortDescription>
            </serviceapi:reference>
        </referenceFlow>
        <functionalUnit xml:lang="en">Foonctional Unit</functionalUnit>
    </quantitativeReference>
    <location>RER</location>
    <time>
        <referenceYear>2009</referenceYear>
        <validUntil>2012</validUntil>
    </time>
    <parameterized>true</parameterized>
    <hasResults>true</hasResults>
    <containsProductModel>false</containsProductModel>
    <lciMethodInformation>
        <methodPrinciple>Attributional</methodPrinciple>
        <approach>Allocation - gross calorific value</approach>
        <approach>Allocation - element content</approach>
    </lciMethodInformation>
    <completenessProductModel>All relevant flows quantified</completenessProductModel>
    <complianceSystem name="ILCD Compliance - entry level">
        <overallCompliance>Fully compliant</overallCompliance>
    </complianceSystem>
    <review type="Independent external review">
        <scope name="LCI results or Partly terminated system">
            <method name="Element balance"/>
        </scope>
        <dataQualityIndicators>
            <dataQualityIndicator name="Completeness" value="Good"/>
        </dataQualityIndicators>
        <reviewDetails xml:lang="en">details here</reviewDetails>
    </review>
    <overallQuality>FOOOO</overallQuality>
    <useAdvice xml:lang="en">hear my advice</useAdvice>
    <accessInformation>
        <copyright>true</copyright>
        <licenseType>Free of charge for all users and uses</licenseType>
        <useRestrictions xml:lang="en">Rated R</useRestrictions>
    </accessInformation>
    <format>ILCD 1.0</format>
    <ownership type="contact data set" refObjectId="00000000-0000-0000-0000-000000000000">
        <serviceapi:shortDescription xml:lang="en">JRC</serviceapi:shortDescription>
    </ownership>
</process>
~~~~

### Sample Request

    GET /processes/00000000-0000-0000-0000-000000000000?format=xml

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="utf-8"?>
<?xml-stylesheet version="1.0" href="../../stylesheets/process2html.xsl" type="text/xsl"?>
<processDataSet xmlns="http://lca.jrc.it/ILCD/Process" xmlns:common="http://lca.jrc.it/ILCD/Common" locations="../ILCDLocations.xml" version="1.1">
<processInformation>
<dataSetInformation>
<common:UUID>00000000-0000-0000-0000-000000000000</common:UUID>
<name>
<baseName xml:lang="en">Electricity Mix, Foo</baseName>
<treatmentStandardsRoutes xml:lang="en">AC</treatmentStandardsRoutes>
<mixAndLocationTypes xml:lang="en">consumption mix, at consumer</mixAndLocationTypes>
<functionalUnitFlowProperties xml:lang="en">1kV - 60kV</functionalUnitFlowProperties>
</name>
<common:synonyms xml:lang="en">power grid mix</common:synonyms>
<classificationInformation>
<common:classification>
<common:class level="0">Energy carriers and technologies</common:class>
<common:class level="1">Electricity</common:class>
</common:classification>
</classificationInformation>
<common:generalComment xml:lang="en">Good overall data quality. Energy carrier mix information based on official statistical information including import/export. Detailed power plant models were used, which combine measured emissions plus calculated values for not measured emissions of e.g. organics or heavy metals. Energy carrier extraction and processing data is of sufficient to good (e.g. refinery) data quality. Inventory is partly based on primary industry data, partly on secondary literature data.</common:generalComment>
</dataSetInformation>
<quantitativeReference type="Reference flow(s)">
<referenceToReferenceFlow>63</referenceToReferenceFlow>
</quantitativeReference>
<time>
<common:referenceYear>2002</common:referenceYear>
<common:dataSetValidUntil>2010</common:dataSetValidUntil>
<common:timeRepresentativenessDescription xml:lang="en">Annual average</common:timeRepresentativenessDescription>
</time>
<geography>
...
</processDataSet>
~~~~
