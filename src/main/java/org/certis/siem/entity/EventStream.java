package org.certis.siem.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table("eventStream")
public class EventStream {
    @Id
    private Long id;
    private String eventName; //
    private String eventType; // cloud, web, waf
    private LocalDateTime timestamp;
    private String logs;
}
