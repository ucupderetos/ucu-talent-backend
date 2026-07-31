package ucu.retojulio2026.talent.common;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import tools.jackson.databind.exc.InvalidFormatException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ProblemDetail handleForbidden(ForbiddenOperationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicate(DuplicateResourceException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AccountNotApprovedException.class)
    public ProblemDetail handleAccountNotApproved(AccountNotApprovedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ProblemDetail handleInvalidStatusTransition(InvalidStatusTransitionException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String sqlState = null;
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof ConstraintViolationException cve) {
                sqlState = cve.getSQLState();
                break;
            }
        }
        if ("23505".equals(sqlState)) {
            return ProblemDetail.forStatusAndDetail(
                    HttpStatus.CONFLICT, "El recurso ya existe o viola una restriccion de unicidad (ej: email duplicado)");
        }
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Uno o mas campos son invalidos, faltan datos obligatorios, o hay una referencia invalida");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Uno o mas campos son invalidos");
        problem.setProperty("errores", errores);
        return problem;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleParamValidation(HandlerMethodValidationException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(result -> {
            String campo = result.getMethodParameter().getParameterName();
            result.getResolvableErrors().forEach(error ->
                    errores.put(campo, error.getDefaultMessage()));
        });
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Uno o mas campos son invalidos");
        problem.setProperty("errores", errores);
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex) {

        InvalidFormatException ife = null;
        for (Throwable t = ex.getCause(); t != null; t = t.getCause()) {
            if (t instanceof InvalidFormatException found) {
                ife = found;
                break;
            }
        }
        if (ife != null && ife.getTargetType() != null && ife.getTargetType().isEnum()) {
            String campo = ife.getPath().isEmpty()
                    ? "cuerpo"
                    : ife.getPath().get(ife.getPath().size() - 1).getPropertyName();
            String validos = Arrays.stream(ife.getTargetType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            Map<String, String> errores = new LinkedHashMap<>();
            errores.put(campo, "Valor invalido '" + ife.getValue() + "'. Valores validos: " + validos);
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST, "Uno o mas campos son invalidos");
            problem.setProperty("errores", errores);
            return problem;
        }

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "El cuerpo de la peticion es invalido o esta mal formado");
    }
}
