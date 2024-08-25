package org.certis.siem.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CloudTrailLog {
    private String id;
    private String logGroup;
    private LocalDateTime timestamp;
    private String eventID;
    private String eventType;
    private Map<String, Object> userIdentity;
    private String eventSource;
    private String eventName;
    private Instant eventTime;
    private String awsRegion;
    private String sourceIPAddress;
    private String userAgent;
    private List<Map<String, Object>> resources;
    private Map<String, Object> requestParameters;
    private Map<String, Object> responseElements;
    private Map<String, Object> additionalEventData;
    private Boolean readOnly;
}
