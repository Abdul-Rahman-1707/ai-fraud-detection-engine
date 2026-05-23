package com.portfolio.frauddetection.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class VelocityTracker {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String VELOCITY_KEY_PREFIX = "fraud:velocity:";
    private static final String AMOUNT_KEY_PREFIX = "fraud:amount:";
    private static final Duration WINDOW = Duration.ofMinutes(10);

    public long trackAndGetCount(String userId) {
        String key = VELOCITY_KEY_PREFIX + userId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, WINDOW.toSeconds(), TimeUnit.SECONDS);
        }
        log.debug("Velocity count for user {}: {}", userId, count);
        return count != null ? count : 0;
    }

    public double trackAmountAndGetTotal(String userId, double amount) {
        String key = AMOUNT_KEY_PREFIX + userId;
        Double currentTotal = (Double) redisTemplate.opsForValue().get(key);
        double newTotal = (currentTotal != null ? currentTotal : 0.0) + amount;
        redisTemplate.opsForValue().set(key, newTotal, WINDOW.toSeconds(), TimeUnit.SECONDS);
        log.debug("Amount total for user {} in window: ${}", userId, newTotal);
        return newTotal;
    }

    public boolean isVelocityBreached(String userId, int maxTransactions) {
        long count = trackAndGetCount(userId);
        return count > maxTransactions;
    }

    public boolean isAmountThresholdBreached(String userId, double amount, double maxWindowAmount) {
        double total = trackAmountAndGetTotal(userId, amount);
        return total > maxWindowAmount;
    }

    public void resetUserVelocity(String userId) {
        redisTemplate.delete(VELOCITY_KEY_PREFIX + userId);
        redisTemplate.delete(AMOUNT_KEY_PREFIX + userId);
        log.info("Reset velocity tracking for user {}", userId);
    }
}
