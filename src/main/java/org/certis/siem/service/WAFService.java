package org.certis.siem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.certis.siem.entity.WAF.WAFEvent;
import org.certis.siem.repository.WAFRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class WAFService {
    private final WAFRepository wafRepository;

    public Flux<WAFEvent> findAll() {
        return wafRepository.findAll();
    }


}


