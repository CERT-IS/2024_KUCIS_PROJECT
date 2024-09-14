package org.certis.siem.utils;


import com.fasterxml.jackson.databind.JsonNode;
import org.certis.siem.entity.AccessLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AccessLogsMapper {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public static AccessLog mapJsonNodeToAccessLogsEvent(JsonNode jsonNode) {
        return AccessLog.builder()
                .id(getFieldAsString(jsonNode, "@id"))
                .logGroup(getFieldAsString(jsonNode, "@log_group"))
                .timestamp(parseTimestamp(getFieldAsString(jsonNode, "@timestamp")))
                .message(getFieldAsString(jsonNode, "@message"))
                .build();
    }

    private static String getFieldAsString(JsonNode jsonNode, String fieldName) {
        JsonNode field = jsonNode.get(fieldName);
        return field != null ? field.asText() : null;
    }

    private static LocalDateTime parseTimestamp(String timestampStr) {
        try {
            return LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Unrecognized timestamp format: " + timestampStr, e);
        }
    }
}

