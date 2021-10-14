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
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import io.cassata.commons.dal.EventsTableDao;
import io.cassata.commons.models.Event;
import io.cassata.commons.models.EventStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class WorkerThread implements Runnable {


    private EventsTableDao eventsTableDao;
    private int batchSize;
    private ExecutorService executorService;
    private EventProcessorFactory eventProcessorFactory;

    private boolean isShutDown;

    public WorkerThread(EventProcessorFactory eventProcessorFactory, EventsTableDao eventsTableDao, int batchSize) {
        this.eventsTableDao = eventsTableDao;
        this.batchSize = batchSize;
        this.executorService = Executors.newFixedThreadPool(batchSize);
        this.eventProcessorFactory = eventProcessorFactory;
        this.isShutDown = false;
    }

    public void run() {

        List<Event> eventList = eventsTableDao.fetchAndLockEventsToProcess(batchSize);


        while (eventList.size() > 0 && !isShutDown) {
            log.info("Received {} events from DB to process.", eventList.size());

            List<Callable<Void>> eventProcessors = new ArrayList<Callable<Void>>();
            for (Event event: eventList) {
                eventProcessors.add(eventProcessorFactory.getEventProcessor(event));
            }

            try {

                List<Future<Void>> futures = executorService.invokeAll(eventProcessors);

            } catch (InterruptedException e) {
                log.error("Exception while executing event processors", e);
            }

            eventList = eventsTableDao.fetchAndLockEventsToProcess(batchSize);
        }

        if (!isShutDown) {
            log.info("All pending events processed.");
        } else if (eventList.size() > 0) {
            List<Integer> eventIds = new ArrayList<Integer>();
            for (Event event: eventList) {
                eventIds.add(event.getId());
            }

            eventsTableDao.batchUpdateStatus(EventStatus.PENDING.name(), eventIds);
        }
    }

    public void shutDown() {
        log.info("Received shutdown signal. Will commence shutdown");
        this.isShutDown = true;
    }
}
