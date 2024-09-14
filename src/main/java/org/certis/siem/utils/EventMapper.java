package org.certis.siem.utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.certis.siem.entity.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static String convertToJson(Object log) {
        try {
            return objectMapper.writeValueAsString(log);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting WAFLog to JSON: " + e.getMessage(), e);
        }
    }

    private static String convertListToJson(List<String> logs) {
        try {
            return objectMapper.writeValueAsString(logs);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting logs list to JSON: " + e.getMessage(), e);
        }
    }

    public static EventStream mapAccessLogsToEvent(String eventName, String eventType, AccessLog accessLogsEvent) {
        LocalDateTime createdAt = accessLogsEvent.getTimestamp();
        List<String> logs = new ArrayList<>();
        logs.add(convertToJson(accessLogsEvent));

        String logsAsJson = convertListToJson(logs);

        return EventStream.builder()
                .eventName(eventName)
                .eventType(eventType)
                .timestamp(createdAt)
                .logs(logsAsJson)
                .build();
    }

    public static EventStream mapWAFLogsToEvent(String eventName, String eventType, WAFLog wafEvent) {
        LocalDateTime createdAt = wafEvent.getTimestamp();
        List<String> logs = new ArrayList<>();
        logs.add(convertToJson(wafEvent));

        String logsAsJson = convertListToJson(logs);

        return EventStream.builder()
                .eventName(eventName)
                .eventType(eventType)
                .timestamp(createdAt)
                .logs(logsAsJson)
                .build();
    }

    public static EventStream mapCloudTrailLogsToEvent(String eventName, String eventType, CloudTrailLog cloudTrailLog) {
        LocalDateTime createdAt = cloudTrailLog.getTimestamp();
        List<String> logs = new ArrayList<>();
        logs.add(convertToJson(cloudTrailLog));

        String logsAsJson = convertListToJson(logs);

        return EventStream.builder()
                .eventName(eventName)
                .eventType(eventType)
                .timestamp(createdAt)
                .logs(logsAsJson)
                .build();
    }

    public static EventStream mapHttpLogsToEvent(String eventName, String eventType, HttpLog httpLog) {
        LocalDateTime createdAt = httpLog.getTimestamp();
        List<String> logs = new ArrayList<>();
        logs.add(convertToJson(httpLog));

        String logsAsJson = convertListToJson(logs);

        return EventStream.builder()
                .eventName(eventName)
                .eventType(eventType)
                .timestamp(createdAt)
                .logs(logsAsJson)
                .build();
    }
}

