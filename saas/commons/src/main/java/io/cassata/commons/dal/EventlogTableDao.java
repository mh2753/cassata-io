package io.cassata.commons.dal;

import io.cassata.commons.models.EventLog;

public interface EventlogTableDao {

    public void insertEventLog(EventLog eventLog);
}
