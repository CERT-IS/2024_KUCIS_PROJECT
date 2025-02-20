package org.certis.siem.entity.log;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
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
