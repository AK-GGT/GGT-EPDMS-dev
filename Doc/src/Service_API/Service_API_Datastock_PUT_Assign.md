---

Assign (PUT) Datasets to a Logical Data Stock
==============================


PUT operations to assign datasets to a specific data stock. Therefore the assigner 
has to authenticate either by logging in or via Authentication token (see ["Chapter Authentication : 
Token-Based"](./Service_API_Authentication_Token.md) for more information).
 Applies to all dataset types.

Requests
--------

### Syntax

    PUT /datastocks/{datastock-uuid}/processes/{dataset-uuid}[?version={version}&withDependencies={dependenciesmode}]
    PUT /datastocks/{datastock-uuid}/flows/{dataset-uuid}[?version={version}&withDependencies={dependenciesmode}]
    PUT /datastocks/{datastock-uuid}/flowproperties/{dataset-uuid}[?version={version}&withDependencies={dependenciesmode}]
    PUT /datastocks/{datastock-uuid}/unitgroups/{dataset-uuid}[?version={version}&withDependencies={dependenciesmode}]
    PUT /datastocks/{datastock-uuid}/sources/{dataset-uuid}[?version={version}&withDependencies={dependenciesmode}]
    PUT /datastocks/{datastock-uuid}/contacts/{dataset-uuid}[?version={version}&withDependencies={dependenciesmode}]
    PUT /datastocks/{datastock-uuid}/lciamethods/{dataset-uuid}[?version={version}&withDependencies={dependenciesmode}]
                    

### Request Parameters

The request parameters are

| Name                  | Description                                               |
| :-------------------: | :-------------------------------------------------------- |
| *withDependencies*    | The dependencies mode of the data set                     |
|                       | The dependencies modes are:                               |
|                       |  - 0: none (selected entries only)                        |
|                       |  - 1: including reference flow(s)                         |
|                       |  - 2: all from same data stock                            |
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

The response returned is a message as a String reporting whether dataset could be assigned or reporting what went wrong during assignment.

Examples
--------

### Sample Request

    PUT
                        /datastocks/aca74e60-146e-11e2-892e-0800200c9a66/processes/3fc467e6-280d-4de0-a426-a036b6a30c99?withDependencies=3&version=03.000.000
                    

### Sample Response if (Only) Dataset Could Be Assigned to Data Stock

    HTTP/1.1 200 OK
                        Content-Type: application/xml
                    
    Dataset has been assigned to data stock.
    
### Sample Response if Dataset With Dependencies Could Be Assigned to Data Stock

    HTTP/1.1 200 OK
                        Content-Type: application/xml
                    
    Dataset with its dependencies has been assigned to data stock.

### Sample Response if Dataset Is Already Assigned to Data Stock

    HTTP/1.1 200 OK
                        Content-Type: application/xml
                    
    Dataset is already assigned to data stock.
    
### Sample Response if Data Stock is not correctly set

    HTTP/1.1 401 UNAUTHORIZED
                        Content-Type: application/xml
                    
    Data stock is not correctly set.
    
### Sample Response if Data Stock is no logical data stock

    HTTP/1.1 401 UNAUTHORIZED
                        Content-Type: application/xml
                    
    Data stock must be a logical data stock.
    
### Sample Response if User Is Not Permitted to Assign Data Set to Specified Data Stock

    HTTP/1.1 401 UNAUTHORIZED
                        Content-Type: application/xml
                    
    You are not permitted to assign datasets to this data stock.
    
### Sample Response if Dataset UUID and/or version Is Not Correctly Set

    HTTP/1.1 401 UNAUTHORIZED
                        Content-Type: application/xml
                    
    Dataset with given UUID and/or version is not existing in database.
    
### Sample Response if Dataset UUID is not correctly set

    HTTP/1.1 401 UNAUTHORIZED
                        Content-Type: application/xml
                    
    Dataset with given UUID is not existing in database.
    
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
                    
    Dataset could not be assigned to data stock.
        
~~~~
