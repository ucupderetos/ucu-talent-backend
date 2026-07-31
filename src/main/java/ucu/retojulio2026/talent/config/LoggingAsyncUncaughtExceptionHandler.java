package ucu.retojulio2026.talent.config;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

public class LoggingAsyncUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(LoggingAsyncUncaughtExceptionHandler.class);

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... params) {
        log.error("Fallo en tarea async {}.{}({}) - el caller ya habia recibido una respuesta exitosa",
                method.getDeclaringClass().getSimpleName(), method.getName(), Arrays.toString(params), throwable);
    }
}
