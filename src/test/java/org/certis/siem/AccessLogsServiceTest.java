package org.certis.siem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.certis.siem.service.AccessLogsService.ADMIN_PAGE_PATTERNS;
import static org.certis.siem.service.AccessLogsService.SQL_INJECTION_PATTERNS;
import static org.certis.siem.service.AccessLogsService.XSS_PATTERNS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.certis.siem.entity.EventStream;
import org.certis.siem.entity.log.AccessLog;
import org.certis.siem.repository.EventRepository;
import org.certis.siem.service.AccessLogsService;
import org.certis.siem.service.OpenSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AccessLogsServiceTest {

    @Mock private OpenSearchService openSearchService;
    @Mock private EventRepository eventRepository;
    @InjectMocks private AccessLogsService accessLogsService;


    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void 로그가_존재하는_경우_보안_위협을_탐지하는_프로세스_동작() {
        LocalDateTime lastTimestamp = LocalDateTime.now().minusDays(1);
        LocalDateTime newTimestamp = LocalDateTime.now();

        AccessLog log1 = AccessLog.builder()
                .timestamp(LocalDateTime.now())
                .message("GET /admin HTTP/1.1")
                .build();
        AccessLog log2 = AccessLog.builder()
                .timestamp(LocalDateTime.now())
                .message("GET /index HTTP/1.1")
                .build();

        JsonNode jsonNode1 = objectMapper.valueToTree(log1);
        JsonNode jsonNode2 = objectMapper.valueToTree(log2);

        List<JsonNode> jsonNodes = List.of(jsonNode1, jsonNode2);
        List<AccessLog> accessLogs = List.of(log1, log2);

        when(openSearchService.executeSearch(any())).thenReturn(Flux.fromIterable(jsonNodes));
        when(eventRepository.saveAll(Flux.empty())).thenReturn(Flux.empty());

        StepVerifier.create(accessLogsService.process(lastTimestamp))
                .expectNext(newTimestamp)
                .verifyComplete();

        ArgumentCaptor<List<EventStream>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(eventRepository, times(1)).saveAll(eventCaptor.capture());

        List<EventStream> savedEvents = eventCaptor.getValue();
        assertThat(savedEvents).isNotEmpty();
    }

    @Test
    void SQL_Injection_검출하는_메서드() {
        AccessLog log = AccessLog.builder()
                .timestamp(LocalDateTime.now())
                .message("GET /index.php?id=1' OR '1'='1 HTTP/1.1")
                .build();
        Flux<AccessLog> logs = Flux.just(log);

        StepVerifier.create(accessLogsService.checkPattern(logs, SQL_INJECTION_PATTERNS))
                .expectNext(log)
                .verifyComplete();
    }

    @Test
    void XSS_검출하는_메서드() {
        AccessLog log = AccessLog.builder()
                .timestamp(LocalDateTime.now())
                .message("GET /index.php?search=<script>alert('XSS')</script> HTTP/1.1")
                .build();
        Flux<AccessLog> logs = Flux.just(log);

        StepVerifier.create(accessLogsService.checkPattern(logs, XSS_PATTERNS))
                .expectNext(log)
                .verifyComplete();
    }

    @Test
    void 관리자_페이지_접근_검출하는_메서드() {
        AccessLog log = AccessLog.builder()
                .timestamp(LocalDateTime.now())
                .message("GET /index.php?id=1' OR '1'='1 HTTP/1.1")
                .build();
        Flux<AccessLog> logs = Flux.just(log);

        StepVerifier.create(accessLogsService.checkPattern(logs, ADMIN_PAGE_PATTERNS))
                .expectNext(log)
                .verifyComplete();
    }

    @Test
    void 정상적인_로그는_탐지시_건너뛰는_메서드() {
        AccessLog log = AccessLog.builder()
                .timestamp(LocalDateTime.now())
                .message("GET /home HTTP/1.1")
                .build();
        Flux<AccessLog> logs = Flux.just(log);

        StepVerifier.create(accessLogsService.checkPattern(logs, SQL_INJECTION_PATTERNS))
                .verifyComplete();
    }

    @Test
    void 디렉토리_인덱스_검출_메서드() {
        AccessLog log = AccessLog.builder()
                .timestamp(LocalDateTime.now())
                .message("GET /directory HTTP/1.1")
                .build();
        Flux<AccessLog> logs = Flux.just(log);

        StepVerifier.create(accessLogsService.checkDirectoryIndex(logs))
                .expectNext(log)
                .verifyComplete();
    }

    @Test
    void 해당_로그_그룹의_마지막_Timestamp를_조회하는_메서드() {
        LocalDateTime t1 = LocalDateTime.now().minusDays(2);
        LocalDateTime t2 = LocalDateTime.now().minusDays(1);
        LocalDateTime t3 = LocalDateTime.now();

        List<AccessLog> logs = Stream.of(t1, t2, t3)
                .map(t -> AccessLog.builder()
                        .timestamp(LocalDateTime.now())
                        .message("GET /test HTTP/1.1")
                        .build())
                .collect(Collectors.toList());

        LocalDateTime latest = accessLogsService.getLatestTimestamp(logs, t1);
        assertThat(latest).isEqualTo(t3);
    }
}
