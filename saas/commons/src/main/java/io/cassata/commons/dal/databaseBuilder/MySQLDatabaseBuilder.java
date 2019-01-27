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

package io.cassata.commons.dal.databaseBuilder;

import io.cassata.commons.bootstrap.configurations.ServiceConfig;
import io.cassata.commons.exceptions.CassataException;
import lombok.extern.slf4j.Slf4j;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

@Slf4j
public class MySQLDatabaseBuilder extends DatabaseBuilder {

    private static final String CREATE_EVENTS_FILE_NAME = "sql/mysql-create-events.sql";
    private static final String CREATE_EVENTLOG_FILE_NAME = "sql/mysql-create-eventlog.sql";

    private DBI dbi;
    private ServiceConfig serviceConfig;

    public MySQLDatabaseBuilder(DBI dbi, ServiceConfig serviceConfig) {
        this.dbi = dbi;
        this.serviceConfig = serviceConfig;
    }

    public void build() {

        if (serviceConfig.isCreateTablesIfNotExists()) {

            //Create the main events table
            log.info("CreateTablesIfNotExists is set to true. Trying to create tables");
            log.info("Creating events table");
            createTableFromFile(CREATE_EVENTS_FILE_NAME);

            log.info("Creating events log table");
            //Create the event log table
            createTableFromFile(CREATE_EVENTLOG_FILE_NAME);
        }
    }

    private void createTableFromFile(String fileName) {
        try {

            Handle handle = dbi.open();
            handle.execute(getQueryFromFile(fileName));
        } catch (Exception e) {

            log.error("Error in creating tables.", e);
            throw new CassataException("Exception in creating table. ", e);
        }

    }
}