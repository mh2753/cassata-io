package io.cassata.commons.dal;

import io.cassata.commons.models.EventLog;

import java.util.List;

public interface EventlogTableDao {

    public void insertEventLog(EventLog eventLog);

    public List<EventLog> getEventLogByEventId(int id);
}
