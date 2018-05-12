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
import oracle.jrockit.jfr.openmbean.EventSettingType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class WorkerThread implements Runnable {

    private EventsTableDao eventsTableDao;
    private int batchSize = 5;
    private ExecutorService executorService;

    public WorkerThread(EventsTableDao eventsTableDao) {
        this.eventsTableDao = eventsTableDao;
        this.executorService = Executors.newFixedThreadPool(batchSize);
    }

    public void run() {

        List<Event> eventList = eventsTableDao.fetchAndLockEventsToProcess(batchSize);

        while (eventList.size() > 0) {
            List<Callable<Void>> eventProcessors = new ArrayList<Callable<Void>>();
            for (Event event: eventList) {
                eventProcessors.add(new EventProcessor(event, 5, eventsTableDao));
            }

            try {

                List<Future<Void>> futures = executorService.invokeAll(eventProcessors);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
