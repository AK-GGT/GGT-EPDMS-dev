---

Authentication : Session-Based
==============

Login
-----

In order to perform certain operations, authentication may be required.
This GET operation performs authentication against the application,
setting a session cookie if successful.

### Requests

#### Syntax

    GET /authenticate/login

#### Request Parameters

| Name           | Description                                               |
| :------------: | :-------------------------------------------------------- |
| *userName*     | The username to authenticate with                         |
|                | Type: String                                              |
|                | Default: None                                             |
| *password*     | The password to authenticate with                         |
|                | Type: String                                              |
|                | Default: None                                             |

### Responses

#### Response Elements

None

### Examples

#### Sample Request

    GET /authenticate/login?userName=foo&password=bar

#### Sample Response if Login Successful

    HTTP/1.1 200 OK
    Content-Type: text/plain

    Login successful

#### Sample Response if Already Logged In

    HTTP/1.1 200 OK
    Content-Type: text/plain

    You are already logged in as a user

#### Sample Response if Wrong User Name or Password

    HTTP/1.1 200 OK
    Content-Type: text/plain

    incorrect password or user name

#### Sample Response if User Name or Password Missing in Request

    HTTP/1.1 200 OK
    Content-Type: text/plain

    user name and password must have a value

Logout
------

With this GET operation, the session for a currently authenticated user
can be closed.

### Requests

#### Syntax

    GET /authenticate/logout

#### Request Parameters

none

### Responses

#### Response Elements

None.

### Examples

#### Sample Request

    GET /authenticate/logout

#### Sample Response if authenticated

    HTTP/1.1 200 OK
    Content-Type: text/plain

    successfully logged out

#### Sample Response if not authenticated

    HTTP/1.1 200 OK
    Content-Type: text/plain

    currently not authenticated

Status
------

With this GET operation, the current authentication status
(authenticated or not) can be retrieved, as well the user name and any
associated roles if authenticated. Both global roles as well as 
per-datastock permissions are shown.

### Requests

#### Syntax

    GET /authenticate/status

#### Request Parameters

none

### Responses

#### Response Elements

| Name           | Description                                               |
| :------------: | :-------------------------------------------------------- |
| *authInfo*     | Contains the elements that describe the authentication status. |
|                | Type: Container                                           |
|                | Ancestors: None                                           |
| *authenticated* | Indicates whether the session is currently authenticated or not. |
|                | Type: Boolean                                             |
|                | Ancestors: authInfo                                       |
| *userName*     | The username for the current session                      |
|                | Type: String                                              |
|                | Ancestors: authInfo                                       |
| *role*         | One entry for each role associated for the current session |
|                | Type: String                                              |
|                | may occur multiple times                                  |
|                | Ancestors: authInfo                                       |

### Examples

#### Sample Request

    GET /authenticate/status

#### Sample Response if authenticated

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<authInfo xmlns="http://www.ilcd-network.org/ILCD/ServiceAPI">
    <authenticated>true</authenticated>
    <userName>admin</userName>
    <role>ADMIN</role>
    <role>SUPER_ADMIN</role>
</authInfo>
~~~~


#### Sample Response if authenticated with permissions on datastocks

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<authInfo xmlns="http://www.ilcd-network.org/ILCD/ServiceAPI">
    <authenticated>true</authenticated>
    <userName>foobar</userName>
    <dataStock root="true">
		<uuid>42217b74-22ba-4bda-78b6-faa412d0ac9f</uuid>
		<shortName>PRIVATE_DATASTOCK</shortName>
		<role>READ</role>
		<role>EXPORT</role>
		<role>WRITE</role>
		<role>IMPORT</role>
		<role>CHECKIN</role>
		<role>CHECKOUT</role>
		<role>RELEASE</role>
		<role>CREATE</role>
		<role>DELETE</role>
	</dataStock>
	<dataStock root="false">
		<uuid>2996a429-b1ec-4b3b-a654-0c3a02afaa3d</uuid>
		<shortName>PUBLIC</shortName>
		<role>READ</role>
		<role>EXPORT</role>
	</dataStock>
</authInfo>
~~~~


#### Sample Response if not authenticated

    HTTP/1.1 200 OK
    Content-Type: application/xml

~~~~ {.myxml}
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<authInfo xmlns="http://www.ilcd-network.org/ILCD/ServiceAPI">
    <authenticated>false</authenticated>
</authInfo>
~~~~
