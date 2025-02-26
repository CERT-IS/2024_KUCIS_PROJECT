package org.certis.siem;

import static org.certis.siem.service.AccessLogsService.SQL_INJECTION_PATTERNS;
import static org.certis.siem.service.AccessLogsService.XSS_PATTERNS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.List;
import org.certis.siem.entity.EventStream;
import org.certis.siem.entity.log.HttpLog;
import org.certis.siem.repository.EventRepository;
import org.certis.siem.service.HttpLogsService;
import org.certis.siem.service.OpenSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class HttpLogsServiceTest {

    @Mock private OpenSearchService openSearchService;
    @Mock private EventRepository eventRepository;
    @InjectMocks private HttpLogsService httpLogsService;

    private LocalDateTime testTimestamp;
    private HttpLog testLog;

    @BeforeEach
    void init() {
        testTimestamp = LocalDateTime.now().minusHours(1);
        testLog = HttpLog.builder()
                .message("\"GET http://example.com/test?input=<script>alert(1)</script> HTTP/1.1\"")
                .timestamp(testTimestamp)
                .build();
    }

    @Test
    void 보안_위협을_탐지하는_프로세스_동작() {
        // given
        List<JsonNode> mockJsonNodes = List.of(mock(JsonNode.class));
        List<EventStream> mockEventStreams = List.of(mock(EventStream.class));

        when(openSearchService.executeSearch(any())).thenReturn(Flux.fromIterable(mockJsonNodes));
        when(eventRepository.saveAll(Flux.empty())).thenReturn(Flux.empty());

        // when
        Mono<LocalDateTime> result = httpLogsService.process(testTimestamp);

        // then
        StepVerifier.create(result)
                .expectNextMatches(timestamp -> !timestamp.isBefore(testTimestamp))
                .verifyComplete();

        verify(openSearchService).executeSearch(any());
        verify(eventRepository).saveAll(Flux.empty());
    }

    @Test
    void XSS_검출하는_메서드() {
        // given
        Flux<HttpLog> logs = Flux.just(testLog);

        // when
        Flux<HttpLog> result = httpLogsService.checkPattern(logs, XSS_PATTERNS);

        // then
        StepVerifier.create(result)
                .expectNext(testLog)
                .verifyComplete();
    }

    @Test
    void SQL_Injection_검출하는_메서드() {
        // given
        testLog = HttpLog.builder()
                .message("\"GET http://naver.com/login?user=admin' OR '1'='1 HTTP/1.1\"")
                .timestamp(testTimestamp)
                .build();
        Flux<HttpLog> logs = Flux.just(testLog);

        // when
        Flux<HttpLog> result = httpLogsService.checkPattern(logs, SQL_INJECTION_PATTERNS);

        // then
        StepVerifier.create(result)
                .expectNext(testLog)
                .verifyComplete();
    }

    @Test
    void 디렉토리_인덱스_검출_메서드() {
        // given
        testLog = HttpLog.builder()
                .message("\"GET http://google.com/ HTTP/1.1\"")
                .timestamp(testTimestamp)
                .build();
        Flux<HttpLog> logs = Flux.just(testLog);

        // when
        Flux<HttpLog> result = httpLogsService.checkDirectoryIndex(logs);

        // then
        StepVerifier.create(result)
                .expectNext(testLog)
                .verifyComplete();
    }

    @Test
    void 해당_로그_그룹의_마지막_Timestamp를_조회하는_메서드() {
        // given
        LocalDateTime newerTimestamp = LocalDateTime.now();
        HttpLog newerLog = HttpLog.builder()
                .message("\"GET http://naver.com/ HTTP/1.1\"")
                .timestamp(newerTimestamp)
                .build();

        // when
        LocalDateTime result = httpLogsService.getLatestTimestamp(List.of(testLog, newerLog), testTimestamp);

        // then
        assertEquals(newerTimestamp, result);
    }

    @Test
    void 정상적으로_로그에서_필요한_데이터를_추출하는_메서드() {
        // given
        String logEntry = "\"GET http://example.com/home HTTP/1.1\"";

        // when
        String extractedUrl = httpLogsService.extractUrl(logEntry);

        // then
        assertEquals("http://example.com/home", extractedUrl);
    }

    @Test
    void 잘못된_형식의_URL은_추출하지_않는_메서드() {
        // given
        String logEntry = "Random Log Entry without URL";

        // when
        String extractedUrl = httpLogsService.extractUrl(logEntry);

        // then
        assertNull(extractedUrl);
    }

    @Test
    void 정상적으로_디코딩하는_메서드() {
        // given
        String encodedUrl = "http%3A%2F%2Fexample.com%2Ftest%3Finput%3D%3Cscript%3Ealert%281%29%3C%2Fscript%3E";

        // when
        String decodedUrl = httpLogsService.decodeUrl(encodedUrl);

        // then
        assertEquals("http://example.com/test?input=<script>alert(1)</script>", decodedUrl);
    }
}
