package com.yeditepe.firstspingproject.config;

import com.yeditepe.firstspingproject.repository.EventRepository;
import com.yeditepe.firstspingproject.repository.PaymentRepository;
import com.yeditepe.firstspingproject.repository.TicketRepository;
import com.yeditepe.firstspingproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Custom Health Indicators for Application Monitoring
 */
public class HealthIndicators {

    /**
     * Database Health Indicator
     */
    @Component("databaseHealth")
    public static class DatabaseHealthIndicator implements HealthIndicator {

        @Autowired
        private DataSource dataSource;

        @Override
        public Health health() {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(2)) {
                    return Health.up()
                            .withDetail("database", "H2")
                            .withDetail("status", "Connection is valid")
                            .withDetail("catalog", connection.getCatalog())
                            .build();
                }
            } catch (SQLException e) {
                return Health.down()
                        .withDetail("error", e.getMessage())
                        .build();
            }
            return Health.down().build();
        }
    }

    /**
     * User Service Health Indicator
     */
    @Component("userServiceHealth")
    public static class UserServiceHealthIndicator implements HealthIndicator {

        @Autowired
        private UserRepository userRepository;

        @Override
        public Health health() {
            try {
                long userCount = userRepository.count();
                return Health.up()
                        .withDetail("service", "UserService")
                        .withDetail("totalUsers", userCount)
                        .withDetail("status", "Operational")
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("service", "UserService")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        }
    }

    /**
     * Event Service Health Indicator
     */
    @Component("eventServiceHealth")
    public static class EventServiceHealthIndicator implements HealthIndicator {

        @Autowired
        private EventRepository eventRepository;

        @Override
        public Health health() {
            try {
                long eventCount = eventRepository.count();
                return Health.up()
                        .withDetail("service", "EventService")
                        .withDetail("totalEvents", eventCount)
                        .withDetail("status", "Operational")
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("service", "EventService")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        }
    }

    /**
     * Ticket Service Health Indicator
     */
    @Component("ticketServiceHealth")
    public static class TicketServiceHealthIndicator implements HealthIndicator {

        @Autowired
        private TicketRepository ticketRepository;

        @Override
        public Health health() {
            try {
                long ticketCount = ticketRepository.count();
                return Health.up()
                        .withDetail("service", "TicketService")
                        .withDetail("totalTickets", ticketCount)
                        .withDetail("status", "Operational")
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("service", "TicketService")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        }
    }

    /**
     * Payment Service Health Indicator
     */
    @Component("paymentServiceHealth")
    public static class PaymentServiceHealthIndicator implements HealthIndicator {

        @Autowired
        private PaymentRepository paymentRepository;

        @Override
        public Health health() {
            try {
                long paymentCount = paymentRepository.count();
                return Health.up()
                        .withDetail("service", "PaymentService")
                        .withDetail("totalPayments", paymentCount)
                        .withDetail("status", "Operational")
                        .build();
            } catch (Exception e) {
                return Health.down()
                        .withDetail("service", "PaymentService")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        }
    }

    /**
     * Memory Health Indicator
     */
    @Component("memoryHealth")
    public static class MemoryHealthIndicator implements HealthIndicator {

        @Override
        public Health health() {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

            Health.Builder builder = memoryUsagePercent < 90 ? Health.up() : Health.down();
            
            return builder
                    .withDetail("max_memory_mb", maxMemory / (1024 * 1024))
                    .withDetail("total_memory_mb", totalMemory / (1024 * 1024))
                    .withDetail("free_memory_mb", freeMemory / (1024 * 1024))
                    .withDetail("used_memory_mb", usedMemory / (1024 * 1024))
                    .withDetail("memory_usage_percent", String.format("%.2f%%", memoryUsagePercent))
                    .build();
        }
    }

    /**
     * Disk Space Health Indicator
     */
    @Component("diskSpaceHealth")
    public static class DiskSpaceHealthIndicator implements HealthIndicator {

        @Override
        public Health health() {
            java.io.File root = new java.io.File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double usagePercent = (double) usedSpace / totalSpace * 100;

            // Warn if disk usage is above 90%
            Health.Builder builder = usagePercent < 90 ? Health.up() : Health.down();

            return builder
                    .withDetail("total_space_gb", totalSpace / (1024 * 1024 * 1024))
                    .withDetail("free_space_gb", freeSpace / (1024 * 1024 * 1024))
                    .withDetail("used_space_gb", usedSpace / (1024 * 1024 * 1024))
                    .withDetail("usage_percent", String.format("%.2f%%", usagePercent))
                    .build();
        }
    }
}
