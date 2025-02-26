package org.certis.siem;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.certis.siem.entity.EventStream;
import org.certis.siem.entity.log.CloudTrailLog;
import org.certis.siem.repository.EventRepository;
import org.certis.siem.service.CloudTrailService;
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
class CloudTrailServiceTest {

    @Mock private OpenSearchService openSearchService;
    @Mock private EventRepository eventRepository;
    @InjectMocks private CloudTrailService cloudTrailService;

    private LocalDateTime testTimestamp;


    @BeforeEach
    void init() {
        testTimestamp = LocalDateTime.now().minusHours(1);
    }


    @Test
    void 보안_위협을_탐지하는_프로세스_동작() {
        // given
        List<JsonNode> mockJsonNodes = List.of(mock(JsonNode.class));
        List<CloudTrailLog> mockCloudTrailLogs = List.of(mock(CloudTrailLog.class));
        List<EventStream> mockEventStreams = List.of(mock(EventStream.class));

        when(openSearchService.executeSearch(any())).thenReturn(Flux.fromIterable(mockJsonNodes));
        when(eventRepository.saveAll(Flux.empty())).thenReturn(Flux.empty());

        // when
        Mono<LocalDateTime> result = cloudTrailService.process(testTimestamp);

        // then
        StepVerifier.create(result)
                .expectNextMatches(timestamp -> !timestamp.isBefore(testTimestamp))
                .verifyComplete();

        verify(openSearchService).executeSearch(any());
        verify(eventRepository).saveAll(Flux.empty());
    }

    @Test
    void 인가받지_않은_로그인_시도에_대한_검출_메서드() {
        // given
        CloudTrailLog log = mock(CloudTrailLog.class);
        when(log.getResponseElements()).thenReturn(Map.of("ConsoleLogin", "Failure"));

        // when
        boolean result = cloudTrailService.isFailedLogin(log);

        // then
        assertTrue(result);
    }

    @Test
    void 허용되지_않은_리젼에서_수행된_작업인지_검출_메서드() {
        // given
        CloudTrailLog log = mock(CloudTrailLog.class);
        when(log.getAwsRegion()).thenReturn("ap-northeast-2");

        // when
        boolean result = cloudTrailService.isRegionExcluded(List.of("ap-northeast-2"), log);

        // then
        assertFalse(result);
    }

    @Test
    void IAM_정책상_와일드카드를_포함하는지_검출_메서드() {
        // given
        CloudTrailLog log = mock(CloudTrailLog.class);
        when(log.getRequestParameters()).thenReturn(Map.of("policyDocument", "{\"Action\": \"*\"}"));

        // when
        boolean result = cloudTrailService.containsWildcard(log);

        // then
        assertTrue(result);
    }
}
