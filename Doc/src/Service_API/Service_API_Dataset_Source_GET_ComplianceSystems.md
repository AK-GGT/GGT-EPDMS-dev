---

GET all Source datasets that represent a compliance system
==========================================================

GET operation to retrieve a list of source datasets representing a compliance
system, which are identified by having "Compliance systems" as level 0
classification.
 

Requests
--------

### Syntax

    GET /sources/complianceSystems



#### Format

Data can either be returned in XML (default) or JSON formats, which can be controlled by using a request parameter:

| Name             |Values      | Description                                 |
| :------------:   |:---------- | :-----------------------------------------  |
| *format*         | *XML*,JSON | Returns the result in the specified format. |



Responses
---------

The response returned is a list of source dataset overview objects, 
wrapped in a dataSetList object. See the section ["DataSetList Response
Elements"](./Service_API_Response_DatasetList.md) for a detailed description.



Examples
--------

### Sample Request

    GET /sources/complianceSystems

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml
    <sapi:dataSetList xmlns:sapi="http://www.ilcd-network.org/ILCD/ServiceAPI" xmlns:xlink="http://www.w3.org/1999/xlink" 
    xmlns:s="http://www.ilcd-network.org/ILCD/ServiceAPI/Source"  sapi:totalSize="4" sapi:startIndex="0" sapi:pageSize="4">
    <s:source sapi:sourceId="ACME2" xlink:href="http://localhost:8080/Node/sources/b00f9ec0-7874-11e3-981f-0800200c9a66">
        <sapi:uuid>b00f9ec0-7874-11e3-981f-0800200c9a66</sapi:uuid>
        <sapi:dataSetVersion>00.02.000</sapi:dataSetVersion>
        <sapi:name xml:lang="en">EN 15804</sapi:name>
        <sapi:name xml:lang="de">EN 15804</sapi:name>
        <sapi:classification name="ilcd">
            <sapi:class level="0">Compliance systems</sapi:class>
        </sapi:classification>
    </s:source>
    <s:source sapi:sourceId="ACME2" xlink:href="http://localhost:8080/Node/sources/d92a1a12-2545-49e2-a585-55c259997756">
        <sapi:uuid>d92a1a12-2545-49e2-a585-55c259997756</sapi:uuid>
        <sapi:dataSetVersion>30.00.000</sapi:dataSetVersion>
        <sapi:name xml:lang="en">ILCD Data Network - Entry-level</sapi:name>
        <sapi:classification name="ilcd">
            <sapi:class level="0">Compliance systems</sapi:class>
        </sapi:classification>
    </s:source>
    <s:source sapi:sourceId="ACME2" xlink:href="http://localhost:8080/Node/sources/ab25397d-fbf8-4563-889a-5bb7fb1baa93">
        <sapi:uuid>ab25397d-fbf8-4563-889a-5bb7fb1baa93</sapi:uuid>
        <sapi:dataSetVersion>00.00.000</sapi:dataSetVersion>
        <sapi:name xml:lang="en">ISO 14040 conformity system</sapi:name>
        <sapi:classification name="ilcd">
            <sapi:class level="0">Compliance systems</sapi:class>
        </sapi:classification>
    </s:source>
    <s:source sapi:sourceId="ACME2" xlink:href="http://localhost:8080/Node/sources/66279383-8dc3-46c1-80d1-99866cc01e6a">
        <sapi:uuid>66279383-8dc3-46c1-80d1-99866cc01e6a</sapi:uuid>
        <sapi:dataSetVersion>00.00.000</sapi:dataSetVersion>
        <sapi:name xml:lang="en">PEF/OEF implementation, mandatory data 2016-2020</sapi:name>
        <sapi:classification name="ilcd">
            <sapi:class level="0">Compliance systems</sapi:class>
        </sapi:classification>
    </s:source>
</sapi:dataSetList>

