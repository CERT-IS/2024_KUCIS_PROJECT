package org.certis.siem;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.certis.siem.entity.EventStream;
import org.certis.siem.entity.log.WAFLog;
import org.certis.siem.mapper.WAFMapper;
import org.certis.siem.repository.EventRepository;
import org.certis.siem.service.OpenSearchService;
import org.certis.siem.service.WAFService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class WAFServiceTest {

    @InjectMocks private WAFService wafService;
    @Mock private OpenSearchService openSearchService;
    @Mock private EventRepository eventRepository;

    private LocalDateTime lastProcessedTimestamp;
    private List<JsonNode> mockJsonNodes;
    private List<WAFLog> mockWafLogs;
    private List<EventStream<WAFLog>> mockEventStreams;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    void init() throws JsonProcessingException {
        lastProcessedTimestamp = LocalDateTime.now();

        mockJsonNodes = List.of(
                objectMapper.readTree("{\"id\":\"1\",\"logGroup\":\"aws-waf-logs\",\"timestamp\":\"2025-02-26T12:00:00\",\"terminatingRuleType\":\"RULE_1\",\"terminatingRuleId\":\"ID_1\",\"action\":\"ALLOW\",\"country\":\"US\",\"clientIp\":\"192.168.1.1\"}"),
                objectMapper.readTree("{\"id\":\"2\",\"logGroup\":\"aws-waf-logs\",\"timestamp\":\"2025-02-26T12:05:00\",\"terminatingRuleType\":\"RULE_2\",\"terminatingRuleId\":\"ID_2\",\"action\":\"BLOCK\",\"country\":\"CA\",\"clientIp\":\"192.168.1.2\"}")
        );

        mockWafLogs = mockJsonNodes.stream().map(WAFMapper::mapJsonNodeToWAFEvent).toList();
        mockEventStreams = mockWafLogs.stream()
                .map(wafLog -> EventStream.<WAFLog>builder()
                        .logs(List.of(wafLog))
                        .eventName("WAF EventStream")
                        .eventType("WAF").build())
                .toList();
    }

    @Test
    void 로그가_존재하는_경우_보안_위협을_탐지하는_프로세스_동작() {
        when(openSearchService.executeSearch(any())).thenReturn(Flux.fromIterable(mockJsonNodes));
        when(eventRepository.saveAll(Flux.empty())).thenReturn(Flux.fromIterable(mockEventStreams));

        StepVerifier.create(wafService.process(lastProcessedTimestamp))
                .expectNextMatches(newTimestamp -> newTimestamp.isAfter(lastProcessedTimestamp) || newTimestamp.equals(lastProcessedTimestamp))
                .verifyComplete();

        verify(openSearchService).executeSearch(any());
        verify(eventRepository).saveAll(Flux.empty());
    }

    @Test
    void 로그가_존재하지_않는_경우_보안_위협을_탐지하는_프로세스_동작() {
        when(openSearchService.executeSearch(any())).thenReturn(Flux.empty());

        StepVerifier.create(wafService.process(lastProcessedTimestamp))
                .expectNext(lastProcessedTimestamp)
                .verifyComplete();

        verify(openSearchService).executeSearch(any());
        verify(eventRepository, never()).saveAll(Flux.empty());
    }

    @Test
    void WAF_로그가_쌓인_상태에서_마지막_Timestamp를_조회하는_메서드() {
        LocalDateTime latestTimestamp = lastProcessedTimestamp.plusMinutes(10);

        WAFLog updatedWafLog1 = WAFLog.builder()
                .id(mockWafLogs.get(0).getId())
                .timestamp(lastProcessedTimestamp)
                .logGroup(mockWafLogs.get(0).getLogGroup())
                .terminatingRuleType(mockWafLogs.get(0).getTerminatingRuleType())
                .terminatingRuleId(mockWafLogs.get(0).getTerminatingRuleId())
                .action(mockWafLogs.get(0).getAction())
                .country(mockWafLogs.get(0).getCountry())
                .clientIp(mockWafLogs.get(0).getClientIp())
                .build();

        WAFLog updatedWafLog2 = WAFLog.builder()
                .id(mockWafLogs.get(1).getId())
                .timestamp(latestTimestamp)
                .logGroup(mockWafLogs.get(1).getLogGroup())
                .terminatingRuleType(mockWafLogs.get(1).getTerminatingRuleType())
                .terminatingRuleId(mockWafLogs.get(1).getTerminatingRuleId())
                .action(mockWafLogs.get(1).getAction())
                .country(mockWafLogs.get(1).getCountry())
                .clientIp(mockWafLogs.get(1).getClientIp())
                .build();

        List<WAFLog> updatedWafLogs = List.of(updatedWafLog1, updatedWafLog2);

        LocalDateTime result = wafService.getLatestTimestamp(updatedWafLogs, lastProcessedTimestamp);
        assert result.equals(latestTimestamp);
    }

    @Test
    void 로그_없이_비어있으면_당연히_아무것도_작업하지_않는다() {
        LocalDateTime result = wafService.getLatestTimestamp(List.of(), lastProcessedTimestamp);
        assert result.equals(lastProcessedTimestamp);
    }

    @Test
    void 예상값과_검출한_Json_데이터의_개수가_같은지_검증하는_메서드() {
        Flux<EventStream> eventStreamFlux = wafService.mapJsonToEventStream(mockJsonNodes);

        StepVerifier.create(eventStreamFlux)
                .expectNextCount(mockJsonNodes.size())
                .verifyComplete();
    }
}
