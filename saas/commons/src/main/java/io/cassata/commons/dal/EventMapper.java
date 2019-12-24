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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cassata.commons.exceptions.CassataException;
import io.cassata.commons.http.HttpRequestType;
import io.cassata.commons.models.Event;
import io.cassata.commons.models.EventStatus;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EventMapper implements ResultSetMapper<Event> {
    public Event map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> headers = null;
        try {
             headers = objectMapper.readValue(resultSet.getString("http_headers"), List.class);
        } catch (IOException e) {
            throw new CassataException("Unable to parse http_headers from DB", e);
        }

        return Event.builder()
                .id(resultSet.getInt("id"))
                .eventId(resultSet.getString("event_id"))
                .application(resultSet.getString("application"))
                .destinationUrl(resultSet.getString("destination_url"))
                .eventJson(resultSet.getString("event_json"))
                .eventStatus(EventStatus.valueOf(resultSet.getString("status")))
                .httpMethod(HttpRequestType.valueOf(resultSet.getString("http_method")))
                .headers(headers)
                .headerJson(resultSet.getString("http_headers"))
                .expiry(resultSet.getTimestamp("expiry"))
                .lastUpdated(resultSet.getTimestamp("last_updated"))
                .build();
    }
}
