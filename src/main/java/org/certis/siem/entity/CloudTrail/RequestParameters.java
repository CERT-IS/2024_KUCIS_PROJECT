package org.certis.siem.entity.CloudTrail;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String bucketName;
    private String key;

    @JsonProperty("x-amz-acl")
    private String xAmzAcl;

    @JsonProperty("x-amz-server-side-encryption")
    private String xAmzServerSideEncryption;

    @JsonProperty("x-amz-server-side-encryption-aws-kms-key-id")
    private String xAmzServerSideEncryptionAwsKmsKeyId;

    @JsonProperty("x-amz-server-side-encryption-context")
    private String xAmzServerSideEncryptionContext;

    @JsonProperty("Host")
    private String host;
}
