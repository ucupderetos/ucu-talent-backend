package ucu.retojulio2026.talent.auth;

import java.io.IOException;
import java.time.Duration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.filter.OncePerRequestFilter;

import tools.jackson.databind.ObjectMapper;

// Base compartida por LoginRateLimitFilter y SignupRateLimitFilter: doble limite (IP + email) con
// bloqueo progresivo, cada uno protegiendo un unico (metodo, path) con su propia config/estado.
abstract class AbstractKeyedRateLimitFilter extends OncePerRequestFilter {

    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String EMAIL_FALLBACK_KEY = "__missing_or_invalid_email__";
    private static final String IP_KEY_PREFIX = "ip:";
    private static final String EMAIL_KEY_PREFIX = "email:";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ObjectMapper objectMapper;
    private final String path;
    private final String method;
    private final boolean enabled;
    private final long emailCapacity;
    private final Duration emailWindow;
    private final long ipCapacity;
    private final Duration ipWindow;
    private final EscalatingKeyRateLimiter ipLimiter;
    private final EscalatingKeyRateLimiter emailLimiter;
    private final String tooManyRequestsMessage;

    protected AbstractKeyedRateLimitFilter(
            ObjectMapper objectMapper,
            String path,
            String method,
            boolean enabled,
            long emailCapacity,
            long emailWindowSeconds,
            long ipCapacity,
            long ipWindowSeconds,
            long cacheMaxSize,
            long cacheExpireMinutes,
            String ipLockoutScheduleSeconds,
            String emailLockoutScheduleSeconds,
            long lockoutResetMinutes,
            String tooManyRequestsMessage) {
        this.objectMapper = objectMapper;
        this.path = path;
        this.method = method;
        this.enabled = enabled;
        this.emailCapacity = emailCapacity;
        this.emailWindow = Duration.ofSeconds(emailWindowSeconds);
        this.ipCapacity = ipCapacity;
        this.ipWindow = Duration.ofSeconds(ipWindowSeconds);
        this.ipLimiter = new EscalatingKeyRateLimiter(cacheMaxSize, cacheExpireMinutes, ipLockoutScheduleSeconds,
                lockoutResetMinutes);
        this.emailLimiter = new EscalatingKeyRateLimiter(cacheMaxSize, cacheExpireMinutes, emailLockoutScheduleSeconds,
                lockoutResetMinutes);
        this.tooManyRequestsMessage = tooManyRequestsMessage;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestPath = request.getServletPath();
        if (requestPath == null || requestPath.isBlank()) {
            requestPath = request.getRequestURI();
        }
        return !enabled
                || !method.equalsIgnoreCase(request.getMethod())
                || !path.equals(requestPath);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, jakarta.servlet.ServletException {
        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);

        String clientIp = RateLimitSupport.resolveClientIp(request, FORWARDED_FOR_HEADER);
        String email = RateLimitSupport.extractJsonField(
                objectMapper, wrappedRequest.getCachedBody(), "email", EMAIL_FALLBACK_KEY);
        String ipKey = IP_KEY_PREFIX + clientIp;
        String emailKey = EMAIL_KEY_PREFIX + email;

        long now = System.nanoTime();

        // Si ya esta en penitencia, ni siquiera tocamos los buckets: se mantiene bloqueado hasta
        // que venza el castigo actual (que crece cada vez que vuelve a exceder el limite).
        long lockedSeconds = Math.max(
                ipLimiter.remainingLockoutSeconds(ipKey, now),
                emailLimiter.remainingLockoutSeconds(emailKey, now));
        if (lockedSeconds > 0) {
            logBlocked("castigo-vigente", clientIp, email, lockedSeconds);
            writeRateLimitedResponse(response, lockedSeconds);
            return;
        }

        boolean ipConsumed = ipLimiter.tryConsume(ipKey, ipCapacity, ipWindow);
        boolean emailConsumed = emailLimiter.tryConsume(emailKey, emailCapacity, emailWindow);

        if (!ipConsumed || !emailConsumed) {
            long retryAfterSeconds = 0;
            if (!ipConsumed) {
                retryAfterSeconds = Math.max(retryAfterSeconds, ipLimiter.escalateLockout(ipKey, now));
            }
            if (!emailConsumed) {
                retryAfterSeconds = Math.max(retryAfterSeconds, emailLimiter.escalateLockout(emailKey, now));
            }
            String triggeredBy = !ipConsumed && !emailConsumed ? "ip+email" : (ipConsumed ? "email" : "ip");
            logBlocked(triggeredBy, clientIp, email, retryAfterSeconds);
            writeRateLimitedResponse(response, retryAfterSeconds);
            return;
        }

        filterChain.doFilter(wrappedRequest, response);
    }

    private void logBlocked(String triggeredBy, String clientIp, String email, long retryAfterSeconds) {
        log.warn("rate_limit_blocked endpoint=\"{} {}\" clave={} ip={} email={} retryAfterSeconds={}",
                method, path, triggeredBy, clientIp, maskEmail(email), retryAfterSeconds);
    }

    // El email se enmascara: el log sirve para identificar el patron de abuso, no para
    // dejar direcciones de alumnos en texto plano en Cloud Logging.
    private static String maskEmail(String email) {
        if (email == null || EMAIL_FALLBACK_KEY.equals(email)) {
            return "-";
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    private void writeRateLimitedResponse(HttpServletResponse response, long retryAfterSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, tooManyRequestsMessage);
        problem.setProperty("retryAfterSeconds", retryAfterSeconds);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
