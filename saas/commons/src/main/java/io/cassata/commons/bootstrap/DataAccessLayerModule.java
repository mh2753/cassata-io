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
import io.cassata.commons.dal.EventsTableDao;
import io.cassata.commons.dal.MySQLEventsTableDao;
import org.skife.jdbi.v2.DBI;

public class DataAccessLayerModule extends AbstractModule {

    private static DBI dbi;
    private static DatabaseTypes dbType;

    public DataAccessLayerModule(DBI dbi, DatabaseTypes dbType) {
        DataAccessLayerModule.dbi = dbi;
        DataAccessLayerModule.dbType = dbType;
    }

    protected void configure() {

        bind(EventsTableDao.class).toProvider(EventsTableDAOProvider.class);
    }

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
}
