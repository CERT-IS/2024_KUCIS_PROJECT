package org.certis.siem.controller;

import com.sun.management.OperatingSystemMXBean;
import lombok.RequiredArgsConstructor;
import org.certis.siem.service.SystemInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemInfoController {

    private final SystemInfoService systemInfo;

    @GetMapping("/info")
    public Mono<Map<String ,Object>> getSystemInfo() {
        return systemInfo.getSystemInfo();
    }

}
