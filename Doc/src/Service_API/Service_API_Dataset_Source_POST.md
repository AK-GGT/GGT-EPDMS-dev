---

POST Source Dataset with binary attachments
===========================================

Basic POST operation to import one source dataset along with its binary
attachments into the database.

Requests
--------

### Form Parameters

| Name                | Type                | Description                                                   |
| :-----------------: | :-----------------: | :------------------------------------------------------------ |
| *file*              | File (input stream) | The file (usually an input stream) of the source dataset.                  |
| *stock (optional)*  | String              | The UUID of the root data stock to store the dataset(s) in. If omitted, the default root data stock will be used.  |
| *{the file name}*   | File (input stream) | The file (usually an input stream) of the binary attachment. NOTE: This may only contain ASCII characters. Ideally, use only letters, numbers, and the characters -_#+=. |

Syntax
------

    POST /sources/withBinaries

The content can be POSTed as `multipart/form-data`. The UUID of the data
stock the data is to be stored in on the server side may be transmitted
as either option form parameter or optional header parameter.

The binary attachments can be provided as InputStreams wrapped in
MultiPart fields, where the MultiPart field name is the case-sensitive
name of the file without path (e.g. "recycling.png") and its
contents is the InputStream.

Responses
---------

If the import was successful, an HTTP 200 status code is returned along
with a single datastock object that represents the datastock the
imported dataset(s) has/have been stored in.

### Response Elements

See [Datastock Response Elements](./Service_API_Response_Datastocks.md).

Examples
--------

### Sample Request

    POST /sources/withBinaries

    multipart/form-data
    stock=7c6fdb08-902e-48de-bb94-9600bf317331
    file=@efd832cd-8bb2-4c66-911f-4e67c9dd7e3e.xml
    review_report.pdf=@review_report.pdf


### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/xml

    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <dataStock xmlns="http://www.ilcd-network.org/ILCD/ServiceAPI" root="true">
        <uuid>7c6fdb08-902e-48de-bb94-9600bf317331</uuid>
        <shortName>Foo</shortName>
        <name xml:lang="en">Foo root data stock</name>
    </dataStock>
