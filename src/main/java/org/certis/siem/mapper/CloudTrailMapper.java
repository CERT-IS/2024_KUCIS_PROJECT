package org.certis.siem.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.certis.siem.entity.log.CloudTrailLog;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class CloudTrailMapper {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public static CloudTrailLog mapJsonNodeToCloudTrailLog(JsonNode jsonNode) {
        return CloudTrailLog.builder()
                .id(getFieldAsString(jsonNode, "@id"))
                .logGroup(getFieldAsString(jsonNode, "@log_group"))
                .timestamp(parseTimestamp(getFieldAsString(jsonNode, "@timestamp")))
                .eventID(getFieldAsString(jsonNode, "eventID"))
                .eventType(getFieldAsString(jsonNode, "eventType"))
                .userIdentity(getFieldAsMap(jsonNode, "userIdentity"))
                .eventSource(getFieldAsString(jsonNode, "eventSource"))
                .eventName(getFieldAsString(jsonNode, "eventName"))
                .eventTime(getFieldAsInstant(jsonNode,"eventTime"))
                .awsRegion(getFieldAsString(jsonNode, "awsRegion"))
                .sourceIPAddress(getFieldAsString(jsonNode, "sourceIPAddress"))
                .userAgent(getFieldAsString(jsonNode, "userAgent"))
                .resources(getFieldAsListOfMap(jsonNode, "resources"))
                .requestParameters(getFieldAsMap(jsonNode, "requestParameters"))
                .responseElements(getFieldAsMap(jsonNode, "responseElements"))
                .additionalEventData(getFieldAsMap(jsonNode, "additionalEventData"))
                .readOnly(getFieldAsBoolean(jsonNode, "readOnly"))
                .build();
    }

    private static String getFieldAsString(JsonNode jsonNode, String fieldName) {
        JsonNode field = jsonNode.get(fieldName);
        return field != null ? field.asText() : null;
    }

    private static Instant getFieldAsInstant(JsonNode jsonNode, String fieldName) {
        String timestampStr = getFieldAsString(jsonNode, fieldName);
        return Optional.ofNullable(timestampStr)
                .map(ts -> Instant.parse(ts))
                .orElse(null);
    }

    private static LocalDateTime parseTimestamp(String timestampStr) {
        try {
            return LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Unrecognized timestamp format: " + timestampStr, e);
        }
    }

    private static Map<String, Object> getFieldAsMap(JsonNode jsonNode, String fieldName) {
        JsonNode field = jsonNode.get(fieldName);
        Map<String, Object> resultMap = new HashMap<>();
        if (field != null && field.isObject()) {
            field.fields().forEachRemaining(entry -> resultMap.put(entry.getKey(), entry.getValue()));
        }
        return resultMap;
    }

    private static List<Map<String, Object>> getFieldAsListOfMap(JsonNode jsonNode, String fieldName) {
        JsonNode field = jsonNode.get(fieldName);
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (field != null && field.isArray()) {
            for (JsonNode element : field) {
                resultList.add(getFieldAsMap(element, ""));
            }
        }
        return resultList;
    }

    private static Boolean getFieldAsBoolean(JsonNode jsonNode, String fieldName) {
        JsonNode field = jsonNode.get(fieldName);
        return field != null ? field.asBoolean() : null;
    }
}

