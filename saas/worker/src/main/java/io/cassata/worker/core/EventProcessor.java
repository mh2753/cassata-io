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

package io.cassata.worker.core;

import io.cassata.commons.dal.EventsTableDao;
import io.cassata.commons.http.HttpRequestWrapper;
import io.cassata.commons.http.HttpResponse;
import io.cassata.commons.models.Event;
import io.cassata.commons.models.EventStatus;

import java.util.concurrent.Callable;

public class EventProcessor implements Callable<Void> {

    private Event event;
    private int retryCount;
    private EventsTableDao eventsTableDao;

    public EventProcessor(Event event, int retryCount, EventsTableDao eventsTableDao) {
        this.event = event;
        this.retryCount = retryCount;
        this.eventsTableDao = eventsTableDao;
    }

    public Void call() throws Exception {

        int retries = 0;
        while (++retries <= retryCount) {
            try {
                HttpRequestWrapper requestWrapper = new HttpRequestWrapper.Builder(event.getDestinationUrl())
                        .withRequestType(event.getHttpMethod())
                        .withHeaders(event.getHeaderMap())
                        .build();

                HttpResponse response = requestWrapper.execute(event.getEventJson());

                if (response.getResponseCode() < 300) {
                    event.setEventStatus(EventStatus.COMPLETED);
                    return (null);
                } if (response.getResponseCode() >= 500) {
                    //Service error, retry
                } else {
                    event.setEventStatus(EventStatus.FAILED);
                    return (null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                event.setEventStatus(EventStatus.FAILED);
                return (null);
            }
        }

        event.setEventStatus(EventStatus.FAILED);
        return (null);
    }

}
