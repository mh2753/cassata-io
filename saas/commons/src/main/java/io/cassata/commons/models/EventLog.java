package io.cassata.commons.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EventLog {
    private int id;
    private int saasEventsId;

    private int httpResponseCode;
    private String httpResponse;
}
