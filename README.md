# cassata-io

## Introduction 
Cassata is a simple, persistent _Event Scheduler_. At the most basic level, it fires a given _Event_, at a given time, to a given URL. An _Event_ is just an arbitrary JSON defined by the user. Cassata can be used as a **Delay Queue** application behind an existing _Kafka_ or _RMQ_ to let them process events after a delay.

Cassata is designed as a Scheduler Service (as opposed to a library), that can be managed and scaled independently of the services/applications using it. 


## Components 
Cassata has three components: Service, Worker and the Datastore. 

**Service** is a Dropwizard based service that accepts requests to schedule/unschedule events via http endpoints and persists them in the datastore. 

**Worker** is a long running Java application that periodically identifies the events that have expired, and fires them to their respective destinations.

**Datastore** is an RDBMS database (currently MySQL and Postgres are supported) that is used to store the event and its metadata. 

## Service API
### Create Event
###### POST
**/cassata/add/** Schedule an Event. The request object is:

```json
{
  "event-id": "def381e4-98a6-4353-8d96-792ceea2bb33",
  "application-id": "order-management-system",
  "event": "{arbitrary event JSON}",
  "expiry": 1524141245,
  "url": "http://urltodestination/",
  "method": "POST",
  "request-headers": [
    "Content-Type: application/json",
    "Header1: value1",
    "Header2: value2"
  ]
}
```

**Application Id**: Name of the application generating the event.

**Event Id**: A unique event Id used for de-duplication. Event Id must be unique within every application. Different applications can share an event ID.

**Event**: An arbitrary JSON payload that is sent to the destination URL at the time of expiry.

**Expiry**: The UNIX timestamp of the time when this event should be emitted by the worker.

**URL**: URL of the destination to where the event will be sent. 

**Http Method (Optional)**: The HTTP method to be used. (Optional, defaults to POST)

**Http Request-Headers (Optional)**: An array of request headers to be sent along with the http request. (Optional. Defaults to “Content-Type: application/json”)

### Delete Event
###### GET
**/cassata/delete/{appId}/{eventId}** Delete the event identified by _appId_ and _eventID_

## Get Status
###### GET
**/cassata/status/{appId}/{eventId}** Get the status of event identified by _appId_ and _eventID_. Possible status are PENDING, PROCESSING, COMPLETED, FAILED.


## Gotchas
Like any other distributed application, Cassata does not (and cannot) guarentee an exactly once delivery. It provides an at least once guarentee of emitting an event. Hence it is the responsibility of the event consumer to manage de-duplication. 

## Coming Soon 

Support for Apache Derby.
