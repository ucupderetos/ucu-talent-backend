package ucu.retojulio2026.talent.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Pool de hilos usado para persistir los audit logs y mandar mails sin
 * bloquear el hilo del request (ver AuditService.saveAuditLog y MailServiceImpl,
 * ambos anotados @Async("taskExecutor")).
 * @EnableAsync se declara en TalentApplication, junto con @EnableScheduling
 * y @EnableAspectJAutoProxy. Implementar AsyncConfigurer acá (en vez de solo
 * exponer el Executor como bean) es lo que engancha el
 * LoggingAsyncUncaughtExceptionHandler en vez del default de Spring.
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("AuditThread-");
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new LoggingAsyncUncaughtExceptionHandler();
    }
}
