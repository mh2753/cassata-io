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

import com.codahale.metrics.Meter;
import io.cassata.commons.dal.EventsTableDao;
import io.cassata.commons.http.HttpRequestWrapper;
import io.cassata.commons.http.HttpResponse;
import io.cassata.commons.models.Event;
import io.cassata.commons.models.EventStatus;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
@Setter
public class EventProcessor implements Callable<Void> {

    private Event event;
    private int retryCount;
    private EventsTableDao eventsTableDao;

    private Meter numRequests;
    private Meter failedRequests;
    private Meter completedRequests;

    public Void call() throws Exception {

        log.info("Processing Event with id: {}, App Id: {}, Event Id: {}", event.getId(), event.getApplication(), event.getEventId());
        numRequests.mark();

        int retries = 0;
        while (++retries <= retryCount) {
            try {
                HttpRequestWrapper requestWrapper = new HttpRequestWrapper.Builder(event.getDestinationUrl())
                        .withRequestType(event.getHttpMethod())
                        .withHeaders(event.getHeaderMap())
                        .build();

                HttpResponse response = requestWrapper.execute(event.getEventJson());

                if (response.getResponseCode() < 300) {

                    log.info("Request completed successfully. Marking event Id: {} to completed.", event.getId());
                    eventsTableDao.updateEventStatus(event.getId(), EventStatus.COMPLETED);
                    completedRequests.mark();
                    return (null);

                } if (response.getResponseCode() >= 500) {

                    //Service error, retry
                    //FIXME Add exponential backoff
                    log.warn("Received error response from service: {}. Retrying.", response.getResponseString());
                } else {

                    log.error("Irrecoverable error in executing the event id: {}. Marking as failed in DB", event.getId());
                    eventsTableDao.updateEventStatus(event.getId(), EventStatus.FAILED);
                    failedRequests.mark();
                    return (null);
                }
            } catch (Exception e) {
                log.error("Exception in calling service. Marking event as failed in DB", e);
                eventsTableDao.updateEventStatus(event.getId(), EventStatus.FAILED);
                failedRequests.mark();
                return (null);
            }
        }

        log.error("Unable to execute event after {} retries. Marking the event with Id: {} as failed in DB", retries, event.getId());
        eventsTableDao.updateEventStatus(event.getId(), EventStatus.FAILED);
        failedRequests.mark();
        return (null);
    }

}
