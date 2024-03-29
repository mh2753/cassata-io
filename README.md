# cassata-io

## Introduction 
Cassata is a simple, light-weight, persistent, multi-tenant _**HTTP Request Scheduler**_ as a service. Essentially, it allows an application to schedule an HTTP request (called an _event_) to be sent in the future to a specified URL.


## Components 
The scheduler is made up of a _Service_ that accepts requets from clients, a _Datastore_ (currently MySQL) that persists these requests and a _Worker_ that processes expired events. All these components can be run and scaled independently of each other. 

**Service** is a Dropwizard based service that accepts requests to schedule/unschedule events via http endpoints and persists them in the datastore. (See API definition below)

**Worker** is a long running Java application that periodically identifies the events that have expired, and fires them to their respective destinations. Clients can provide the HTTP method and optional headers that need to be sent with this request. (See API description below). In case of http failures in calling the destination URL, error codes and status messages can also be persisted to the datastore for diagnosis. 

**Datastore** is a database (currently MySQL is supported) that is used to store the event and its metadata.

## Service API
### Create Event
###### POST
**/cassata/add/** Schedule an Event. The request object is:

```json
{
  "eventId": "def381e4-98a6-4353-8d96-792ceea2bb33",
  "application": "order-management-system",
  "eventJson": "{}",
  "expiry": 1724141245,
  "destinationUrl": "http://urltodestination/",
  "httpMethod": "POST",
  "headers": [
    "Content-Type: application/json",
    "Header1: value1",
    "Header2: value2"
  ]
}
```
**Event Id**: A unique event Id used for de-duplication. Event Id must be unique within every application. Different applications can share an event ID.

**Application Id**: Name of the application generating the event.

**Event**: This is the body of the HTTP request that will be sent to the destination URL. Can be any arbitrary JSON string.

**Expiry**: The UNIX timestamp of the time when this event should be emitted by the worker.

**URL**: URL of the destination to where the event will be sent. 

**Http Method (Optional)**: The HTTP method to be used. (Optional, defaults to POST)

**Http Request-Headers (Optional)**: An array of request headers to be sent along with the http request. (Optional. Defaults to “Content-Type: application/json”)

### Delete Event
###### GET
**/cassata/delete/{appId}/{eventId}** Delete the event identified by _appId_ and _eventID_

## Get Status
###### GET
**/cassata/status/{appId}/{eventId}** Get the status of event identified by _appId_ and _eventID_. Possible status are PENDING, PROCESSING, COMPLETED, SERVICE_UNAVAILABLE, FAILED.

## Setup 

### Download
Download the lastest binary for Linux and Mac OS [here](https://github.com/mh2753/cassata-io/releases/download/v1.0/cassata-1.0-bin.tar.gz) and unzip it. The same binary has the worker and service applications.

### Table Setup 
Cassata needs the _events_ (\_saas_events) and the _eventlog_ (\_saas_eventlog) tables in the Datastore. You can create them with SQLs statements [here](https://github.com/mh2753/cassata-io/tree/master/saas/service/src/main/resources/sql).
Alternatively, you can start the service with _createTablesIfNotExists_ as _true_ and the tables will be created by the service. Note that this will require your DB user to have DDL permissions.

### Configurations

Edit the Service and/or Worker configurations in `$CASSATA_HOME/config` folder. 

### Start Service/Worker
Go to `$CASSATA_HOME/bin` folder. 

Start service with `./cassata service start`

Start Worker with `./cassata worker start`

The Service and Worker do not have to be started on the same host (they just need to be connected to the same Database to co-ordinate). Also, you have have multiple instances of Service and Worker running for scalability.

## Configuration 
### Service Configuration

The Service is a regular dropwizard service and the bulk of config is just regular dropwizard config. Refer https://www.dropwizard.io/en/latest/manual/configuration.html for details. 

Cassata specific service config is nested under _service_ in the config file

| Config        | Default           | Explanation  |
| ------------- |:-------------:| -----|
| createTablesIfNotExists      | false      |   Create Tables in DBMS if they don't already exist. (Note that this will require your DB user to have DDL permissions. |

### Worker Configuration 

Cassata specific worker configurations are nested under _workerThreadProperties_ in the config file. They are explained below. Database configurations are nested under _database_ and these are regular JDBI configurations (refer: https://www.dropwizard.io/en/latest/manual/configuration.html) 

| Config        | Default           | Explanation  |
| ------------- |:-------------:| -----|
| numWorkerThreads      | 5 | Number of worker threads polling for expired events. |
| workerThreadPollingIntervalInMillis      | 10000      |   Number of milli-seconds a worker thread sleeps between each poll of the Datastore  |
| numEventsProcessedPerTransaction      | 5      |   Number of expired events picked up by a Worker thread for processing.  |
| httpRetryCount      | 5      |   Number of retries to the destination URL (in case of Connection Error or 5XX error) before marking the event as failure. |
| logFailedRequests      | false      |   Log the http code and message of a failed request in a separate table for diagnosis |


## Gotchas
Like any other distributed application, Cassata does not guarentee an exactly once delivery. It provides an at least once guarentee of emitting an event.  It is the responsibility of the event consumer to manage de-duplication. 

## Coming Soon 

Support for Postgres and Elastic Search as data sources. 
