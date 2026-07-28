package ucu.retojulio2026.talent.auth;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import tools.jackson.databind.ObjectMapper;
import ucu.retojulio2026.talent.auth.dto.LoginRequest;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/auth/login";
    private static final String LOGIN_METHOD = "POST";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String EMAIL_FALLBACK_KEY = "__missing_or_invalid_email__";
    private static final String IP_KEY_PREFIX = "ip:";
    private static final String EMAIL_KEY_PREFIX = "email:";

    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final long emailCapacity;
    private final Duration emailWindow;
    private final long ipCapacity;
    private final Duration ipWindow;
    private final Cache<String, Bucket> emailBuckets;
    private final Cache<String, Bucket> ipBuckets;
    private final Duration[] lockoutSchedule;
    private final Cache<String, LockoutState> lockouts;

    public LoginRateLimitFilter(
            ObjectMapper objectMapper,
            @Value("${security.rate-limit.login.enabled:true}") boolean enabled,
            @Value("${security.rate-limit.login.per-email.capacity:5}") long emailCapacity,
            @Value("${security.rate-limit.login.per-email.window-seconds:60}") long emailWindowSeconds,
            @Value("${security.rate-limit.login.per-ip.capacity:20}") long ipCapacity,
            @Value("${security.rate-limit.login.per-ip.window-seconds:60}") long ipWindowSeconds,
            @Value("${security.rate-limit.login.cache-max-size:50000}") long cacheMaxSize,
            @Value("${security.rate-limit.login.cache-expire-minutes:120}") long cacheExpireMinutes,
            @Value("${security.rate-limit.login.lockout-schedule-seconds:30,180,900}") String lockoutScheduleSeconds,
            @Value("${security.rate-limit.login.lockout-reset-minutes:1440}") long lockoutResetMinutes) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.emailCapacity = emailCapacity;
        this.emailWindow = Duration.ofSeconds(emailWindowSeconds);
        this.ipCapacity = ipCapacity;
        this.ipWindow = Duration.ofSeconds(ipWindowSeconds);
        this.emailBuckets = Caffeine.newBuilder()
                .maximumSize(cacheMaxSize)
                .expireAfterAccess(Duration.ofMinutes(cacheExpireMinutes))
                .build();
        this.ipBuckets = Caffeine.newBuilder()
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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path == null || path.isBlank()) {
            path = request.getRequestURI();
        }
        return !enabled
                || !LOGIN_METHOD.equalsIgnoreCase(request.getMethod())
                || !LOGIN_PATH.equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, jakarta.servlet.ServletException {
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);

        String clientIp = resolveClientIp(wrappedRequest);
        String email = extractEmail(wrappedRequest.getCachedBody());
        String ipKey = IP_KEY_PREFIX + clientIp;
        String emailKey = EMAIL_KEY_PREFIX + email;

        long now = System.nanoTime();
        
        long lockedSeconds = Math.max(
                remainingLockoutSeconds(ipKey, now),
                remainingLockoutSeconds(emailKey, now));
        if (lockedSeconds > 0) {
            writeRateLimitedResponse(response, lockedSeconds);
            return;
        }

        Bucket ipBucket = ipBuckets.get(clientIp, ignored -> newBucket(ipCapacity, ipWindow));
        Bucket emailBucket = emailBuckets.get(email, ignored -> newBucket(emailCapacity, emailWindow));

        // Consumo de ambos límites: por IP y por email.
        boolean ipConsumed = ipBucket.tryConsume(1);
        boolean emailConsumed = emailBucket.tryConsume(1);

        if (!ipConsumed || !emailConsumed) {
            long retryAfterSeconds = 0;
            if (!ipConsumed) {
                retryAfterSeconds = Math.max(retryAfterSeconds, escalateLockout(ipKey, now));
            }
            if (!emailConsumed) {
                retryAfterSeconds = Math.max(retryAfterSeconds, escalateLockout(emailKey, now));
            }
            writeRateLimitedResponse(response, retryAfterSeconds);
            return;
        }

        filterChain.doFilter(wrappedRequest, response);
    }

    private Bucket newBucket(long capacity, Duration window) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, window)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private long remainingLockoutSeconds(String key, long nowNanos) {
        LockoutState state = lockouts.getIfPresent(key);
        if (state == null) {
            return 0;
        }
        long remainingNanos = state.lockedUntilNanos() - nowNanos;
        return remainingNanos <= 0 ? 0 : (long) Math.ceil(remainingNanos / 1_000_000_000.0);
    }

    private long escalateLockout(String key, long nowNanos) {
        LockoutState previous = lockouts.getIfPresent(key);
        int strikes = (previous == null ? 0 : previous.strikes()) + 1;
        Duration penalty = lockoutSchedule[Math.min(strikes - 1, lockoutSchedule.length - 1)];
        lockouts.put(key, new LockoutState(strikes, nowNanos + penalty.toNanos()));
        return penalty.toSeconds();
    }

    private record LockoutState(int strikes, long lockedUntilNanos) {
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] values = forwardedFor.split(",");
            if (values.length > 0 && !values[0].isBlank()) {
                return values[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private String extractEmail(byte[] requestBody) {
        if (requestBody == null || requestBody.length == 0) {
            return EMAIL_FALLBACK_KEY;
        }
        try {
            LoginRequest loginRequest = objectMapper.readValue(requestBody, LoginRequest.class);
            if (loginRequest == null) {
                return EMAIL_FALLBACK_KEY;
            }
            String value = loginRequest.email();
            if (value == null || value.isBlank()) {
                return EMAIL_FALLBACK_KEY;
            }
            return value.trim().toLowerCase();
        } catch (Exception ignored) {
            return EMAIL_FALLBACK_KEY;
        }
    }

    private void writeRateLimitedResponse(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS,
                "Demasiados intentos de login. Reintenta en unos segundos.");
        objectMapper.writeValue(response.getOutputStream(), problem);
    }

    private static final class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

        private final byte[] cachedBody;

        private CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            try (InputStream inputStream = request.getInputStream()) {
                this.cachedBody = inputStream.readAllBytes();
            }
        }

        private byte[] getCachedBody() {
            return cachedBody;
        }

        @Override
        public ServletInputStream getInputStream() {
            return new CachedBodyServletInputStream(cachedBody);
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }

    private static final class CachedBodyServletInputStream extends ServletInputStream {

        private final InputStream delegate;

        private CachedBodyServletInputStream(byte[] cachedBody) {
            this.delegate = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public boolean isFinished() {
            try {
                return delegate.available() == 0;
            } catch (IOException ignored) {
                return true;
            }
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            // No-op: stream sin IO asíncrono.
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }
    }
}
