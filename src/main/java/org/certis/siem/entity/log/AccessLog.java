package org.certis.siem.entity.log;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccessLog {
    private String id;
    private String logGroup;
    private LocalDateTime timestamp;
    private String message;
}
