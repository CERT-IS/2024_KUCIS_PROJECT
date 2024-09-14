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

            systemInfo.put("availableProcessors", osBean.getAvailableProcessors());
            systemInfo.put("systemLoadAverage", osBean.getSystemLoadAverage());
            // 실행 대기중인 Task의 평균 수

            long freeMemory = osBean.getFreePhysicalMemorySize();
            long totalMemory = osBean.getTotalPhysicalMemorySize();
            double freeMemoryPercentage = ((double) freeMemory / totalMemory) * 100;
            systemInfo.put("freeMemory", formatMemory(freeMemory));
            systemInfo.put("totalMemory", formatMemory(totalMemory));
            systemInfo.put("freeMemoryPercentage", String.format("%.2f%%", freeMemoryPercentage));

            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            systemInfo.put("jvmUptime", runtimeMXBean.getUptime());

            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            long usedHeapMemory = heapMemoryUsage.getUsed();
            long maxHeapMemory = heapMemoryUsage.getMax();
            double usedHeapPercentage = ((double) usedHeapMemory / maxHeapMemory) * 100;
            systemInfo.put("usedHeapMemory", formatMemory(usedHeapMemory));
            systemInfo.put("maxHeapMemory",  formatMemory(maxHeapMemory));
            systemInfo.put("usedHeapPercentage",  String.format("%.2f%%", usedHeapPercentage));

            return systemInfo;
        });
    }

    private String formatMemory(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
