/*
 * Copyright (c) 2018. cassata.io
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.cassata.service.bootstrap;


import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import io.cassata.commons.models.EventStatus;
import io.cassata.service.http.request.AddEventRequest;
import io.cassata.service.http.response.BasicResponse;
import io.cassata.service.processor.AddEventProcessor;
import io.cassata.service.processor.DeleteEventProcessor;
import io.cassata.service.processor.GetRequestProcessor;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;

@Produces(MediaType.APPLICATION_JSON)
@Path("/cassata/")
@Slf4j
public class CassataServiceResource {

    @Inject
    private AddEventProcessor addEventProcessor;

    @Inject
    private DeleteEventProcessor deleteEventProcessor;

    @Inject
    private GetRequestProcessor getRequestProcessor;

    @POST
    @Path("add/")
    @Timed
    public Response addEvent(AddEventRequest addEventRequest) {

        try {

            //Cheap way to parse the URL.
            //TODO All this validation should be moved to a separte module
            URL endpoint = new URL(addEventRequest.getDestinationUrl());
        } catch (MalformedURLException e) {
            log.error("Unable to parse URL {} for app id: {} event id: {}",
                    addEventRequest.getDestinationUrl(),
                    addEventRequest.getApplication(),
                    addEventRequest.getEventId()
            );

            return Response.status(404)
                    .entity("Unable to parse destination URL")
                    .build();
        }

        try {
            return addEventProcessor.addEvent(addEventRequest);
        } catch (Exception e) {
            log.error("Exception in calling addEVent. ", e);

            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("status/{appId}/{eventId}")
    @Timed
    public Response getStatus(@PathParam("appId") String appId, @PathParam("eventId") String eventId) {
        EventStatus eventStatus = getRequestProcessor.getStatus(appId, eventId);

        if (eventStatus == null) {
            return Response.status(404).build();
        }

        return Response.ok(eventStatus).build();
    }

    @GET
    @Path("event/{appId}/{eventId}")
    @Timed
    public Response getEvent(@PathParam("appId") String appId, @PathParam("eventId") String eventId) {

        return getRequestProcessor.getEvent(appId, eventId);
    }

    @DELETE
    @Timed
    @Path("delete/{appId}/{eventId}")
    public BasicResponse deleteEvent(@PathParam("appId") String appId, @PathParam("eventId") String eventId) {
        return deleteEventProcessor.deleteEvent(appId, eventId);
    }

    @POST
    @Path("_test")
    public void testApi() {
        log.info("Received test request");
    }
}
