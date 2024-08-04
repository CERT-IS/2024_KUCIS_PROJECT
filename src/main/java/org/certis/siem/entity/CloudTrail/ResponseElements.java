package org.certis.siem.entity.CloudTrail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseElements {
    @JsonProperty("x-amz-server-side-encryption-aws-kms-key-id")
    private String xAmzServerSideEncryptionAwsKmsKeyId;

    @JsonProperty("x-amz-server-side-encryption")
    private String xAmzServerSideEncryption;

    @JsonProperty("x-amz-server-side-encryption-context")
    private String xAmzServerSideEncryptionContext;
}
