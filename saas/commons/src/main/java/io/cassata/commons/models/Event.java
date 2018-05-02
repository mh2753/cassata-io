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

package io.cassata.commons.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cassata.commons.exceptions.SchedulerException;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;
import org.skife.jdbi.v2.unstable.BindIn;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Builder
@Data
public class Event {

    private int id;
    private String eventId;
    private String application;

    private String eventJson;
    private String httpMethod; //TODO Replace this with an Enum
    private List<String> headers;
    private String destinationUrl;
    private EventStatus eventStatus;
    private Timestamp expiry;

    public String getHeaders() {
        try {
            return new ObjectMapper().writeValueAsString(headers);
        } catch (JsonProcessingException e) {
            throw new SchedulerException("Unable to convert map to JSON", e);
        }
    }
}