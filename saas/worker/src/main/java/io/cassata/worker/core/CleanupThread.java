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
import io.cassata.commons.models.Event;
import io.cassata.commons.models.EventStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

@Slf4j
public class CleanupThread implements  Runnable {
    private EventsTableDao eventsTableDao;
    private int timeToWaitForCleanup;

    private static final int CLEANUP_LIMIT = 100;

    public CleanupThread(EventsTableDao eventsTableDao, int timeToWaitForCleanup) {
        this.eventsTableDao = eventsTableDao;
        this.timeToWaitForCleanup = timeToWaitForCleanup;
    }

    public void run() {

        List<Event> events = eventsTableDao.getDeadEvents(CLEANUP_LIMIT, timeToWaitForCleanup);
        log.info("Running cleanup thread. Found {} events to clean up", events.size());

        for (Event event: events) {

            long processingSince = (new Date().getTime() - event.getLastUpdated().getTime())/1000;

            log.warn("Event id: {}, application: {} in processing state for more than {} seconds. Marking as Pending.", event.getId(), event.getApplication(),processingSince);

            eventsTableDao.updateEventStatus(event.getId(), EventStatus.PENDING);
        }
    }
}