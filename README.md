# srx-services-admin
**Student Record Exchange Admin service.**

Provides message logging and configuration data for all SRX components and services.

## Configuration

### Environment variables
Variable | Description | Example
-------- | ----------- | -------
AES_PASSWORD | Password used to decrypt AES encrypted request body payloads. |  (see Heroku)
AES_SALT | Password used to decrypt AES encrypted request body payloads. | (see Heroku)
AMAZON_S3_ACCESS_KEY | AWS S3 access key for SRX cache. | (see Heroku)
AMAZON_S3_BUCKET_NAME | AWS S3 bucket name for SRX cache. | (see Heroku)
AMAZON_S3_PATH | Root path to files within SRX cache. | (see Heroku)
AMAZON_S3_SECRET | AWS S3 secret for SRX cache. | (see Heroku)
DATASOURCE_CLASS_NAME | Data source class. | org.postgresql.ds.PGSimpleDataSource
DATASOURCE_MAX_CONNECTIONS | Max concurrent connections allowed by this service. | 1
DATASOURCE_TIMEOUT | Database connection timeout (in milliseconds). | 10000
DATASOURCE_URL | Connection string to database. | (see Heroku)
ENVIRONMENT | Deployment environment name.  | development
LOG_LEVEL | Logging level (info, debug, error). | debug
ROLLBAR_ACCESS_TOKEN | Rollbar access token for error logging. | (see Heroku)
ROLLBAR_URL | URL to Rollbar API. | https://api.rollbar.com/api/1/item/
SERVER_API_ROOT | Root path for this service. | (typically leave blank)
SERVER_HOST | Host IP for this service. | 127.0.0.1
SERVER_NAME | Server name for this service. | localhost
SERVER_PORT | Port this service listens on. | 8080
SERVER_URL | URL for this service. | http://localhost
SRX_ENVIRONMENT_URL | HostedZone environment URL. | https://psesd.hostedzone.com/svcs/dev/requestProvider
SRX_SESSION_TOKEN | HostedZone session token assigned to this service. | (see HostedZone configuration)
SRX_SHARED_SECRET | HostedZone shared secret assigned to this service. | (see HostedZone configuration)

### HostedZone
The Admin service (srx-services-admin) must be registered in HostedZone as a new "environment" (application) that provides the following "services" (resources):

 * srxMessages
 * srxZoneConfig

Once registered, the supplied HostedZone session token and shared secret should be set in the srx-services-admin host server (Heroku) environment variables (see above).

This Admin service must be further configured in HostedZone as follows:

Service | Zone | Context | Provide | Query | Create | Update | Delete
------- | ---- | ------- | ------- | ----- | ------ | ------ | ------
srxMessages | default | default | X | X | X | |
srxMessages | test | test | X | X | X | |
srxMessages | test | default | X | | | |
srxMessages | test | district | X | | | |
srxMessages | [district*] | default | X | | | |
srxMessages | [district*] | district | X | | | |
srxZoneConfig | default | default | X | X | | |
srxZoneConfig | test | test | X | X | | |
srxZoneConfig | test | default | X | | | |
srxZoneConfig | test | district | X | | | |
srxZoneConfig | [district*] | default | X | | | |
srxZoneConfig | [district*] | district | X | | | |

[district*] = all district zones utilizing SRX services


## Usage
All SRX services (including Admin itself) depend on the Admin service properly deployed and configured in HostedZone.

This includes:

 * srx-services-admin
 * srx-services-ears
 * srx-services-prs
 * srx-services-sre
 * srx-services-xsre
 * srx-converters-*

Each service that uses srx-shared-core (which includes all those listed above) sends a server start and stop message to Admin.
In addition, each of the above services also utilizes the core SrxMessageService to submit messages to Admin describing usage activity specific to each service. For example: Querying a student xSRE, or updating a student consent form in PRS.

Each service that requires zone-specific configuration also queries the Admin service for this data.

The Core library provides services such as SrxMessageService and ZoneConfig that implement common Admin API requests through the SRX SIF/HostedZone framework for the developer.

### SRX Messages
Messages are created via a POST request using the following URL format:

```
https://[baseUrl]/srxMessages;zoneId=[zoneId];contextId=[contextId]
```

Variable | Description | Example
--------- | ----------- | -------
baseUrl   | URL of the deployment environment hosting the SRX API. |  srx-services-admin-dev.herokuapp.com
zoneId    | Zone of request. | test
contextId | Client context of request. | test

The following required headers must be present in the POST request:

Header | Description | Example
------ | ----------- | -------
authorization | Must be set to a valid HMAC-SHA256 encrypted authorization token. | SIF_HMACSHA256 ZGNlYjgxZmQtNjE5My00NWVkL...
timeStamp | Must be set to a valid date/time in the following format: yyyy-MM-ddTHH:mm:ss:SSSZ | 2016-12-20T18:09:18.539Z

The following optional headers may also be included:

Header | Description | Example
------ | ----------- | -------
generatorId | Identification token of the "generator" of this request or event. | srx-services-xsre
messageId | Consumer-generated. If specified, must be set to a valid UUID. | 54d984e6-3785-4fe5-9d89-7ee5203f087f
messageType | If specified, must be set to: REQUEST | REQUEST
requestAction | If specified, must be set to: CREATE | CREATE
requestId | Consumer-generated. If specified, must be set to a valid UUID. | ba74efac-94c1-42bf-af8b-9b149d067816
requestType | If specified, must be set to: IMMEDIATE | IMMEDIATE
serviceType | If specified, must be set to: OBJECT | OBJECT

#### Example srxMessages POST request
```
POST
https://srx-services-admin-dev.herokuapp.com/srxMessages;zoneId=test;contextId=test

authorization: SIF_HMACSHA256 ZGNlYjgxZmQtNjE5My00NWVkL...
timestamp: 2016-12-20T18:09:18.539Z

<message>
  <messageId>316c8450-8e64-4d06-8862-beb39e9ecd1d</messageId>
  <timestamp>2016-08-09T05:05:02.521Z</timestamp>
  <component>srx-services-prs</component>
  <componentVersion>1.0</componentVersion>
  <resource>district</resource>
  <method>QUERY</method>
  <status>success</status>
  <generatorId>Student Success Link</generatorId>
  <requestId>a528ea6a-cbf5-45d4-9d79-cf775e37991b</requestId>
  <zoneId>test</zoneId>
  <contextId>test</contextId>
  <studentId></studentId>
  <description>QUERY successful for district '1'.</description>
  <uri>https://srx-services-prs-dev.herokuapp.com/districts/1;zoneId=test;contextId=test</uri>
  <userAgent>Apache-HttpClient/4.3.2 (java 1.5)</userAgent>
  <sourceIp>127.0.0.1</sourceIp>
  <headers></headers>
  <body></body>
</message>
```

***
#### Example srxMessage POST response
```
Content-Type: application/xml;charset=UTF-8
Messageid: c91035d5-8aba-49f3-8b77-f64cf5a6a5a8
Messagetype: RESPONSE
Responseaction: CREATE
Responsesource: PROVIDER
Servicetype: OBJECT

<createResponse>
  <creates>
    <create id="316c8450-8e64-4d06-8862-beb39e9ecd1d" advisoryId="1" statusCode="201"/>
  </creates>
</createResponse>
```

### Zone Config
ZoneConfig xml is queried via a GET request using the following URL format:

```
https://[baseUrl]/srxZoneConfig/[zoneId];zoneId=[zoneId];contextId=[contextId]
```

Variable | Description | Example
--------- | ----------- | -------
baseUrl   | URL of the deployment environment hosting the SRX API. |  srx-services-admin-dev.herokuapp.com
zoneId    | Zone of request. | test
contextId | Client context of request. | test

The following required headers must be present in the GET request:

Header | Description | Example
------ | ----------- | -------
authorization | Must be set to a valid HMAC-SHA256 encrypted authorization token. | SIF_HMACSHA256 ZGNlYjgxZmQtNjE5My00NWVkL...
timeStamp | Must be set to a valid date/time in the following format: yyyy-MM-ddTHH:mm:ss:SSSZ | 2016-12-20T18:09:18.539Z

The following optional headers may also be included:

Header | Description | Example
------ | ----------- | -------
generatorId | Identification token of the "generator" of this request or event. | srx-services-xsre
messageId | Consumer-generated. If specified, must be set to a valid UUID. | 54d984e6-3785-4fe5-9d89-7ee5203f087f
messageType | If specified, must be set to: REQUEST | REQUEST
requestAction | If specified, must be set to: QUERY | QUERY
requestId | Consumer-generated. If specified, must be set to a valid UUID. | ba74efac-94c1-42bf-af8b-9b149d067816
requestType | If specified, must be set to: IMMEDIATE | IMMEDIATE
serviceType | If specified, must be set to: OBJECT | OBJECT

#### Example srxZoneConfig GET request
```
GET
https://srx-services-admin-dev.herokuapp.com/srxZoneConfig/test;zoneId=test;contextId=test

authorization: SIF_HMACSHA256 ZGNlYjgxZmQtNjE5My00NWVkL...
timestamp: 2016-12-20T18:09:18.539Z
```

***
#### Example srxZoneConfig GET response
```
Content-Type: application/xml;charset=UTF-8
Messageid: c91035d5-8aba-49f3-8b77-f64cf5a6a5a8
Messagetype: RESPONSE
Responseaction: QUERY

<zone name="test">
  <resource type="sre">
    <destination serviceType="sftp">
      <sftp>
        ...
      </sftp>
    </destination>
  </resource>
  <resource type="xSre">
    <cache>
      ...
    </cache>
    <converter>
      ...
    </converter>
    <schema>
      ...
    </schema>
    <source serviceType="shareFile">
      <shareFile>
        ...
      </shareFile>
    </source>
  </resource>
</zone>
```