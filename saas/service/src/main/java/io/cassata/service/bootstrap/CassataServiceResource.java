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
import io.cassata.commons.models.Event;
import io.cassata.service.api.AddEventRequest;
import io.cassata.service.http.response.BasicResponse;
import io.cassata.service.processor.AddEventProcessor;
import io.cassata.service.processor.DeleteEventProcessor;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Path("/cassata/")
@Slf4j
public class CassataServiceResource {

    @Inject
    private AddEventProcessor addEventProcessor;

    @Inject
    private DeleteEventProcessor deleteEventProcessor;

    @POST
    @Path("add/")
    @Timed
    public BasicResponse addEvent(AddEventRequest addEventRequest) {

        return addEventProcessor.addEvent(addEventRequest);
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
