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

import io.cassata.commons.dal.EventsTableDao;
import io.cassata.commons.http.HttpRequestType;
import io.cassata.commons.models.Event;
import io.cassata.commons.models.EventStatus;
import io.cassata.service.api.AddEventRequest;

import java.sql.Timestamp;

public class AddEventProcessor {

    private EventsTableDao eventsTableDao;

    public AddEventProcessor(EventsTableDao eventsTableDao) {
        this.eventsTableDao = eventsTableDao;
    }

    public void addEvent(AddEventRequest addEventRequest) {

        Event event = Event.builder()
                .eventId(addEventRequest.getEventId())
                .application(addEventRequest.getApplication())
                .eventJson(addEventRequest.getEventJson())
                .httpMethod(HttpRequestType.POST) //FIXME get the real one
                .destinationUrl(addEventRequest.getDestinationUrl())
                .eventStatus(EventStatus.PENDING)
                .expiry(new Timestamp(addEventRequest.getExpiry()))
                .headers(addEventRequest.getHeaders())
                .build();

        this.eventsTableDao.insertEvent(event);
    }
}
