package com.yeditepe.firstspingproject.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Custom Metrics Service
 * Provides application-specific metrics for monitoring
 */
@Service
public class CustomMetricsService {

    private final MeterRegistry meterRegistry;

    // Counters
    private Counter loginSuccessCounter;
    private Counter loginFailureCounter;
    private Counter registrationCounter;
    private Counter eventCreatedCounter;
    private Counter ticketPurchasedCounter;
    private Counter paymentSuccessCounter;
    private Counter paymentFailureCounter;

    // Timers
    private Timer authenticationTimer;
    private Timer eventSearchTimer;
    private Timer paymentProcessTimer;

    // Gauges
    private final AtomicInteger activeUsersGauge = new AtomicInteger(0);
    private final AtomicLong totalRevenueGauge = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicInteger> activeUsersByRole = new ConcurrentHashMap<>();

    public CustomMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void initMetrics() {
        // Initialize counters
        loginSuccessCounter = Counter.builder("auth.login.success")
                .description("Number of successful logins")
                .tag("type", "success")
                .register(meterRegistry);

        loginFailureCounter = Counter.builder("auth.login.failure")
                .description("Number of failed logins")
                .tag("type", "failure")
                .register(meterRegistry);

        registrationCounter = Counter.builder("auth.registration")
                .description("Number of user registrations")
                .register(meterRegistry);

        eventCreatedCounter = Counter.builder("events.created")
                .description("Number of events created")
                .register(meterRegistry);

        ticketPurchasedCounter = Counter.builder("tickets.purchased")
                .description("Number of tickets purchased")
                .register(meterRegistry);

        paymentSuccessCounter = Counter.builder("payments.success")
                .description("Number of successful payments")
                .tag("status", "success")
                .register(meterRegistry);

        paymentFailureCounter = Counter.builder("payments.failure")
                .description("Number of failed payments")
                .tag("status", "failure")
                .register(meterRegistry);

        // Initialize timers
        authenticationTimer = Timer.builder("auth.time")
                .description("Time taken for authentication")
                .register(meterRegistry);

        eventSearchTimer = Timer.builder("events.search.time")
                .description("Time taken for event search")
                .register(meterRegistry);

        paymentProcessTimer = Timer.builder("payments.process.time")
                .description("Time taken to process payment")
                .register(meterRegistry);

        // Initialize gauges
        Gauge.builder("users.active", activeUsersGauge, AtomicInteger::get)
                .description("Number of currently active users")
                .register(meterRegistry);

        Gauge.builder("revenue.total", totalRevenueGauge, AtomicLong::get)
                .description("Total revenue in cents")
                .register(meterRegistry);

        // Role-based active users
        activeUsersByRole.put("USER", new AtomicInteger(0));
        activeUsersByRole.put("ORGANIZER", new AtomicInteger(0));
        activeUsersByRole.put("ADMIN", new AtomicInteger(0));

        activeUsersByRole.forEach((role, count) ->
                Gauge.builder("users.active.by_role", count, AtomicInteger::get)
                        .tag("role", role)
                        .description("Active users by role")
                        .register(meterRegistry));
    }

    // Counter increment methods
    public void incrementLoginSuccess() {
        loginSuccessCounter.increment();
    }

    public void incrementLoginFailure() {
        loginFailureCounter.increment();
    }

    public void incrementRegistration() {
        registrationCounter.increment();
    }

    public void incrementEventCreated() {
        eventCreatedCounter.increment();
    }

    public void incrementTicketPurchased() {
        ticketPurchasedCounter.increment();
    }

    public void incrementPaymentSuccess() {
        paymentSuccessCounter.increment();
    }

    public void incrementPaymentFailure() {
        paymentFailureCounter.increment();
    }

    // Timer methods
    public void recordAuthenticationTime(long milliseconds) {
        authenticationTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    public void recordEventSearchTime(long milliseconds) {
        eventSearchTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    public void recordPaymentProcessTime(long milliseconds) {
        paymentProcessTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    public <T> T timeAuthentication(Supplier<T> supplier) {
        return authenticationTimer.record(supplier);
    }

    public <T> T timeEventSearch(Supplier<T> supplier) {
        return eventSearchTimer.record(supplier);
    }

    public <T> T timePaymentProcess(Supplier<T> supplier) {
        return paymentProcessTimer.record(supplier);
    }

    // Gauge methods
    public void userLoggedIn(String role) {
        activeUsersGauge.incrementAndGet();
        AtomicInteger roleCount = activeUsersByRole.get(role);
        if (roleCount != null) {
            roleCount.incrementAndGet();
        }
    }

    public void userLoggedOut(String role) {
        activeUsersGauge.decrementAndGet();
        AtomicInteger roleCount = activeUsersByRole.get(role);
        if (roleCount != null) {
            roleCount.decrementAndGet();
        }
    }

    public void addRevenue(long amountInCents) {
        totalRevenueGauge.addAndGet(amountInCents);
    }

    // Custom metric registration
    public Counter createCustomCounter(String name, String description, String... tags) {
        Counter.Builder builder = Counter.builder(name).description(description);
        for (int i = 0; i < tags.length - 1; i += 2) {
            builder.tag(tags[i], tags[i + 1]);
        }
        return builder.register(meterRegistry);
    }

    public Timer createCustomTimer(String name, String description) {
        return Timer.builder(name)
                .description(description)
                .register(meterRegistry);
    }

    // Get current metrics values
    public int getActiveUsersCount() {
        return activeUsersGauge.get();
    }

    public long getTotalRevenue() {
        return totalRevenueGauge.get();
    }
}
