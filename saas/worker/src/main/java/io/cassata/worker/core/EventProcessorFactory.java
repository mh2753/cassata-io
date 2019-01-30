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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import io.cassata.commons.dal.EventlogTableDao;
import io.cassata.commons.dal.EventsTableDao;
import io.cassata.commons.models.Event;
import lombok.Setter;

@Setter
public class EventProcessorFactory {

    private final static String METRIC_PREFIX = "io.cassata.worker.core";

    private Integer retryCount;
    private EventsTableDao eventsTableDao;
    private EventlogTableDao eventlogTableDao;
    private boolean logFailedRequests;

    private MetricRegistry metricRegistry;
    private Meter numRequests;
    private Meter failedRequests;
    private Meter completedRequets;

    public EventProcessorFactory(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        this.numRequests = this.metricRegistry.meter(METRIC_PREFIX + ".requests.total");
        this.failedRequests = this.metricRegistry.meter(METRIC_PREFIX + ".requests.failed");
        this.completedRequets = this.metricRegistry.meter(METRIC_PREFIX + ".requests.completed");
    }

    public EventProcessor getEventProcessor(Event event) {

        EventProcessor eventProcessor = new EventProcessor();
        eventProcessor.setEvent(event);
        eventProcessor.setEventsTableDao(eventsTableDao);
        eventProcessor.setEventlogTableDao(eventlogTableDao);
        eventProcessor.setRetryCount(retryCount);
        eventProcessor.setLogFailedRequests(logFailedRequests);

        eventProcessor.setNumRequests(numRequests);
        eventProcessor.setFailedRequests(failedRequests);
        eventProcessor.setCompletedRequests(completedRequets);

        return eventProcessor;
    }
}
