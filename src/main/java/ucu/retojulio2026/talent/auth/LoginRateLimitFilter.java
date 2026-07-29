package ucu.retojulio2026.talent.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

@Component
public class LoginRateLimitFilter extends AbstractKeyedRateLimitFilter {

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
        super(objectMapper, "/auth/login", "POST", enabled, emailCapacity, emailWindowSeconds, ipCapacity,
                ipWindowSeconds, cacheMaxSize, cacheExpireMinutes, lockoutScheduleSeconds, lockoutResetMinutes,
                "Demasiados intentos de login. Reintenta en unos segundos.");
    }
}
