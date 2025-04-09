---

GET Category System Definition
==============================

GET operation that returns the timestamp when the category system definition was updated for the last time.

The date/time format returned can be customized with patterns, the default format is "DAY.MONTH.YEAR".

Requests
--------

### Syntax

    GET /categorySystems/{categorySystem-name}/lastModified

### Request Parameters

| Name             |Values             | Description                                 |
| :------------:   |:----------------- | :-----------------------------------------  |
| *format*         | datetime pattern  | Formats the result in the specified pattern. Default is `dd.MM.YYYY`. Patterns are described [here](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html). |


Responses
---------

### Response Elements

A date (and/or time) according to the pattern given.

Examples
--------

### Sample Request

    GET /categorySystems/ILCD/lastModified

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: text/plain

    10.09.2018


### Sample Request

    GET /categorySystems/ILCD/lastModified?format=YYYY-MM-dd_HH:mm 

### Sample Response

    HTTP/1.1 200 OK
    Content-Type: text/plain

    2018-09-10_14:27

