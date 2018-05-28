# cassata-io

## Introduction 
Cassata is a Simple, Persistent, Event Scheduler. At the most basic level, it fires a given event, at a given time to a given URL. Event is just an arbitrary JSON defined by the user. 

Cassata is meant to be a simple to use scheduling application that can offload firing of events. It is designed to be a standalone application, that can be managed and scaled independent of the applications using it. 

## Components 
Cassata has three components: Service, Worker and the Datastore. 

Service is a simple Dropwizard based service that accepts requests to schedule/unschedule events and persists them in the datastore. 

Worker is a long running Java application that periodically identifies the events that have expired and fires them to their respective destinations.

Datastore is a RDBM database (currently MySQL and Postgres are supported) that is used to store the event and its metadata. 
 


## It's going to be a piece of Cake.
