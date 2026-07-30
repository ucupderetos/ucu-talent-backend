package ucu.retojulio2026.talent.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

// QA reporto que POST /user (alta de cuenta) no tenia ningun limite: 1000+ altas seguidas sin un
// solo 429. Misma mecanica que LoginRateLimitFilter, pero con cache/config totalmente
// independiente: una IP o email penalizados aca no afectan su limite de login, y viceversa.
@Component
public class SignupRateLimitFilter extends AbstractKeyedRateLimitFilter {

    public SignupRateLimitFilter(
            ObjectMapper objectMapper,
            @Value("${security.rate-limit.signup.enabled:true}") boolean enabled,
            @Value("${security.rate-limit.signup.per-email.capacity:3}") long emailCapacity,
            @Value("${security.rate-limit.signup.per-email.window-seconds:60}") long emailWindowSeconds,
            @Value("${security.rate-limit.signup.per-ip.capacity:3}") long ipCapacity,
            @Value("${security.rate-limit.signup.per-ip.window-seconds:60}") long ipWindowSeconds,
            @Value("${security.rate-limit.signup.cache-max-size:50000}") long cacheMaxSize,
            @Value("${security.rate-limit.signup.cache-expire-minutes:120}") long cacheExpireMinutes,
            @Value("${security.rate-limit.signup.lockout-schedule-seconds:86400,259200,604800}") String lockoutScheduleSeconds,
            @Value("${security.rate-limit.signup.lockout-reset-minutes:43200}") long lockoutResetMinutes) {
        super(objectMapper, "/user", "POST", enabled, emailCapacity, emailWindowSeconds, ipCapacity,
                ipWindowSeconds, cacheMaxSize, cacheExpireMinutes, lockoutScheduleSeconds, lockoutResetMinutes,
                "Demasiadas cuentas creadas desde este origen. Reintenta en unos segundos.");
    }
}
