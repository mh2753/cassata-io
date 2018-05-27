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
package io.cassata.commons.dal;

import io.cassata.commons.models.Event;
import io.cassata.commons.models.EventStatus;

import java.util.List;

public interface EventsTableDao {

    public void insertEvent(Event event);

    public List<Event> fetchAndLockEventsToProcess(int count);

    public Event getEventById(String appId, String eventId);

    public void updateEventStatus(int eventId, EventStatus eventStatus);

    public void batchUpdateStatus(String status, List<Integer> ids);

    public int deleteEvent(String appId, String eventId);

    public List<Event> getDeadEvents(int limit, int gracePeriod);

}
