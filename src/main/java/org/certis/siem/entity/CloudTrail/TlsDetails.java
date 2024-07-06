package org.certis.siem.entity.CloudTrail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TlsDetails {
    private String signatureVersion;
    private String cipherSuite;
    private String authenticationMethod;
    private String xAmzId2;
    private int bytesTransferredIn;
    private int bytesTransferredOut;
    private String sseApplied;
}


