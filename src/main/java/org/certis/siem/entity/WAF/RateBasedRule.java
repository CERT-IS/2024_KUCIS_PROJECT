package org.certis.siem.entity.WAF;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

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

    @Field(type = FieldType.Nested)
    private List<CustomValue> customValues;
}
