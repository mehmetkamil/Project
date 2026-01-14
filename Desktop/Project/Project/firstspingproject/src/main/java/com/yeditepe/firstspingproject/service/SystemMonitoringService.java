package com.yeditepe.firstspingproject.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * System Monitoring Service
 * Provides scheduled system health checks and alerts
 */
@Service
public class SystemMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(SystemMonitoringService.class);

    // Thresholds
    private static final double MEMORY_WARNING_THRESHOLD = 80.0;
    private static final double MEMORY_CRITICAL_THRESHOLD = 90.0;
    private static final double DISK_WARNING_THRESHOLD = 80.0;
    private static final double DISK_CRITICAL_THRESHOLD = 90.0;
    private static final int THREAD_WARNING_THRESHOLD = 200;

    /**
     * Scheduled health check - runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void performHealthCheck() {
        logger.info("🔍 Performing scheduled health check at {}", 
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        checkMemoryUsage();
        checkThreadCount();
        checkDiskSpace();
        checkGarbageCollection();
    }

    /**
     * Check memory usage
     */
    private void checkMemoryUsage() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();

        long usedMemory = heapUsage.getUsed();
        long maxMemory = heapUsage.getMax();
        double usagePercent = (usedMemory * 100.0) / maxMemory;

        if (usagePercent >= MEMORY_CRITICAL_THRESHOLD) {
            logger.error("🚨 CRITICAL: Memory usage at {:.2f}% ({} MB / {} MB)", 
                    usagePercent, 
                    usedMemory / (1024 * 1024), 
                    maxMemory / (1024 * 1024));
            // Here you could trigger alerts, notifications, etc.
        } else if (usagePercent >= MEMORY_WARNING_THRESHOLD) {
            logger.warn("⚠️ WARNING: Memory usage at {:.2f}% ({} MB / {} MB)", 
                    usagePercent, 
                    usedMemory / (1024 * 1024), 
                    maxMemory / (1024 * 1024));
        } else {
            logger.debug("✅ Memory usage: {:.2f}% ({} MB / {} MB)", 
                    usagePercent, 
                    usedMemory / (1024 * 1024), 
                    maxMemory / (1024 * 1024));
        }
    }

    /**
     * Check thread count
     */
    private void checkThreadCount() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        int threadCount = threadMXBean.getThreadCount();
        int peakThreadCount = threadMXBean.getPeakThreadCount();

        if (threadCount >= THREAD_WARNING_THRESHOLD) {
            logger.warn("⚠️ WARNING: High thread count: {} (peak: {})", threadCount, peakThreadCount);
        } else {
            logger.debug("✅ Thread count: {} (peak: {})", threadCount, peakThreadCount);
        }

        // Check for deadlocks
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        if (deadlockedThreads != null && deadlockedThreads.length > 0) {
            logger.error("🚨 CRITICAL: Deadlock detected! {} deadlocked threads", deadlockedThreads.length);
        }
    }

    /**
     * Check disk space
     */
    private void checkDiskSpace() {
        File root = new File("/");
        if (!root.exists()) {
            root = new File("C:\\");
        }
        
        long totalSpace = root.getTotalSpace();
        long freeSpace = root.getFreeSpace();
        long usedSpace = totalSpace - freeSpace;
        double usagePercent = (usedSpace * 100.0) / totalSpace;

        if (usagePercent >= DISK_CRITICAL_THRESHOLD) {
            logger.error("🚨 CRITICAL: Disk usage at {:.2f}% ({} GB free)", 
                    usagePercent, 
                    freeSpace / (1024 * 1024 * 1024));
        } else if (usagePercent >= DISK_WARNING_THRESHOLD) {
            logger.warn("⚠️ WARNING: Disk usage at {:.2f}% ({} GB free)", 
                    usagePercent, 
                    freeSpace / (1024 * 1024 * 1024));
        } else {
            logger.debug("✅ Disk usage: {:.2f}% ({} GB free)", 
                    usagePercent, 
                    freeSpace / (1024 * 1024 * 1024));
        }
    }

    /**
     * Check garbage collection statistics
     */
    private void checkGarbageCollection() {
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            long collectionCount = gc.getCollectionCount();
            long collectionTime = gc.getCollectionTime();

            if (collectionCount > 0) {
                double avgCollectionTime = (double) collectionTime / collectionCount;
                
                if (avgCollectionTime > 500) { // More than 500ms average
                    logger.warn("⚠️ WARNING: GC '{}' average collection time: {:.2f}ms (count: {})", 
                            gc.getName(), avgCollectionTime, collectionCount);
                } else {
                    logger.debug("✅ GC '{}': {} collections, {} ms total (avg: {:.2f} ms)", 
                            gc.getName(), collectionCount, collectionTime, avgCollectionTime);
                }
            }
        }
    }

    /**
     * Get current system status summary
     */
    public SystemStatus getSystemStatus() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        double memoryUsagePercent = (heapUsage.getUsed() * 100.0) / heapUsage.getMax();
        int threadCount = threadMXBean.getThreadCount();
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();

        String status = "HEALTHY";
        if (memoryUsagePercent >= MEMORY_CRITICAL_THRESHOLD || 
            (deadlockedThreads != null && deadlockedThreads.length > 0)) {
            status = "CRITICAL";
        } else if (memoryUsagePercent >= MEMORY_WARNING_THRESHOLD || 
                   threadCount >= THREAD_WARNING_THRESHOLD) {
            status = "WARNING";
        }

        return new SystemStatus(
                status,
                memoryUsagePercent,
                heapUsage.getUsed() / (1024 * 1024),
                heapUsage.getMax() / (1024 * 1024),
                threadCount,
                deadlockedThreads != null ? deadlockedThreads.length : 0
        );
    }

    /**
     * System status record
     */
    public record SystemStatus(
            String status,
            double memoryUsagePercent,
            long usedMemoryMb,
            long maxMemoryMb,
            int threadCount,
            int deadlockedThreads
    ) {}
}
