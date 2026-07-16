package ucu.retojulio2026.talent.common;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import tools.jackson.databind.exc.InvalidFormatException;

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

    //Cuerpo JSON ilegible. Cubre el caso de un enum con valor invalido (ej: location "BUENOS_AIRES"),
    //que Jackson rechaza al deserializar, ANTES de que corran las validaciones de @Valid.
    //Lo devolvemos con el mismo formato {campo: mensaje} y listamos los valores validos del enum.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex) {
        // Buscamos un InvalidFormatException en TODA la cadena de causas: cuando el campo
        // problematico pertenece a un record, Jackson envuelve el error en un ValueInstantiationException.
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
        //JSON malformado u otro cuerpo ilegible que no sea un enum invalido.
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "El cuerpo de la peticion es invalido o esta mal formado");
    }
}
