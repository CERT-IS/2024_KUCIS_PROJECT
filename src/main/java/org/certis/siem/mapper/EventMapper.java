package org.certis.siem.mapper;

import org.certis.siem.entity.EventStream;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventMapper {


    public static <T> EventStream mapLogsToEvent(String eventName, String eventType, T logEvent) {
        List<T> logs = new ArrayList<>();
        logs.add(logEvent);

        return EventStream.builder()
                .eventName(eventName)
                .eventType(eventType)
                .timestamp(LocalDateTime.now())
                .logs(new ArrayList<>(logs))
                .build();
    }
}

