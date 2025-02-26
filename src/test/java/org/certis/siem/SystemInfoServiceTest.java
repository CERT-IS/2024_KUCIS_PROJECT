package org.certis.siem;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import org.certis.siem.service.SystemInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class SystemInfoServiceTest {

    @InjectMocks private SystemInfoService systemInfoService;
    @Mock private OperatingSystemMXBean osBean;

    @Mock private RuntimeMXBean runtimeMXBean;
    @Mock private MemoryMXBean memoryMXBean;
    @Mock private MemoryUsage heapMemoryUsage;


    @BeforeEach
    void init() {
        try (var mock = mockStatic(ManagementFactory.class)) {
            mock.when(() -> ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class)).thenReturn(osBean);
            mock.when(ManagementFactory::getRuntimeMXBean).thenReturn(runtimeMXBean);
            mock.when(ManagementFactory::getMemoryMXBean).thenReturn(memoryMXBean);
        }

        when(osBean.getAvailableProcessors()).thenReturn(8);
        when(osBean.getSystemLoadAverage()).thenReturn(2.5);
        when(osBean.getFreePhysicalMemorySize()).thenReturn(4L * 1024 * 1024 * 1024);
        when(osBean.getTotalPhysicalMemorySize()).thenReturn(16L * 1024 * 1024 * 1024);

        when(runtimeMXBean.getUptime()).thenReturn(123456L);

        when(memoryMXBean.getHeapMemoryUsage()).thenReturn(heapMemoryUsage);
        when(heapMemoryUsage.getUsed()).thenReturn(2L * 1024 * 1024 * 1024);  // 2GB
        when(heapMemoryUsage.getMax()).thenReturn(8L * 1024 * 1024 * 1024);
    }

    @Test
    void WAS_시스템_정보를_표시하는_메서드() {
        StepVerifier.create(systemInfoService.getSystemInfo())
                .expectNextMatches(systemInfo -> systemInfo.containsKey("availableProcessors")
                            && systemInfo.get("availableProcessors").equals(8)
                            && systemInfo.containsKey("systemLoadAverage")
                            && systemInfo.get("systemLoadAverage").equals(2.5)
                            && systemInfo.containsKey("freeMemoryPercentage")
                            && systemInfo.containsKey("jvmUptime")
                            && systemInfo.get("jvmUptime").equals(123456L)
                            && systemInfo.containsKey("usedHeapMemory")
                            && systemInfo.get("usedHeapMemory").equals("2.0 GB")
                            && systemInfo.containsKey("maxHeapMemory")
                            && systemInfo.get("maxHeapMemory").equals("8.0 GB")
                            && systemInfo.containsKey("usedHeapPercentage"))
                .verifyComplete();
    }
}