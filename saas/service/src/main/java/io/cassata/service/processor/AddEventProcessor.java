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

package io.cassata.service.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import io.cassata.commons.dal.EventsTableDao;
import io.cassata.commons.exceptions.CassataException;
import io.cassata.commons.models.Event;
import io.cassata.commons.models.EventStatus;
import io.cassata.service.http.request.AddEventRequest;
import io.cassata.service.http.response.BasicResponse;
import lombok.extern.slf4j.Slf4j;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;

import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.sql.Timestamp;

@Slf4j
public class AddEventProcessor {

    private static final int MYSQL_DUPLICATE_ENTRY_ERROR_CODE = 1062;

    @Inject
    private EventsTableDao eventsTableDao;


    public Response addEvent(AddEventRequest addEventRequest) {

        log.info("Received Add Event request for appId: {}, event Id: {}.", addEventRequest.getApplication(), addEventRequest.getEventId());

        //Build the Event
        Event event = null;
        try {
            event = Event.builder()
                    .eventId(addEventRequest.getEventId())
                    .application(addEventRequest.getApplication())
                    .eventJson(addEventRequest.getEventJson())
                    .httpMethod(addEventRequest.getHttpMethod())
                    .destinationUrl(addEventRequest.getDestinationUrl())
                    .eventStatus(EventStatus.PENDING)
                    .expiry(new Timestamp(addEventRequest.getExpiry()))
                    .headerJson(new ObjectMapper().writeValueAsString(addEventRequest.getHeaders()))
                    .headers(addEventRequest.getHeaders())
                    .build();
        } catch (JsonProcessingException e) {
            throw new CassataException("Internal server error");
        }

        //Insert the event into DB
        try {
            this.eventsTableDao.insertEvent(event);

            log.info("Event added successfully for appId: {}, eventId: {}", event.getApplication(), event.getEventId());
            return Response.ok().build();

        } catch (UnableToExecuteStatementException e)  {

            Response.ResponseBuilder responseBuilder = Response.status(500);
            log.error("Exception in adding event to DB.", e);

            //FIXME Is there a better way of doing this?
            if (e.getCause() instanceof  SQLException) {
                SQLException sqlException = (SQLException) e.getCause();
                if (sqlException.getErrorCode() == MYSQL_DUPLICATE_ENTRY_ERROR_CODE) {
                    log.error("Duplicate entry for appId: {}, eventId: {}", addEventRequest.getApplication(), addEventRequest.getEventId());

                    responseBuilder.status(409);
                    responseBuilder.entity("duplicate entry for appId and eventId combination");
                }
            } else {

                responseBuilder.entity("Internal Server Error while executing the request");
            }

            return responseBuilder.build();
        }

    }
}
