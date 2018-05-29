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

package io.cassata.service.http.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.cassata.commons.http.HttpRequestType;
import io.cassata.commons.models.EventStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class AddEventRequest {
    private String eventId;
    private String application;

    private String eventJson;

    @JsonProperty("httpMethod")
    private HttpRequestType httpMethod; //TODO Replace this with an Enum

    private List<String> headers;
    private String destinationUrl;
    private EventStatus eventStatus;
    private long expiry;
}
