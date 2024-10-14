package org.certis.siem.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.certis.siem.entity.log.HttpLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class HttpMapper {

    public static HttpLog convertJsonNodeToHttpLog(JsonNode jsonNode) {
        String id = jsonNode.get("@id").asText();
        LocalDateTime timestamp = parseDateTime(jsonNode.get("@timestamp").asText());
        String message = jsonNode.get("@message").asText();
        String logGroup = jsonNode.get("@log_group").asText();

        return new HttpLog(id, logGroup, timestamp, message);
    }

    public static LocalDateTime parseDateTime(String dateTimeString) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            return LocalDateTime.parse(dateTimeString, formatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Error parsing date-time: " + dateTimeString, e);
        }
    }
}
