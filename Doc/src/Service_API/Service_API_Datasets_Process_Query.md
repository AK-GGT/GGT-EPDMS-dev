---

GET Process Datasets (Query)
============================

Process datasets can be queried for a number of fields that can be specified as request
parameters. All of those parameters specified are regarded as AND conjunction.

Some parameters will work in "contains" match mode, that means the given search string may
be contained somewhere in the field to be searched. For others, the exact value has to be
given (see table below).

Requests
--------

### Syntax

    GET /processes?search=true[&param=value][&param=value]

### Request Parameters

#### Search 

These parameters can be used to search for specific attributes or any combination thereof:

| Name             |Data type|Match mode|Multiple|Sortable| Description                                                |
| :--------------------   |:------- |:---------|:-------|:-------|:--------------------------------------------------------  |
| *name*                  | String  | contains | no     | yes    | Matches name and synonyms fields. May contain multiple terms separated by spaces which will be applied using AND. |
| *description*           | String  | contains | no     | no     | Matches "general comment" and "use advice" field           |
| *location*              | String  | exact    | yes    | yes    | Matches location field, regional code to be given.         |
| *validUntil*            | Year    | >= than  | no     | yes    | Matches the "valid until" date (year). Exact match mode can be forced with a "=" prefix of the argument. |
| *referenceYear*         | Year    | exact    | no     | yes    | Matches the reference year                                 |
| *classes*               | String  | contains | no     | yes    | Matches any name in the dataset's classification           |
| *classId*               | String  | exact    | no     | no     | Matches any class id in the dataset's classification       |
| *subType*               | Enum    | exact    | yes    | yes    | Matches the subtype (only for ILCD+EPD). Accepts values like `GENERIC_DATASET`, `SPECIFIC_DATASET` etc. |
| *type*                  | Enum    | exact    | no     | yes    | Matches the dataset type                                   |
| *parameterized*         | Boolean |          | no     | no     | Matches the parameterized property                         |
| *owner*                 | String  | contains | no     | yes    | Matches the owner of the dataset                           |
| *dataSource*            | String  | exact    | yes    | no     | Matches the datasources (UUID)                             |
| *dataSourceMode*        | String  |          |        |        | Values "OR" (default) or "NOT" - will determine whether result must match any (OR) or none (NOT) given datasource UUIDs |
| *datasetGenerator*      | String  | exact    | yes    | no     | Matches the contacts under dataset generator (UUID)        |
| *datasetGeneratorMode*  | String  |          |        |        | Values "OR" (default) or "NOT" - will determine whether result must match any (OR) or none (NOT) given contact UUIDs |
| *compliance*            | String  | exact    | yes    | yes    | Matches the compliance declarations (UUID)                 |
| *complianceMode*        | String  |          |        |        | Values "OR" (default) or "AND" - will determine whether result must match any (OR) or all (AND) given compliance system UUIDs |
| *metaDataOnly*          | Boolean |          | no     | no     | Matches the metaDataOnly property                          |
| *registrationAuthority* | String  | contains | no     | no     | Matches the reference data set for a registration authority, by matching the description OR the UUID. |
| *registrationNumber*    | String  | contains | no     | no     | Matches the registration number of the data set |
| *tag*                   | String  | exact    | yes    | no     | Matches the tags on a process data set. Default behaviour is 'Process has all tags', other modes can be utilised by parameter *tagMode*.| 
| *tagMode*               | String  |          |        |        | Controls how multiple *tag* parameters will be treated. Possible values for tagMode: *containsAll* (default), *containsAny* and *containsNone*.|

Parameters indicated as multiple occurrence can be given multiple times (which by default corresponds to boolean OR).


#### Sort

For optional sorting of the results, the following parameters can be used:

| Name             |Data type| Description                                                      |
| :------------:   |:------- | :--------------------------------------------------------------  |
| *sortBy*         | String  | Sorts the result by a specific attribute. Value can be the name of any of the parameters from the table above 
where sortable is true.  |
| *sortOrder*      | Boolean | If true, sort ascending, otherwise descending. Defaults to true. |


#### Format

Data can either be returned in XML (default) or JSON formats, which can be controlled by using a request parameter:

| Name             |Values      | Description                                 |
| :------------:   |:---------- | :-----------------------------------------  |
| *format*         | *XML*,JSON | Returns the result in the specified format. |


Responses
---------

### Response Elements

The response returned is a list of process dataset overview objects, 
wrapped in a dataSetList object. See the section ["DataSetList Response
Elements"](./Service_API_Response_DatasetList.md) for a detailed description.


Examples
--------

The following request would yield all datasets that contain the word "foo" in their
name and declare a validity until 2020.

### Sample Request

    GET /processes?search=true&name=foo&validUntil=2020
    

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml
