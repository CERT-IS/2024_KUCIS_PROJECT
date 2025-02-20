package org.certis.siem.entity.log;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WAFLog {
    private String id;
    private String logGroup;
    private LocalDateTime timestamp;
    private String terminatingRuleType;
    private String terminatingRuleId;
    private String action;
    private String country;
    private String clientIp;
}

