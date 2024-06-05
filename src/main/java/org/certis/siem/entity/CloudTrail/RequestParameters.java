package org.certis.siem.entity.CloudTrail;

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
public class RequestParameters {
    @Field(type = FieldType.Nested)
    private InstancesSet instancesSet;
    private String userName;
}
