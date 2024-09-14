package org.certis.siem.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.certis.siem.entity.EventStream;
import org.certis.siem.repository.EventRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Instant;


@Service
@Slf4j
@RequiredArgsConstructor
public class EventDetectService {

    private final AwsS3Service awsS3Service;
    private final EventRepository eventRepository;

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

    public Flux<EventStream> findByEventType(Instant lastTimestamp, int size,int offset, String eventType) {
        return eventRepository.findByEventTypeAfterTimestamp(eventType, lastTimestamp, size, offset);
    }

    public Flux<EventStream> findByEventTypeNot(Instant lastTimestamp, int size, int offset, String eventType) {
        return eventRepository.findByEventTypeNotAfterTimestamp(eventType, lastTimestamp, size, offset);
    }

    public Flux<EventStream> findAll(){
        return eventRepository.findAll();
    }


}
