package ucu.retojulio2026.talent.config;

import java.util.Map;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

/**
 * MDC es ThreadLocal: sin esto, el traceId que seteamos en AuditAspect
 * (en el hilo del request) no llegaria al hilo del "taskExecutor" donde
 * corre AuditService.saveAuditLog, y los logs del guardado async
 * aparecerian sin traceId.
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
