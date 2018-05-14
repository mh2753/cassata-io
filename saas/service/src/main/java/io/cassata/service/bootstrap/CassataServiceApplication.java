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
package io.cassata.service.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.cassata.commons.bootstrap.DatabaseModule;
import io.cassata.commons.models.Event;
import io.cassata.commons.models.EventStatus;
import io.cassata.commons.dal.EventsTableDao;
import io.cassata.service.processor.AddEventProcessor;
import io.cassata.service.processor.DeleteEventProcessor;
import io.dropwizard.Application;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.setup.Environment;
import lombok.extern.slf4j.Slf4j;
import org.skife.jdbi.v2.DBI;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;

@Slf4j
public class CassataServiceApplication extends Application<CassataServiceConfiguration> {

    public static void main(String[] args) {
        try {
            new CassataServiceApplication().run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(CassataServiceConfiguration cassataServiceConfiguration, Environment environment) throws Exception {

        final DBIFactory factory = new DBIFactory();

        final DBI dbi = factory.build(environment, cassataServiceConfiguration.getDataSourceFactory(), "mysql");

        Injector injector = Guice.createInjector(new DatabaseModule(dbi));

        EventsTableDao eventsTableDao = injector.getInstance(EventsTableDao.class);
        AddEventProcessor addEventProcessor = new AddEventProcessor(eventsTableDao);
        DeleteEventProcessor deleteEventProcessor = new DeleteEventProcessor(eventsTableDao);

        CassataServiceResource resource = new CassataServiceResource(addEventProcessor, deleteEventProcessor);
        environment.jersey().register(resource);
    }
}
