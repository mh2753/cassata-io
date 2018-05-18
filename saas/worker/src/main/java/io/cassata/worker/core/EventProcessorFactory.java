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

import com.google.inject.Inject;
import io.cassata.commons.dal.EventsTableDao;
import io.cassata.commons.models.Event;
import lombok.Setter;

@Setter
public class EventProcessorFactory {

    private Integer retryCount;
    private EventsTableDao eventsTableDao;

    public EventProcessor getEventProcessor(Event event) {

        EventProcessor eventProcessor = new EventProcessor();
        eventProcessor.setEvent(event);
        eventProcessor.setEventsTableDao(eventsTableDao);
        eventProcessor.setRetryCount(retryCount);

        return eventProcessor;
    }
}
