package com.pradeep.aicareerplatform.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimiterService {

    private record Window(AtomicInteger count, long windowStartEpochSeconds) {}

    private final ConcurrentHashMap<String, Window> attempts = new ConcurrentHashMap<>();

    public boolean tryAcquire(String key, int maxAttempts, long windowSeconds) {
        long now = Instant.now().getEpochSecond();

        Window window = attempts.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStartEpochSeconds() > windowSeconds) {
                return new Window(new AtomicInteger(1), now);
            }
            existing.count().incrementAndGet();
            return existing;
        });

        return window.count().get() <= maxAttempts;
    }

    public void reset(String key) {
        attempts.remove(key);
    }
}