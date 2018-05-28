# cassata-io

## Introduction 
Cassata is a Simple, Persistent, Event Scheduler. At the most basic level, it fires a given event, at a given time to a given URL. Event is just an arbitrary JSON defined by the user. 

Cassata is designed to be a standalone application, that can be managed and scaled independently of the services/applications using it. 

## Components 
Cassata has three components: Service, Worker and the Datastore. 

**Service** is a simple Dropwizard based service that accepts requests to schedule/unschedule events and persists them in the datastore. 

**Worker** is a long running Java application that periodically identifies the events that have expired and fires them to their respective destinations.

**Datastore** is a RDBM database (currently MySQL and Postgres are supported) that is used to store the event and its metadata. 

## Service API
###### Create Event
Use the **/cassata/add/** POST API to schedule an event. The request object is:

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
**Event Id**: A unique event Id used for de-duplication. Event Id must be unique within every application. Different applications can share an event ID.

**Application Id**: The application name generating the event.

**Event**: An arbitrary JSON data that is sent to the destination at the time of event expiry.

**Expiry**: The UNIX timestamp of the time when the event expires. 

**URL**: URL of the destination to where the event will be sent. 

**Method (Optional)**: The HTTP method to be used. (Optional, defaults to POST)

**Request-Headers (Optional)**: Request headers to be sent along with the request. (Optional. Defaults to “Content-Type: application/json”)


