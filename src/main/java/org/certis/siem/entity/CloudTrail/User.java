package org.certis.siem.entity.CloudTrail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private String path;
    private String arn;
    private String userId;
    private String createDate;
    private String userName;
}
