---

GET all Source datasets that represent a database
=================================================

GET operation to retrieve a list of source datasets representing a database,
which are identified by having "Database" as level 0 classification.
 

Requests
--------

### Syntax

    GET /sources/databases



#### Format

Data can either be returned in XML (default) or JSON formats, which can be controlled by using a request parameter:

| Name             |Values      | Description                                 |
| :------------:   |:---------- | :-----------------------------------------  |
| *format*         | *XML*,JSON | Returns the result in the specified format. |



Responses
---------

The response returned is a list of source dataset overview objects, 
wrapped in a dataSetList object. See the section ["DataSetList Response
Elements"](./Service_API_Responses_DatasetList.md) for a detailed description.



Examples
--------

### Sample Request

    GET /sources/databases

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml
	<sapi:dataSetList xmlns:sapi="http://www.ilcd-network.org/ILCD/ServiceAPI" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:s="http://www.ilcd-network.org/ILCD/ServiceAPI/Source" sapi:totalSize="4"
	    sapi:startIndex="0" sapi:pageSize="4">
	    <s:source sapi:sourceId="ACME2" xlink:href="http://lcdn.thinkstep.com/Node/sources/b497a91f-e14b-4b69-8f28-f50eb1576766?version=00.03.001">
	        <sapi:uuid>b497a91f-e14b-4b69-8f28-f50eb1576766</sapi:uuid>
	        <sapi:dataSetVersion>00.03.001</sapi:dataSetVersion>
	        <sapi:name xml:lang="en">ecoinvent database (all versions)</sapi:name>
	        <sapi:classification name="ilcd">
	            <sapi:class level="0" classId="ca877255-4278-4aae-960e-1f1951367f8d">Databases</sapi:class>
	        </sapi:classification>
	    </s:source>
	    <s:source sapi:sourceId="ACME2" xlink:href="http://lcdn.thinkstep.com/Node/sources/b9871155-5d59-45a9-a4b2-3d1eaf3a5916?version=03.00.000">
	        <sapi:uuid>b9871155-5d59-45a9-a4b2-3d1eaf3a5916</sapi:uuid>
	        <sapi:dataSetVersion>03.00.000</sapi:dataSetVersion>
	        <sapi:name xml:lang="en">FAO (2005)</sapi:name>
	        <sapi:classification name="ilcd">
	            <sapi:class level="0">Databases</sapi:class>
	        </sapi:classification>
	    </s:source>
	    <s:source sapi:sourceId="ACME2" xlink:href="http://lcdn.thinkstep.com/Node/sources/28d74cc0-db8b-4d7e-bc44-5f6d56ce0c4a?version=00.03.001">
	        <sapi:uuid>28d74cc0-db8b-4d7e-bc44-5f6d56ce0c4a</sapi:uuid>
	        <sapi:dataSetVersion>00.03.001</sapi:dataSetVersion>
	        <sapi:name xml:lang="en">GaBi database (all versions)</sapi:name>
	        <sapi:classification name="ilcd">
	            <sapi:class level="0" classId="ca877255-4278-4aae-960e-1f1951367f8d">Databases</sapi:class>
	        </sapi:classification>
	    </s:source>
	</sapi:dataSetList>
