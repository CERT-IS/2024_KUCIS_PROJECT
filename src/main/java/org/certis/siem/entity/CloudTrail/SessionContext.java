package org.certis.siem.entity.CloudTrail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionContext {
    private SessionIssuer sessionIssuer;
    private WebIdFederationData webIdFederationData;
    private Attributes attributes;
}
