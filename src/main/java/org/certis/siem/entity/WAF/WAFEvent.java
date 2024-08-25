package org.certis.siem.entity.WAF;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WAFEvent {
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

    private List<Header> headers;

    // RateBasedRuleLog fields
    private List<RateBasedRule> rateBasedRuleList;


    // SQLiDetectionLog fields
    private List<Label> labels;

    private List<Condition> terminatingRuleMatchDetails;


    // CaptchaLog fields
    private List<Header> requestHeadersInserted;

    private CaptchaResponse captchaResponse;
}
