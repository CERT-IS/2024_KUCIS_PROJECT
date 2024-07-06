package org.certis.siem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.certis.siem.dto.RegionRequest;
import org.certis.siem.entity.CloudTrail.CloudTrailEvent;
import org.certis.siem.entity.WAF.WAFEvent;
import org.certis.siem.repository.CloudTrailRepository;
import org.certis.siem.repository.WAFRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WAFService {
    private final WAFRepository wafRepository;

    public Flux<WAFEvent> findAll() {
        return wafRepository.findAll();
    }


}


