package org.certis.siem.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Queue;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventStream {
    private String id;
    private String type;
    private int level; // 1,2,3 단계

    private Queue<EventLog> queue;
}