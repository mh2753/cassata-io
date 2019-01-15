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

package io.cassata.worker.guice;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.*;
import com.google.inject.name.Names;
import io.cassata.commons.bootstrap.DataAccessLayerModule;
import io.cassata.commons.bootstrap.DatabaseTypes;
import io.cassata.commons.dal.EventsTableDao;
import io.cassata.worker.core.*;
import org.skife.jdbi.v2.DBI;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class WorkerThreadModule extends AbstractModule {

    private static WorkerConfiguration workerConfiguration;
    private String pathToConfig;

    public WorkerThreadModule(String pathToConfig) {
        this.pathToConfig = pathToConfig;
    }

    protected void configure() {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            //workerConfiguration = mapper.readValue(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.yaml"), WorkerConfiguration.class);
            workerConfiguration = mapper.readValue(new FileInputStream(pathToConfig), WorkerConfiguration.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String dbUrl = workerConfiguration.getDataSourceFactory().getUrl();
        String userName = workerConfiguration.getDataSourceFactory().getUser();
        String password = workerConfiguration.getDataSourceFactory().getPassword();

        DBI dbi = new DBI(dbUrl, userName, password);
        //Get the DB type for the JDBC Url. I hope there is a better way of doing this
        String []urlParts = dbUrl.split(":");
        String dbType = urlParts[1];
        bind(DatabaseTypes.class).toInstance(DatabaseTypes.fromString(dbType));

        install(new DataAccessLayerModule(dbi, DatabaseTypes.fromString(dbType)));

        bind(EventProcessorFactory.class).toProvider(EventProcessorFactoryProvider.class);

        bind(CassataWorker.class).toProvider(CassataWorkerProvider.class);

        MetricRegistry metrics = new MetricRegistry();
        JmxReporter jmxReporter = JmxReporter.forRegistry(metrics).build();
        bind(MetricRegistry.class).toInstance(metrics);
        bind(JmxReporter.class).toInstance(jmxReporter);
    }

    private static class EventProcessorFactoryProvider implements Provider<EventProcessorFactory> {

        @Inject
        EventsTableDao eventsTableDao;

        @Inject
        MetricRegistry metricRegistry;

        public EventProcessorFactory get() {
            EventProcessorFactory eventProcessorFactory = new EventProcessorFactory(metricRegistry);
            eventProcessorFactory.setEventsTableDao(eventsTableDao);
            eventProcessorFactory.setRetryCount(workerConfiguration.getWorkerThreadProperties().getHttpRetryCount());

            return eventProcessorFactory;
        }
    }

    @Singleton
    private static class CassataWorkerProvider implements Provider<CassataWorker> {

        private static final int NUM_CLEANUP_THREADS = 2;
        private static final int CLEANUP_POLLING_INTERVAL = 100;
        @Inject
        EventsTableDao eventsTableDao;

        @Inject
        EventProcessorFactory eventProcessorFactory;

        public CassataWorker get() {

            int batchSize = workerConfiguration.getWorkerThreadProperties().getNumEventsProcessedPerTransaction();

            List<WorkerThread> workerThreads = new ArrayList<WorkerThread>();

            for(int i=0; i<workerConfiguration.getWorkerThreadProperties().getNumWorkerThreads(); i++) {
                workerThreads.add(new WorkerThread(eventProcessorFactory, eventsTableDao, batchSize));
            }

            ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(workerConfiguration.getWorkerThreadProperties().getNumWorkerThreads());

            CleanupThread cleanupThread = new CleanupThread(eventsTableDao, workerConfiguration.getWorkerThreadProperties().getTimeToWaitBeforeCleanup());

            ScheduledExecutorService cleanupScheduledPool = Executors.newScheduledThreadPool(NUM_CLEANUP_THREADS);

            CassataWorker cassataWorker = new CassataWorker();
            cassataWorker.setScheduledExecutorService(scheduledPool);
            cassataWorker.setWorkerThreadPollingInterval(workerConfiguration.getWorkerThreadProperties().getWorkerThreadPollingInterval());
            cassataWorker.setWorkerThreads(workerThreads);
            cassataWorker.setCleanupThread(cleanupThread);
            cassataWorker.setCleanupThreadExecutor(cleanupScheduledPool);
            cassataWorker.setCleanupThreadPollingInterval(CLEANUP_POLLING_INTERVAL);

            return cassataWorker;
        }
    }
}
