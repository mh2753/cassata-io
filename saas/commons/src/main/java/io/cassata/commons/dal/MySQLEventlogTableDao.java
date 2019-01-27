package io.cassata.commons.dal;

import io.cassata.commons.models.EventLog;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;

@UseStringTemplate3StatementLocator
public abstract class MySQLEventlogTableDao implements EventlogTableDao {

    @RegisterMapper(EventMapper.class)
    @SqlUpdate("insert into _saas_eventlog " +
            "(_saas_events_id, http_resposne_code, http_response, created_at)" +
            "values (:saasEventsId, :httpResponseCode, httpResponse, now())")
    public abstract void insertEventLog(@BindBean EventLog eventLog);
}