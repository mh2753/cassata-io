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

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import io.cassata.commons.dal.EventsTableDao;
import io.cassata.commons.http.HttpRequestType;
import io.cassata.commons.models.Event;
import io.cassata.commons.models.EventStatus;
import io.cassata.service.api.AddEventRequest;
import io.cassata.service.http.response.BasicResponse;
import lombok.extern.slf4j.Slf4j;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;

import java.sql.SQLException;
import java.sql.Timestamp;

@Slf4j
public class AddEventProcessor {

    private static final int MYSQL_DUPLICATE_ENTRY_ERROR_CODE = 1062;
    private EventsTableDao eventsTableDao;

    public AddEventProcessor(EventsTableDao eventsTableDao) {
        this.eventsTableDao = eventsTableDao;
    }

    public BasicResponse addEvent(AddEventRequest addEventRequest) {

        log.info("Received Add Event request for appId: {}, event Id: {}.", addEventRequest.getApplication(), addEventRequest.getEventId());

        BasicResponse response = new BasicResponse();

        //Build the Event
        Event event = Event.builder()
                .eventId(addEventRequest.getEventId())
                .application(addEventRequest.getApplication())
                .eventJson(addEventRequest.getEventJson())
                .httpMethod(addEventRequest.getHttpMethod())
                .destinationUrl(addEventRequest.getDestinationUrl())
                .eventStatus(EventStatus.PENDING)
                .expiry(new Timestamp(addEventRequest.getExpiry()))
                .headers(addEventRequest.getHeaders())
                .build();

        //Insert the event into DB
        try {
            this.eventsTableDao.insertEvent(event);

            log.info("Event added successfully for appId: {}, eventId: {}", event.getApplication(), event.getEventId());
            response.setStatus(BasicResponse.StatusCode.ok);

        } catch (UnableToExecuteStatementException e)  {

            response.setStatus(BasicResponse.StatusCode.failed);
            response.setMessage("Internal Server Error while executing the request");

            //FIXME Is there a better way of doing this?
            if (e.getCause() instanceof  SQLException) {
                SQLException sqlException = (SQLException) e.getCause();
                if (sqlException.getErrorCode() == MYSQL_DUPLICATE_ENTRY_ERROR_CODE) {
                    log.error("Duplicate entry for appId: {}, eventId: {}", addEventRequest.getApplication(), addEventRequest.getEventId());

                    response.setMessage("duplicate entry for appId and eventId combination");
                }
            }
        }

        return response;
    }
}
