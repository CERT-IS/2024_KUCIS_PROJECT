package org.certis.siem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.certis.siem.service.EventDetectService;
import org.certis.siem.service.SystemInfoService;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EventWebSocketHandler implements WebSocketHandler {
    private final Sinks.Many<String> sink;
    private final EventDetectService eventDetectService;
    private final SystemInfoService systemInfoService;
    private final ObjectMapper objectMapper;


    @Override
    public Mono<Void> handle(WebSocketSession session) {
        var output = session.receive()
                .map(e -> e.getPayloadAsText())
                .flatMap(e -> {
                    try {
                        Map<String, Object> json = objectMapper.readValue(e, Map.class);
                        String action = (String) json.get("action");
                        switch (action) {
                            case "getWAFEvents":
                                return handleGetWAFEvents(json, session);
                            case "getHandmadeEvents":
                                return handleGetHandmadeEvents(json, session);
                            case "getSystemInfo":
                                return handleGetSystemInfo(session);
                            default:
                                return Mono.empty();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return Mono.just("{\"error\":\"메시지 처리 중 오류 발생\"}");
                    }
                });

        output.subscribe(s -> sink.emitNext((String) s, Sinks.EmitFailureHandler.FAIL_FAST));

        return session.send(sink.asFlux().map(session::textMessage));
    }

    private Mono<Void> handleGetSystemInfo(WebSocketSession session) {
        return systemInfoService.getSystemInfo()
                .map(systemInfo -> {
                    try {
                        String systemInfoJson = objectMapper.writeValueAsString(systemInfo);
                        Map<String, Object> response = Map.of(
                                "action", "sendSystemInfo",
                                "data", systemInfoJson
                        );
                        return objectMapper.writeValueAsString(response);
                    } catch (JsonProcessingException e) {
                        return "{\"error\":\"Failed to parse system info\"}";
                    }
                })
                .flatMap(systemInfoJson -> session.send(Mono.just(session.textMessage(systemInfoJson))))
                .then();
    }

    private Mono<Void> handleGetWAFEvents(Map<String, Object> json, WebSocketSession session) {
        String lastTimestampStr = (String) json.get("lastTimestamp");
        Instant lastTimestamp = (lastTimestampStr != "null") ? Instant.parse(lastTimestampStr) : Instant.now();
        int size = (int) json.get("size");
        int offset = (int) json.get("offset");

        return eventDetectService.findByEventType(lastTimestamp, size, offset, "WAF")
                .collectList()
                .flatMap(eventStreams -> {
                    try {
                        String eventStreamJson = objectMapper.writeValueAsString(eventStreams);
                        Map<String, Object> response = Map.of(
                                "action", "getWAFEvents",
                                "data", eventStreamJson
                        );
                        return Mono.just(objectMapper.writeValueAsString(response));
                    } catch (JsonProcessingException e) {
                        return Mono.just("{\"error\":\"Failed to parse WAF event\"}");
                    }
                })
                .flatMap(eventJson -> session.send(Mono.just(session.textMessage(eventJson))))
                .then();
    }

    private Mono<Void> handleGetHandmadeEvents(Map<String, Object> json, WebSocketSession session) {
        String lastTimestampStr = (String) json.get("lastTimestamp");
        Instant lastTimestamp = (lastTimestampStr != "null") ? Instant.parse(lastTimestampStr) : Instant.now();
        int size = (int) json.get("size");
        int offset = (int) json.get("offset");

        return eventDetectService.findByEventTypeNot(lastTimestamp, size, offset, "WAF")
                .collectList()
                .flatMap(eventStreams -> {
                    try {
                        String eventStreamJson = objectMapper.writeValueAsString(eventStreams);
                        Map<String, Object> response = Map.of(
                                "action", "getHandmadeEvents",
                                "data", eventStreamJson
                        );
                        return Mono.just(objectMapper.writeValueAsString(response));
                    } catch (JsonProcessingException e) {
                        return Mono.just("{\"error\":\"Failed to parse WAF event\"}");
                    }
                })
                .flatMap(eventJson -> session.send(Mono.just(session.textMessage(eventJson))))
                .then();
    }

}


