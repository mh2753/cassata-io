package io.cassata.commons.dal;

import io.cassata.commons.models.EventLog;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;

import java.util.List;

@UseStringTemplate3StatementLocator
public abstract class MySQLEventlogTableDao implements EventlogTableDao {

    @SqlUpdate("insert into _saas_eventlog " +
            "(_saas_events_id, http_response_code, http_response, created_at)" +
            "values (:saasEventsId, :httpResponseCode, :httpResponse, now())")
    public abstract void insertEventLog(@BindBean EventLog eventLog);

    @RegisterMapper(EventlogMapper.class)
    @SqlQuery(
            "SELECT * " +
                    "FROM _saas_eventlog " +
                    "WHERE _saas_events_id = :event_id"
    )
    public abstract List<EventLog> getEventLogByEventId(@Bind("event_id") int id);

}