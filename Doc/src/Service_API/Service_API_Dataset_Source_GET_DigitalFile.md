---

GET Digital File of a Source Dataset
====================================

GET operation to retrieve the digital file attachment for a specific
source dataset.

Requests
--------

### Syntax

    GET /sources/{uuid}/{filename}

The filename must be URL encoded.

    GET /sources/{uuid}/digitalfile

The latter variant will retrieve the first digital file entry for the
specified dataset.

### Request Parameters

none

Responses
---------

The digital file.

The returned MIME type will be `image/*` for images and
`application/pdf` for files that carry a `.pdf` extension.

Examples
--------

### Sample Request

    GET /sources/0a34866e-ce75-48c8-82e6-0080739e7154/100512%20System%20boundaries%20diagram%20-%20with%20reuse.jpg

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: image/jpg

(the image)

### Sample Request

    GET /sources/cb1c5d4a-50ed-4d7b-828b-6fcd560ee17b/digitalfile

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: application/pdf

(the PDF document)
