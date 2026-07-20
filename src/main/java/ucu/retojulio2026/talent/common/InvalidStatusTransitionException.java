package ucu.retojulio2026.talent.common;

// El estado de un recurso avanza en un solo sentido y el cliente pidio retroceder
// (ej: VacancyApplication.status: PENDIENTE -> VISTO -> FINALIZADO, nunca al reves).
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
