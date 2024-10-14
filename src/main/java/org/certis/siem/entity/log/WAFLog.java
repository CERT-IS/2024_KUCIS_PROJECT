package org.certis.siem.entity.log;

import lombok.AllArgsConstructor;
        import lombok.Builder;
        import lombok.Data;
        import lombok.NoArgsConstructor;
        import java.time.LocalDateTime;

@Data
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

