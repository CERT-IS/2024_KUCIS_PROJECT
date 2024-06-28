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
class Condition {
    private String conditionType;
    private String sensitivityLevel;
    private String location;
    private List<String> matchedData;
}

