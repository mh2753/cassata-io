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
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.cassata.commons.dal.EventsTableDao;
import io.cassata.commons.dal.MySQLEventsTableDao;
import org.skife.jdbi.v2.DBI;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseModule extends AbstractModule {

    private Properties properties;
    private static DBI dbi;

    public DatabaseModule(DBI dbi) {
        this.dbi = dbi;
    }

    protected void configure() {

        properties = new Properties();
        try {
            properties.load(new FileInputStream("db.properties"));

        } catch (IOException e) {
        }

        bind(EventsTableDao.class).toProvider(EventsTableDAOProvider.class);
    }

//    public static class DBIProvider implements Provider<DBI> {
//
//        public DBI get() {
//
//            String connectionString = "jdbc:mysql://localhost:3306/cassata";
//            DBI dbi = new DBI(connectionString, "rw", "password123");
//
//            return dbi;
//        }
//    }

    public static class EventsTableDAOProvider implements Provider<EventsTableDao> {

        public EventsTableDao get() {
            return dbi.onDemand(MySQLEventsTableDao.class); //TODO add postgres here
        }
    }
}
