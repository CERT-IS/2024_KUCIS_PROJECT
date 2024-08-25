package org.certis.siem.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.certis.siem.entity.WAFLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class WAFMapper {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public static WAFLog mapJsonNodeToWAFEvent(JsonNode jsonNode) {
        return WAFLog.builder()
                .id(getFieldAsString(jsonNode, "@id"))
                .logGroup(getFieldAsString(jsonNode, "@log_group"))
                .timestamp(parseTimestamp(getFieldAsString(jsonNode, "@timestamp")))
                .terminatingRuleType(getFieldAsString(jsonNode, "terminatingRuleType"))
                .terminatingRuleId(getFieldAsString(jsonNode, "terminatingRuleId"))
                .action(getFieldAsString(jsonNode, "action"))
                .country(getFieldAsString(jsonNode, "httpRequest.country"))
                .clientIp(getFieldAsString(jsonNode, "httpRequest.clientIp"))
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

