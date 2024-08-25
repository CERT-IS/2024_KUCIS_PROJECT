package org.certis.siem.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.certis.siem.entity.CloudTrail.CloudTrailEvent;
import org.certis.siem.entity.FlowLog.FlowLogEvent;
import org.certis.siem.entity.WAF.WAFEvent;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventLog {
    private String type; // EventName:EventType

    private CloudTrailEvent cloudTrailEvent;
    private WAFEvent wafEvent;
    private FlowLogEvent flowLogEvent;
}
