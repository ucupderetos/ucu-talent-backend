package ucu.retojulio2026.talent.auth;

import jakarta.servlet.http.HttpServletRequest;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

// Helpers compartidos por los filtros de rate limit (login y alta de cuentas): identificar el
// cliente y sacar el email del body sin atarse al DTO especifico de cada endpoint.
final class RateLimitSupport {

    private RateLimitSupport() {
    }

    static String resolveClientIp(HttpServletRequest request, String forwardedForHeader) {
        String forwardedFor = request.getHeader(forwardedForHeader);
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] values = forwardedFor.split(",");
            if (values.length > 0 && !values[0].isBlank()) {
                return values[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    static String extractJsonField(ObjectMapper objectMapper, byte[] requestBody, String fieldName, String fallback) {
        if (requestBody == null || requestBody.length == 0) {
            return fallback;
        }
        try {
            JsonNode node = objectMapper.readTree(requestBody);
            String value = node.path(fieldName).asString(null);
            if (value == null || value.isBlank()) {
                return fallback;
            }
            return value.trim().toLowerCase();
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
