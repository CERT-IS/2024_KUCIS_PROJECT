package org.certis.siem.handler;

import static org.certis.siem.controller.AdviceController.webClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.certis.siem.service.EventDetectService;
import org.certis.siem.service.SystemInfoService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Slf4j
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

                        log.info("handle message : " + json); // TEST

                        switch (action) {
                            case "getWAFEvents":
                                return handleGetWAFEvents(json, session);
                            case "getHandmadeEvents":
                                return handleGetHandmadeEvents(json, session);
                            case "getSystemInfo":
                                return handleGetSystemInfo(session);
                            case "getChatMessage":
                                return handleChatMessage(json, session);
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

    private Mono<Void> handleChatMessage(Map<String, Object> json, WebSocketSession session){
        String message = (String) json.get("message");

        if(message == null || message.isBlank()){
            return session.send(Mono.just(session.textMessage("{\"error\":\"blank message\"}")));
        }

        System.out.println("handleChatMessage sendToModel : " + message);
        return sendToModel(message)
                .flatMap(response -> session.send(Mono.just(session.textMessage(response))))
                .then();
    }

    private Mono<String> sendToModel(String message){
        return // Mono.just("test chat message")
                webClient.post()
                .uri("/ask")
                .bodyValue(Map.of("message", message))
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    log.info("askResponse : " + response); // TEST
                    try {
                        Map<String, String> responseJson = Map.of(
                                "action", "askResponse",
                                "data", response
                        );
                        return objectMapper.writeValueAsString(responseJson);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return "{\"error\":\"processing error\"}";
                    }
                });
    }
}


