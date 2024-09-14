package org.certis.siem.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccessLog {
    private String id;
    private String logGroup;
    private LocalDateTime timestamp;
    private String message;
}
