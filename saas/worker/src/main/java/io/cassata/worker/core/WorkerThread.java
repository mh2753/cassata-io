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

import java.util.List;

public class WorkerThread implements Runnable {

    private EventsTableDao eventsTableDao;

    public WorkerThread(EventsTableDao eventsTableDao) {
        this.eventsTableDao = eventsTableDao;
    }

    public void run() {

        List<Event> eventList = eventsTableDao.fetchAndLockEventsToProcess(2);

        for (Event event: eventList) {
            try {
                HttpRequestWrapper requestWrapper = new HttpRequestWrapper.Builder(event.getDestinationUrl())
                        .withRequestType(event.getHttpMethod())
                        .withHeaders(event.getHeaderMap())
                        .build();

                HttpResponse response = requestWrapper.execute(event.getEventJson());

                if (response.getResponseCode() < 300) {
                    eventsTableDao.updateEventStatus(event.getId(), EventStatus.COMPLETED);
                } else {
                    eventsTableDao.updateEventStatus(event.getId(), EventStatus.FAILED);
                }
            } catch (Exception e) {
                eventsTableDao.updateEventStatus(event.getId(), EventStatus.FAILED);
                e.printStackTrace();
            }
        }
    }
}
