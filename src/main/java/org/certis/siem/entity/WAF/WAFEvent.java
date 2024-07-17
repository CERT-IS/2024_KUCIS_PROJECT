package org.certis.siem.entity.WAF;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "waf-event-store")
public class WAFEvent {
    @Id
    private Long id;

    private long timestamp;
    private String formatVersion;
    private String webaclId;
    private String terminatingRuleId;
    private String terminatingRuleType;
    private String action;
    private String httpSourceName;
    private String httpSourceId;
    private String requestId;
    private String clientIp;
    private String country;
    private String uri;
    private String args;
    private String httpVersion;
    private String httpMethod;

    @Field(type = FieldType.Nested)
    private List<Header> headers;

    // RateBasedRuleLog fields
    @Field(type = FieldType.Nested)
    private List<RateBasedRule> rateBasedRuleList;


    // SQLiDetectionLog fields
    @Field(type = FieldType.Nested)
    private List<Label> labels;

    @Field(type = FieldType.Nested)
    private List<Condition> terminatingRuleMatchDetails;


    // CaptchaLog fields
    @Field(type = FieldType.Nested)
    private List<Header> requestHeadersInserted;

    @Field(type = FieldType.Nested)
    private CaptchaResponse captchaResponse;
}
