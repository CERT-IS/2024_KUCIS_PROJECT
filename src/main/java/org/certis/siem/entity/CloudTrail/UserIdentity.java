package org.certis.siem.entity.CloudTrail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserIdentity {
    private String type;
    private String principalId;
    private String arn;
    private String accountId;
    private String accessKeyId;
    private String userName;
    private SessionContext sessionContext;
}
