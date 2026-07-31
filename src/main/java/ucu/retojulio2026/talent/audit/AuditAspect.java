package ucu.retojulio2026.talent.audit;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;
import ucu.retojulio2026.talent.user.Role;
import ucu.retojulio2026.talent.user.User;

@Aspect
@Component
public class AuditAspect {

    private static final String TRACE_KEY = "traceId";
    private static final Set<String> CAMPOS_SENSIBLES = Set.of("password", "passwordHash");
    private static final int MAX_STACKTRACE_LENGTH = 2000;

    private final CurrentUserProvider currentUserProvider;
    private final AuditService auditService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExpressionParser spelParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public AuditAspect(CurrentUserProvider currentUserProvider, AuditService auditService) {
        this.currentUserProvider = currentUserProvider;
        this.auditService = auditService;
    }

    @Around("@annotation(auditable)")
    public Object around(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Optional<User> maybeActor = currentUserProvider.getCurrentUser();
        if (maybeActor.isEmpty() || maybeActor.get().getRole() != Role.ADMIN) {
            return joinPoint.proceed();
        }
        User actor = maybeActor.get();

        if (MDC.get(TRACE_KEY) == null) {
            MDC.put(TRACE_KEY, UUID.randomUUID().toString().substring(0, 8));
        }
        String traceId = MDC.get(TRACE_KEY);
        StandardEvaluationContext context = buildContext(joinPoint);

        try {
            Object result = joinPoint.proceed();
            context.setVariable("result", result);
            String entityId = resolveEntityId(auditable, context);
            String message = "Ejecucion exitosa: " + joinPoint.getSignature().getName();
            String detail = serializeArgs(joinPoint.getArgs());
            auditService.saveAuditLog(traceId, actor.getUserId(), actor.getEmail(), actor.getRole(),
                    auditable.module(), auditable.action(), entityId, "SUCCESS", message, detail);
            return result;
        } catch (Throwable ex) {
            String entityId = resolveEntityId(auditable, context);
            String message = "ERROR en " + joinPoint.getSignature().getName() + ": " + ex.getMessage();
            System.out.println("Fallo auditado en " + joinPoint.getSignature().getName()
                    + " (module=" + auditable.module() + " action=" + auditable.action()
                    + " entityId=" + entityId + ")");
            ex.printStackTrace();
            auditService.saveAuditLog(traceId, actor.getUserId(), actor.getEmail(), actor.getRole(),
                    auditable.module(), auditable.action(), entityId, "ERROR", message, null);
            throw ex;
        }
    }

    private StandardEvaluationContext buildContext(ProceedingJoinPoint joinPoint) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] paramNames = parameterNameDiscoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        return context;
    }

    private String resolveEntityId(Auditable auditable, StandardEvaluationContext context) {
        if (auditable.entityId().isBlank()) {
            return null;
        }
        try {
            Expression expression = spelParser.parseExpression(auditable.entityId());
            Object value = expression.getValue(context);
            return value == null ? null : value.toString();
        } catch (Exception ex) {

            return null;
        }
    }

    private String truncatedStackTrace(Throwable ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        return stackTrace.length() > MAX_STACKTRACE_LENGTH
                ? stackTrace.substring(0, MAX_STACKTRACE_LENGTH)
                : stackTrace;
    }

    private String serializeArgs(Object[] args) {
        try {
            Object[] sanitizados = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                sanitizados[i] = sanitizar(args[i]);
            }
            return objectMapper.writeValueAsString(sanitizados);
        } catch (Exception ex) {
            return null;
        }
    }

    private Object sanitizar(Object valor) {
        if (valor == null) {
            return null;
        }
        Object generico = objectMapper.convertValue(valor, Object.class);
        return limpiar(generico);
    }

    @SuppressWarnings("unchecked")
    private Object limpiar(Object nodo) {
        if (nodo instanceof Map<?, ?> mapa) {
            Map<String, Object> limpio = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entrada : mapa.entrySet()) {
                String clave = String.valueOf(entrada.getKey());
                if (!CAMPOS_SENSIBLES.contains(clave)) {
                    limpio.put(clave, limpiar(entrada.getValue()));
                }
            }
            return limpio;
        }
        if (nodo instanceof List<?> lista) {
            return lista.stream().map(this::limpiar).toList();
        }
        return nodo;
    }
}
