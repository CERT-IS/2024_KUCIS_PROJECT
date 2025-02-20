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
public class HttpLog {
    private String id;
    private String log_group;
    private LocalDateTime timestamp;
    private String message;
}
