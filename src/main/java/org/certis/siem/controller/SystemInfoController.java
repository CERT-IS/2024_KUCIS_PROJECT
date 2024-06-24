package org.certis.siem.controller;

import com.sun.management.OperatingSystemMXBean;
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
public class SystemInfoController {

    @GetMapping("/info")
    public Mono<Map<String ,Object>> getSystemInfo() {
        return Mono.fromSupplier(() -> {
            Map<String, Object> systemInfo = new HashMap<>();

            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

            // CPU - 가용 프로세서(코어)의 수와 평균 부하(실행 대기중인 Task의 평균 수)
            systemInfo.put("availableProcessors", osBean.getAvailableProcessors());
            systemInfo.put("systemLoadAverage", osBean.getSystemLoadAverage());

            // memory - 전체 물리 메모리와 가용 물리 메모리
            systemInfo.put("totalPhysicalMemory", osBean.getTotalPhysicalMemorySize());
            systemInfo.put("freePhysicalMemory", osBean.getFreePhysicalMemorySize());

            // JVM 실행부터 현재까지 ms초, 현재 사용중인 힙 메모리의 양, 힙 메모리의 최대 크기

            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            systemInfo.put("jvmUptime", runtimeMXBean.getUptime());

            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            systemInfo.put("usedHeapMemory", heapMemoryUsage.getUsed());
            systemInfo.put("maxHeapMemory", heapMemoryUsage.getMax());

            return systemInfo;
        });
    }
}

