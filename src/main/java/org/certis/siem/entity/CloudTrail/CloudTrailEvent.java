package org.certis.siem.entity.CloudTrail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloudTrailEvent {
    private String eventID;

    private String eventVersion;

    private UserIdentity userIdentity;

    private String eventTime;

    private String eventSource;

    private String eventName;

    private String awsRegion;

    private String sourceIPAddress;

    private String userAgent;

    private RequestParameters requestParameters;

    private ResponseElements responseElements;

    private String requestID;

    private boolean readOnly;

    private String eventType;

    private boolean managementEvent;

    private String recipientAccountId;

    private String eventCategory;

    private TlsDetails tlsDetails;

    private boolean sessionCredentialFromConsole;

    @JsonProperty("additionalEventData")
    private Map<String, Object> additionalEventData;

    @JsonProperty("resources")
    private List<Resource> resources;

    private String sharedEventID;
    private String vpcEndpointId;
}
