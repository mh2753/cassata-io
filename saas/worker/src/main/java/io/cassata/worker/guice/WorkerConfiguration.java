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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class WorkerConfiguration {

    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    private WorkerThreadProperties workerThreadProperties;

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory factory) {
        this.database = factory;
    }
    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("workerThreadProperties")
    public WorkerThreadProperties getWorkerThreadProperties() {
        return workerThreadProperties;
    }

    @JsonProperty("workerThreadProperties")
    public void setWorkerThreadProperties(WorkerThreadProperties workerThreadProperties) {
        this.workerThreadProperties = workerThreadProperties;
    }

}
