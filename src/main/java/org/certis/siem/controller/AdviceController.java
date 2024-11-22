package org.certis.siem.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@RestController
public class AdviceController {

    private final WebClient webClient;

    private final Sinks.Many<String> sink;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdviceController(WebClient.Builder webClientBuilder, Sinks.Many<String> sink) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:5000").build();
        this.sink = sink;
    }

    @PostMapping("/ask")
    public Mono<String> ask(@RequestBody String message) {
        return webClient.post()
                .uri("/ask")
                .bodyValue("{\"message\":\"" + message + "\"}")
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> {
                    try {
                        String jsonMessage = objectMapper.writeValueAsString(
                                Map.of("action", "askResponse", "data", response));
                        sink.emitNext(jsonMessage, Sinks.EmitFailureHandler.FAIL_FAST);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                })
                .onErrorResume(e -> Mono.just("AI Connection Error: " + e.getMessage()));
    }
}
