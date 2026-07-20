package ucu.retojulio2026.talent.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un metodo de un bean de Spring (service, por ahora) como auditable.
 * AuditAspect intercepta cada invocacion y, si quien la ejecuta es un
 * Por ejemplo usuario con rol ADMIN, dispara (async) el guardado de un registro en
 * audit_log con el resultado de la operacion.
 *
 * entityId acepta una expresion SpEL evaluada contra los parametros del
 * metodo, por nombre (ej: "#id"), y contra el resultado ya ejecutado con
 * la variable #result (ej: "#result.vacancyId").
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    String module();

    String action();

    String entityId() default "";
}
