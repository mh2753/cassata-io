package io.cassata.commons.dal;

import io.cassata.commons.models.EventLog;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EventlogMapper implements ResultSetMapper<EventLog> {
    public EventLog map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {

        return EventLog.builder()
                .id(resultSet.getInt("id"))
                .saasEventsId(resultSet.getInt("_saas_events_id"))
                .httpResponseCode(resultSet.getInt("http_response_code"))
                .httpResponse(resultSet.getString("http_response"))
                .lastUpdated(resultSet.getTimestamp("created_at"))
                .build();
    }
}
