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
class RateBasedRule {
    private String rateBasedRuleId;
    private String rateBasedRuleName;
    private String limitKey;
    private int maxRateAllowed;
    private int evaluationWindowSec;

    private List<CustomValue> customValues;
}
