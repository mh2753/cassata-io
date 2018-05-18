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

import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Setter
public class CassataWorker {


    private List<WorkerThread> workerThreads;
    private int workerThreadPollingInterval;
    private ScheduledExecutorService scheduledExecutorService;

    private static final int INITIAL_DELAY = 0;

    public void run() {

        for (WorkerThread workerThread: workerThreads) {
            scheduledExecutorService.scheduleAtFixedRate(workerThread, INITIAL_DELAY, workerThreadPollingInterval, TimeUnit.SECONDS);
        }
    }
}
