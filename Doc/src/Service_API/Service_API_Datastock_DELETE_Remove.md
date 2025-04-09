---

Remove Assigned (DELETE) Datasets from a Logical Data Stock
==============================


PUT operations to remove assigned datasets from a specific logical data stock. Therefore the operator 
has to authenticate either by logging in or via Authentication token (see ["Chapter Authentication : 
Token-Based"](./Service_API_Authentication_Token.md) for more information).
 Applies to all dataset types.

Requests
--------

### Syntax

    DELETE /datastocks/{datastock-uuid}/processes/{dataset-uuid}[?version={version}&withDependencies={dependenciesmode}]
    DELETE /datastocks/{datastock-uuid}/flows/{dataset-uuid}[?version={version}&withDependencies={dependenciesmode}]
    DELETE /datastocks/{datastock-uuid}/flowproperties/{dataset-uuid}[?version={version}&withDependencies={dependenciesmode}]
    DELETE /datastocks/{datastock-uuid}/unitgroups/{dataset-uuid}[?version={version}&withDependencies={dependenciesmode}]
    DELETE /datastocks/{datastock-uuid}/sources/{dataset-uuid}[?version={version}&withDependencies={dependenciesmode}]
    DELETE /datastocks/{datastock-uuid}/contacts/{dataset-uuid}[?version={version}&withDependencies={dependenciesmode}]
    DELETE /datastocks/{datastock-uuid}/lciamethods/{dataset-uuid}[?version={version}&withDependencies={dependenciesmode}]
                    

### Request Parameters

The request parameters are

| Name                  | Description                                               |
| :-------------------: | :-------------------------------------------------------- |
| *withDependencies*    | The dependencies mode of the data set                     |
|                       | The dependencies modes are:                               |
|                       |  - 0: none (selected entries only)                        |
|                       |  - 1: including reference flow(s)                         |
|                       |  - 2: all from same root data stock                            |
|                       |  - 3: all                                                 |
|                       | Optional                                                  |
|                       | Values: 0,1,2,3                                           |
|                       | Default: None                                             |
| *version*             | The version of the dataset.                               |
|                       | Optional                                                  |
|                       | Values: &ast;&ast;.&ast;&ast;.&ast;&ast;&ast;                                        |
|                       | Default: None                                             |

Responses
---------

### Response Elements

The response returned is a message as a String reporting whether assigned dataset could be removed from data stock or reporting what went wrong during removal.

Examples
--------

### Sample Request

    DELETE
                        /datastocks/aca74e60-146e-11e2-892e-0800200c9a66/processes/3fc467e6-280d-4de0-a426-a036b6a30c99?withDependencies=3&version=03.000.000
                    

### Sample Response if (Only) Assigned Dataset Could Be Removed From Data Stock

    HTTP/1.1 200 OK
                        Content-Type: application/xml
                    
    Dataset has been removed from data stock.
    
### Sample Response if Assigned Dataset With Dependencies Could Be Removed From Data Stock

    HTTP/1.1 200 OK
                        Content-Type: application/xml
                    
    Dataset with its dependencies has been removed from data stock.
    
### Sample Response if Data Stock is not correctly set

    HTTP/1.1 401 UNAUTHORIZED
                        Content-Type: application/xml
                    
    Data stock is not correctly set.
    
### Sample Response if Data Stock is no logical data stock

    HTTP/1.1 401 UNAUTHORIZED
                        Content-Type: application/xml
                    
    Data stock must be a logical data stock.
    
### Sample Response if User Is Not Permitted to Operate On Specified Data Stock

    HTTP/1.1 401 UNAUTHORIZED
                        Content-Type: application/xml
                    
    You are not permitted to operate on this data stock.
    
### Sample Response if Dataset UUID And Version Is Not Assigned to Data Stock

    HTTP/1.1 200 OK
                        Content-Type: application/xml
                    
    Dataset with given UUID and version is not assigned to data stock.
    
### Sample Response if Dataset UUID is Not Assigned to Data Stock

    HTTP/1.1 401 UNAUTHORIZED
                        Content-Type: application/xml
                    
    Dataset with given UUID is not assigned to data stock.
    
### Sample Response if Version Is Not In A Correct Format

    HTTP/1.1 401 UNAUTHORIZED
                        Content-Type: application/xml
                    
    Version is not in a correct format.
    
### Sample Response if Dependency Mode is not correctly set

    HTTP/1.1 401 UNAUTHORIZED
                        Content-Type: application/xml
                    
    Dependencies mode is not legal.
    
### Sample Response if An Unexpected Error Occured During Assigning

    HTTP/1.1 500 INTERNAL SERVER ERROR
                        Content-Type: application/xml
                    
    Dataset could not be removed from data stock.
        
~~~~
