package org.certis.siem.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.certis.siem.entity.EventLog;
import org.certis.siem.entity.EventStream;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventDetectService {

    private final Map<Long, EventStream> eventStreams = new ConcurrentHashMap<>();
    private final AwsS3Service awsS3Service;
    private final ObjectMapper objectMapper;

    private Long counter = 0L;

    @PostConstruct
    public void initialize() {
        String latestEventStreamId = awsS3Service.getLatestEventStreamIdFromS3();

        if (latestEventStreamId != null) {
            try {
                counter = Long.parseLong(latestEventStreamId);
            } catch (NumberFormatException e) {
                counter = 0L;
            }

            log.info("현재 counter: "+counter);
        }
    }

    public Mono<EventLog> register(EventLog event) {
        String eventType = event.getType();

        EventStream eventStream = eventStreams.computeIfAbsent(++counter, k ->
             EventStream.builder()
                    .id(counter.toString())
                    .type(eventType)
                    .level(1)
                    .queue(new ConcurrentLinkedQueue<>())
                    .build());

        eventStream.getQueue().add(event);
        // 처음 감지된 보안 이벤트의 경우 - EventStream을 생성한 뒤, 이벤트 로그를 추가한다.

        return Mono.just(event);
    }


    public Mono<EventStream> getEventStreamByType(Long id) {
        return Mono.justOrEmpty(eventStreams.get(id));
    }

    public Flux<EventStream> getEventStreams() {
        return Flux.fromIterable(eventStreams.values());
    }


    public Flux<EventStream> getDangerousEvents() {
        List<EventStream> dangerousStreams = eventStreams.values().stream()
                .filter(eventStream -> eventStream.getLevel() == 3)
                .collect(Collectors.toList());

        return Flux.fromIterable(dangerousStreams)
                .flatMap(eventStream -> {
                    String eventLog;
                    try {
                        eventLog = objectMapper.writeValueAsString(eventStream);
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed stream", e));
                    }
                    String eventName = eventStream.getId(); // unique

                    return awsS3Service.upload(eventLog, eventName)
                            .thenReturn(eventStream);
                });
    }

}
