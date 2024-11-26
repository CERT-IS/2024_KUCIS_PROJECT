package org.certis.siem.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Sinks;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class WebSocketConfig {

    @Bean
    public SimpleUrlHandlerMapping handlerMapping(WebSocketHandler eventWebSocketHandler) {
        return new SimpleUrlHandlerMapping(Map.of("/ws", eventWebSocketHandler), 1);
    }

    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    public Sinks.Many<String> sink() {
        return Sinks.many().multicast().directBestEffort();
    }
}
