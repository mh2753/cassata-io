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
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import io.cassata.commons.bootstrap.DataAccessLayerModule;
import io.cassata.commons.bootstrap.DatabaseTypes;
import io.cassata.commons.dal.EventsTableDao;
import io.cassata.worker.core.CassataWorker;
import io.cassata.worker.core.WorkerThread;
import io.cassata.worker.guice.WorkerThreadModule;
import lombok.extern.slf4j.Slf4j;
import org.skife.jdbi.v2.DBI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {

    public static void main(String[] args) {

        Injector injector = Guice.createInjector(new WorkerThreadModule());

        CassataWorker cassataWorker = injector.getInstance(CassataWorker.class);

        cassataWorker.run();
    }
}