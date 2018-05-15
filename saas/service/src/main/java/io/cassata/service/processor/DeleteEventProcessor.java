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

package io.cassata.service.processor;

import io.cassata.commons.dal.EventsTableDao;
import io.cassata.commons.models.Event;
import io.cassata.service.http.response.BasicResponse;
import lombok.extern.slf4j.Slf4j;

import javax.sql.rowset.BaseRowSet;

@Slf4j
public class DeleteEventProcessor {

    private EventsTableDao eventsTableDao;

    public DeleteEventProcessor(EventsTableDao eventsTableDao) {
        this.eventsTableDao = eventsTableDao;
    }

    public BasicResponse deleteEvent(String appId, String eventId) {

        BasicResponse response = new BasicResponse();
        try {

            log.info("Received delete event request for appId: {}, eventId: {}", appId, eventId);

            int numRowsDeleted = eventsTableDao.deleteEvent(appId, eventId); //FIXME Handle SQL exceptions


            if (numRowsDeleted == 0) {
                Event event = eventsTableDao.getEventById(appId, eventId);

                if (event == null) {
                    log.info("No event found for appid: {} and eventId: {}", appId, eventId);
                    response.setStatus(BasicResponse.StatusCode.failed);
                    response.setMessage("event not found");
                } else {
                    log.info("Unable to delete event for appId: {}, eventId: {} as status is {}", appId, eventId, event.getEventStatus());
                    response.setStatus(BasicResponse.StatusCode.failed);
                    response.setMessage("event in status " + event.getEventStatus() + ". Cannot be deleted.");
                }
            } else {
                response.setStatus(BasicResponse.StatusCode.ok);
                log.info("Event successfully deleted fr appId: {}, eventId: {}", appId, eventId);
            }
        } catch (Exception e) {
            log.error("Exception in executing delete request for appId: " + appId + ", eventId:" + eventId, e);
            response.setStatus(BasicResponse.StatusCode.failed);
            response.setMessage("Internal Server Error while handling the request");
        }

        return response;
    }
}
