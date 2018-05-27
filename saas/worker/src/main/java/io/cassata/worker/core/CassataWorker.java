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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Setter
@Slf4j
public class CassataWorker {

    private List<WorkerThread> workerThreads;
    private ScheduledExecutorService scheduledExecutorService;
    private int workerThreadPollingInterval;

    private ScheduledExecutorService cleanupThreadExecutor;
    private CleanupThread cleanupThread;
    private int cleanupThreadPollingInterval;

    private static final int INITIAL_DELAY = 0;

    public void run() {

        log.info("Scheduling worker threads");
        for (WorkerThread workerThread: workerThreads) {
            scheduledExecutorService.scheduleAtFixedRate(workerThread, INITIAL_DELAY, workerThreadPollingInterval, TimeUnit.SECONDS);
        }

        log.info("Scheduling cleanup threads");
        cleanupThreadExecutor.scheduleAtFixedRate(cleanupThread, INITIAL_DELAY, cleanupThreadPollingInterval, TimeUnit.SECONDS);

        log.info("Registering shutdown hook");
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                log.info("Shutdown called. Asking all threads to stop and waiting for 5 seconds...");
                scheduledExecutorService.shutdown();

                try {
                    scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS);

                } catch (InterruptedException e) {
                    log.error("Shutdown interrupted", e);
                }

                log.info("Executor service stopped. Calling shutdown on Worker threads");
                for (WorkerThread workerThread: workerThreads) {
                    workerThread.shutDown();
                }

                log.info("Waiting for 10 seconds for all requests to complete.");

                try {
                    Thread.sleep(1000 * 10);
                } catch (InterruptedException e) {
                    log.error("Interrupted", e);
                }

                log.info("Shutting Down Worker!");
            }
        }));

    }
}
