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

package io.cassata.worker.bootstrap;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.cassata.commons.bootstrap.DatabaseModule;
import io.cassata.commons.dal.EventsTableDao;
import io.cassata.worker.core.WorkerThread;
import org.skife.jdbi.v2.DBI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static int threadPoolSize = 5; //FIXME Get from Guice
    private static int threadSchedulingPeriod = 10; //FIXME get from Guice
    private static final int INITIAL_DELAY_BOUND = 10;

    public static void main(String[] args) {

        DBI dbi = new DBI("jdbc:mysql://localhost:3306/cassata", "rw", "password123");
        Injector injector = Guice.createInjector(new DatabaseModule(dbi));

        EventsTableDao eventsTableDao = injector.getInstance(EventsTableDao.class);

        ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(threadPoolSize);

        //FIXME Move this to guice
        List<WorkerThread> workerThreads = new ArrayList<WorkerThread>();
        workerThreads.add(new WorkerThread(eventsTableDao));

        Random rand = new Random();

        for (WorkerThread workerThread: workerThreads) {
            scheduledPool.scheduleAtFixedRate(workerThread, rand.nextInt(INITIAL_DELAY_BOUND), threadSchedulingPeriod, TimeUnit.SECONDS);
        }
    }
}