package ucu.retojulio2026.talent.config;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

// Sin esto, una excepcion en un metodo @Async (ej. MailServiceImpl si el SMTP
// no autentica) se pierde en el handler default de Spring: el caller ya recibio
// una respuesta exitosa y el stacktrace queda genérico, sin decir a quien iba
// dirigida la tarea que fallo.
public class LoggingAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggingAsyncUncaughtExceptionHandler.class);

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... params) {
        log.error("Fallo en tarea async {}.{}({}) - el caller ya habia recibido una respuesta exitosa",
                method.getDeclaringClass().getSimpleName(), method.getName(), Arrays.toString(params), throwable);
    }
}
