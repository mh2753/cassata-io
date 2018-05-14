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


import io.cassata.commons.models.Event;
import io.cassata.service.api.AddEventRequest;
import io.cassata.service.http.response.BasicResponse;
import io.cassata.service.processor.AddEventProcessor;
import io.cassata.service.processor.DeleteEventProcessor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Path("/cassata/")
public class CassataServiceResource {

    private AddEventProcessor addEventProcessor;
    private DeleteEventProcessor deleteEventProcessor;

    public CassataServiceResource(AddEventProcessor addEventProcessor, DeleteEventProcessor deleteEventProcessor) {
        this.addEventProcessor = addEventProcessor;
        this.deleteEventProcessor = deleteEventProcessor;
    }

    @POST
    @Path("add/")
    public void addEvent(AddEventRequest addEventRequest) {

        //TODO return response.
        addEventProcessor.addEvent(addEventRequest);
    }

    @DELETE
    @Path("delete/{appId}/{eventId}")
    public BasicResponse deleteEvent(@PathParam("appId") String appId, @PathParam("eventId") String eventId) {
        return deleteEventProcessor.deleteEvent(appId, eventId);
    }
}
