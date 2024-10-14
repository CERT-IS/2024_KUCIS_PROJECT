package org.certis.siem.entity.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HttpLog {
    private String id;
    private String log_group;
    private LocalDateTime timestamp;
    private String message;
}
