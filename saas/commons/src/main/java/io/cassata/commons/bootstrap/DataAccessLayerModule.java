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

package io.cassata.commons.bootstrap;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.cassata.commons.bootstrap.configurations.ServiceConfig;
import io.cassata.commons.dal.EventlogTableDao;
import io.cassata.commons.dal.EventsTableDao;
import io.cassata.commons.dal.MySQLEventlogTableDao;
import io.cassata.commons.dal.MySQLEventsTableDao;
import io.cassata.commons.dal.databaseBuilder.MySQLDatabaseBuilder;
import org.skife.jdbi.v2.DBI;

public class DataAccessLayerModule extends AbstractModule {

    private static DBI dbi;
    private static DatabaseTypes dbType;
    private static ServiceConfig serviceConfig;

    public DataAccessLayerModule(ServiceConfig serviceConfig, DBI dbi, DatabaseTypes dbType) {
        this(dbi, dbType);
        DataAccessLayerModule.serviceConfig = serviceConfig;
    }

    public DataAccessLayerModule(DBI dbi, DatabaseTypes dbType) {
        DataAccessLayerModule.dbi = dbi;
        DataAccessLayerModule.dbType = dbType;
    }

    protected void configure() {

        if (serviceConfig != null) {
            if (dbType.equals(DatabaseTypes.MYSQL)) {
                MySQLDatabaseBuilder mySQLDatabaseBuilder = new MySQLDatabaseBuilder(dbi, serviceConfig);
                mySQLDatabaseBuilder.build();
            }
        }

        bind(EventsTableDao.class).toProvider(EventsTableDAOProvider.class);
        bind(EventlogTableDao.class).toProvider(EventlogTableDAOProvider.class);
    }

    @Singleton
    public static class EventsTableDAOProvider implements Provider<EventsTableDao> {

        public EventsTableDao get() {

            //TODO add postgres here
            if (dbType.equals(DatabaseTypes.MYSQL)) {

                return dbi.onDemand(MySQLEventsTableDao.class);
            } else {
                throw new IllegalArgumentException("Unsupported Database type: " + dbType.name());
            }
        }
    }

    @Singleton
    public static class EventlogTableDAOProvider implements Provider<EventlogTableDao> {

        public EventlogTableDao get() {

            //TODO add postgres here
            if (dbType.equals(DatabaseTypes.MYSQL)) {

                return dbi.onDemand(MySQLEventlogTableDao.class);
            } else {
                throw new IllegalArgumentException("Unsupported Database type: " + dbType.name());
            }
        }
    }
}
