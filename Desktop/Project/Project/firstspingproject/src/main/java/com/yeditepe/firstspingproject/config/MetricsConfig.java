package com.yeditepe.firstspingproject.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Metrics Configuration
 * Configures Micrometer metrics for monitoring
 */
@Configuration
@EnableAspectJAutoProxy
public class MetricsConfig {

    /**
     * Customize meter registry with common tags
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags(Tags.of(
                        "application", "eventplanner",
                        "environment", getEnvironment()
                ));
    }

    /**
     * Enable @Timed annotation support
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    private String getEnvironment() {
        String env = System.getProperty("spring.profiles.active");
        return env != null ? env : "development";
    }
}
