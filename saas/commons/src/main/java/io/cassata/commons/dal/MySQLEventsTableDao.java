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
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;

import java.util.ArrayList;
import java.util.List;

@UseStringTemplate3StatementLocator
public abstract class MySQLEventsTableDao implements EventsTableDao {

    //TODO get the table name dynamically

    @RegisterMapper(EventMapper.class)
    @SqlUpdate("insert into events " +
            "(event_id, application, event_json, http_method, http_headers, destination_url, status, expiry, created_at)\n" +
            "values (:eventId, :application, :eventJson, :httpMethod, :headers, :destinationUrl, :eventStatus, :expiry, now())")
    public abstract void insertEvent(@BindBean  Event event);


    @SqlUpdate("Update _saas_events set status = :status where id = :event_id")
    public abstract void updateEventStatus(@Bind("event_id") int eventId, @Bind("status") EventStatus status);

    @RegisterMapper(EventMapper.class)
    @SqlQuery("SELECT * FROM _saas_events " +
            "WHERE status = 'PENDING' " +
            "AND expiry \\<= now() " +          //The delimiter \\ is required because of UseStringTemplate3StatementLocator
            "LIMIT :count " +
            "FOR UPDATE")
    protected abstract List<Event> getNextEventsForUpdate(@Bind("count") int count);

    @SqlUpdate("Update _saas_events set status = :status where id in (<ids>) ")
    public abstract void batchUpdateStatus(@Bind("status") String status, @BindIn("ids") List<Integer> ids);

    @Transaction
    public List<Event> fetchAndLockEventsToProcess(int count) {
        List<Event> events = getNextEventsForUpdate(count);
        List<Integer> eventIds = new ArrayList<Integer>();
        for (Event event: events)  {
            eventIds.add(event.getId());
        }

        if (eventIds.size() > 0 ) {
            batchUpdateStatus(EventStatus.PROCESSING.name(), eventIds);
        }

        return events;
    }
}
