package io.cassata.commons.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Builder
@Data
public class EventLog {

    @JsonIgnore
    private int id;

    @JsonIgnore
    private int saasEventsId;

    private int httpResponseCode;
    private String httpResponse;

    private Timestamp lastUpdated;
}
