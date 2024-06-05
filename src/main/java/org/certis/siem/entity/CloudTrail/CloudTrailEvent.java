package org.certis.siem.entity.CloudTrail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "cloudtrail-event-store")
public class CloudTrailEvent {
    @Id
    private String eventID;

    private String eventVersion;

    @Field(type = FieldType.Object)
    private UserIdentity userIdentity;

    private String eventTime;

    private String eventSource;

    private String eventName;

    private String awsRegion;

    private String sourceIPAddress;

    private String userAgent;

    @Field(type = FieldType.Object)
    private RequestParameters requestParameters;

    @Field(type = FieldType.Object)
    private ResponseElements responseElements;

    private String requestID;

    private boolean readOnly;

    private String eventType;

    private boolean managementEvent;

    private String recipientAccountId;

    private String eventCategory;

    @Field(type = FieldType.Object)
    private TlsDetails tlsDetails;

    private boolean sessionCredentialFromConsole;
}
