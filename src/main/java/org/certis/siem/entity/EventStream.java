package org.certis.siem.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Table("eventStream")
public class EventStream<T> {
    @Id
    private Long id;
    private String eventName; //
    private String eventType; // cloud, web, waf
    private LocalDateTime timestamp;
    private List<T> logs;
}
