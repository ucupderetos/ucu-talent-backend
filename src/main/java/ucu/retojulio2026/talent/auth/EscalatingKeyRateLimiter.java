package ucu.retojulio2026.talent.auth;

import java.time.Duration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

final class EscalatingKeyRateLimiter {

    private final Cache<String, Bucket> buckets;
    private final Cache<String, LockoutState> lockouts;
    private final Duration[] lockoutSchedule;

    EscalatingKeyRateLimiter(long cacheMaxSize, long cacheExpireMinutes, String lockoutScheduleSeconds,
            long lockoutResetMinutes) {
        this.buckets = Caffeine.newBuilder()
                .maximumSize(cacheMaxSize)
                .expireAfterAccess(Duration.ofMinutes(cacheExpireMinutes))
                .build();
        this.lockoutSchedule = parseLockoutSchedule(lockoutScheduleSeconds);
        this.lockouts = Caffeine.newBuilder()
                .maximumSize(cacheMaxSize)
                .expireAfterWrite(Duration.ofMinutes(lockoutResetMinutes))
                .build();
    }

    private static Duration[] parseLockoutSchedule(String csv) {
        String[] parts = csv.split(",");
        Duration[] schedule = new Duration[parts.length];
        for (int i = 0; i < parts.length; i++) {
            schedule[i] = Duration.ofSeconds(Long.parseLong(parts[i].trim()));
        }
        return schedule;
    }

    long remainingLockoutSeconds(String key, long nowNanos) {
        LockoutState state = lockouts.getIfPresent(key);
        if (state == null) {
            return 0;
        }
        long remainingNanos = state.lockedUntilNanos() - nowNanos;
        return remainingNanos <= 0 ? 0 : (long) Math.ceil(remainingNanos / 1_000_000_000.0);
    }

    boolean tryConsume(String key, long capacity, Duration window) {
        Bucket bucket = buckets.get(key, ignored -> newBucket(capacity, window));
        return bucket.tryConsume(1);
    }

    long escalateLockout(String key, long nowNanos) {
        LockoutState previous = lockouts.getIfPresent(key);
        int strikes = (previous == null ? 0 : previous.strikes()) + 1;
        Duration penalty = lockoutSchedule[Math.min(strikes - 1, lockoutSchedule.length - 1)];
        lockouts.put(key, new LockoutState(strikes, nowNanos + penalty.toNanos()));
        return penalty.toSeconds();
    }

    private static Bucket newBucket(long capacity, Duration window) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, window)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private record LockoutState(int strikes, long lockedUntilNanos) {
    }
}
