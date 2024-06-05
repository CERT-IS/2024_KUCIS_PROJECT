package org.certis.siem.entity.CloudTrail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseElements {
    private RequestId requestId;
    @Field(type = FieldType.Nested)
    private InstancesSet instancesSet;
    private User user;
}
