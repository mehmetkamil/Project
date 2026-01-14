package com.yeditepe.firstspingproject.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Configuration
 * Limits: 100 requests per minute per IP address
 */
@Configuration
public class RateLimitConfig {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Rate limit: 100 requests per minute
    private static final int REQUESTS_PER_MINUTE = 100;
    
    // Premium rate limit: 500 requests per minute
    private static final int PREMIUM_REQUESTS_PER_MINUTE = 500;

    /**
     * Get or create a bucket for the given key (IP address)
     */
    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, this::createStandardBucket);
    }

    /**
     * Get or create a premium bucket for authenticated users
     */
    public Bucket resolvePremiumBucket(String key) {
        return buckets.computeIfAbsent("premium_" + key, this::createPremiumBucket);
    }

    /**
     * Create a standard bucket with 100 requests per minute
     */
    private Bucket createStandardBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(REQUESTS_PER_MINUTE)
                .refillGreedy(REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Create a premium bucket with 500 requests per minute
     */
    private Bucket createPremiumBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(PREMIUM_REQUESTS_PER_MINUTE)
                .refillGreedy(PREMIUM_REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Get current bucket statistics
     */
    public Map<String, Long> getBucketStats() {
        Map<String, Long> stats = new ConcurrentHashMap<>();
        buckets.forEach((key, bucket) -> {
            stats.put(key, bucket.getAvailableTokens());
        });
        return stats;
    }

    /**
     * Clear all buckets (for testing purposes)
     */
    public void clearBuckets() {
        buckets.clear();
    }

    /**
     * Get total number of tracked IPs
     */
    public int getTrackedIpCount() {
        return buckets.size();
    }
}
