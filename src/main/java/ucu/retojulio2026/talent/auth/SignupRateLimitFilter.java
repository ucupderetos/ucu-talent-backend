package ucu.retojulio2026.talent.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

@Component
public class SignupRateLimitFilter extends AbstractKeyedRateLimitFilter {

    public SignupRateLimitFilter(
            ObjectMapper objectMapper,
            @Value("${security.rate-limit.signup.enabled:true}") boolean enabled,
            @Value("${security.rate-limit.signup.per-email.capacity:3}") long emailCapacity,
            @Value("${security.rate-limit.signup.per-email.window-seconds:60}") long emailWindowSeconds,
            @Value("${security.rate-limit.signup.per-ip.capacity:10}") long ipCapacity,
            @Value("${security.rate-limit.signup.per-ip.window-seconds:60}") long ipWindowSeconds,
            @Value("${security.rate-limit.signup.cache-max-size:50000}") long cacheMaxSize,
            @Value("${security.rate-limit.signup.cache-expire-minutes:120}") long cacheExpireMinutes,
            @Value("${security.rate-limit.signup.per-ip.lockout-schedule-seconds:60,300,1800}") String ipLockoutScheduleSeconds,
            @Value("${security.rate-limit.signup.per-email.lockout-schedule-seconds:900,3600,86400}") String emailLockoutScheduleSeconds,
            @Value("${security.rate-limit.signup.lockout-reset-minutes:43200}") long lockoutResetMinutes) {
        super(objectMapper, "/user", "POST", enabled, emailCapacity, emailWindowSeconds, ipCapacity,
                ipWindowSeconds, cacheMaxSize, cacheExpireMinutes, ipLockoutScheduleSeconds,
                emailLockoutScheduleSeconds, lockoutResetMinutes,
                "Demasiadas cuentas creadas desde este origen.");
    }
}
