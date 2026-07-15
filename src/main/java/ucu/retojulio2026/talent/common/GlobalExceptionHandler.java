package ucu.retojulio2026.talent.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

//Manejo centralizado de excepciones para toda la API.


//Bean que le dice a Spring que la clase maneja excepciones.
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    //Excepcion que se lanza al no econtrar un recurso. Codigo HTTP 404 Not Found.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    //Login fallido (email inexistente o password incorrecta). Codigo HTTP 401 Unauthorized.
    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    //Se lanza cuando falla la validacion de un @Valid (ej: email invalido, pass corta).
    //Junta TODOS los campos que fallaron en un json y devuelve 400 Bad Request.
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

    //Igual que el anterior pero para validaciones sobre parametros sueltos del metodo
    //(@RequestParam / @PathVariable con @Email, @NotBlank, etc), ej: GET /user?email=malo.
    //Spring usa OTRA excepcion para estos, por eso hace falta este handler aparte;
    //lo unificamos al mismo formato {campo: mensaje} para que la API responda igual.
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
}
