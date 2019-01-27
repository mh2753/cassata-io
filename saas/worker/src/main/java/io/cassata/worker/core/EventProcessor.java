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
import io.cassata.commons.dal.EventlogTableDao;
import io.cassata.commons.dal.EventsTableDao;
import io.cassata.commons.http.HttpRequestWrapper;
import io.cassata.commons.http.HttpResponse;
import io.cassata.commons.models.Event;
import io.cassata.commons.models.EventLog;
import io.cassata.commons.models.EventStatus;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.ConnectException;
import java.util.concurrent.Callable;

@Slf4j
@Setter
public class EventProcessor implements Callable<Void> {

    private Event event;
    private int retryCount;
    private EventlogTableDao eventlogTableDao;
    private EventsTableDao eventsTableDao;

    private boolean logFailedRequests;

    private Meter numRequests;
    private Meter failedRequests;
    private Meter completedRequests;

    public Void call() throws Exception {

        log.info("Processing App Id: {}, Event Id: {}", event.getApplication(), event.getEventId());
        numRequests.mark();

        int waitTime = 500;

        int retries = 0;
        while (++retries < retryCount) {
            try {
                HttpRequestWrapper requestWrapper = new HttpRequestWrapper.Builder(event.getDestinationUrl())
                        .withRequestType(event.getHttpMethod())
                        .withHeaders(event.getHeaderMap())
                        .build();

                HttpResponse response = requestWrapper.execute(event.getEventJson());

                if (response.getResponseCode() < 300) {

                    log.info("App Id: {}, Event Id: {}. Request completed successfully. Marking event completed.",
                            event.getApplication(),
                            event.getEventId());

                    eventsTableDao.updateEventStatus(event.getId(), EventStatus.COMPLETED);
                    completedRequests.mark();
                    return (null);

                }
                if (response.getResponseCode() >= 500) {

                    log.warn("App Id: {}, Event Id: {}. Received error response from service: {}. Retrying.",
                            event.getApplication(),
                            event.getEventId(),
                            response.getResponseString());

                    createEventLog(response);

                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        log.warn("Sleep interrupted", e);
                    }

                    waitTime *= 2;

                } else {

                    log.error("App Id: {}, Event Id: {}. Irrecoverable error in service. Marking as failed in DB. Response Code: {}, Response Body: {}",
                            event.getApplication(),
                            event.getEventId(),
                            response.getResponseCode(),
                            response.getResponseString());

                    createEventLog(response);

                    eventsTableDao.updateEventStatus(event.getId(), EventStatus.FAILED);
                    failedRequests.mark();
                    return (null);
                }
            } catch (ConnectException e) {

                log.error("App Id: " + event.getApplication() + ". Event Id: " + event.getEventId() + ".Connect Exception in calling service.", e);

                eventsTableDao.updateEventStatus(event.getId(), EventStatus.SERVICE_UNAVAILABLE);

                failedRequests.mark();
                return (null);
            } catch (Exception e) {
                log.error("Exception in calling service. Marking request as failed", e);
                eventsTableDao.updateEventStatus(event.getId(), EventStatus.FAILED);
                failedRequests.mark();
                return (null);
            }
        }

        log.error("App Id: {}, Event Id: {}. Unable to execute event after {} retries. Marking the event as failed in DB",
                event.getApplication(),
                event.getEventId(),
                retryCount, event.getId());

        eventsTableDao.updateEventStatus(event.getId(), EventStatus.FAILED);
        failedRequests.mark();
        return (null);
    }

    private void createEventLog(HttpResponse httpResponse) {

        if (this.logFailedRequests) {
            log.info("Log failed requests set to true. Logging request for app id: {} event id: {} to database",
                    this.event.getApplication(),
                    this.event.getEventId());

            EventLog eventLog = EventLog.builder()
                    .saasEventsId(this.event.getId())
                    .httpResponseCode(httpResponse.getResponseCode())
                    .httpResponse(httpResponse.getResponseString())
                    .build();

            eventlogTableDao.insertEventLog(eventLog);
        }
    }

}
