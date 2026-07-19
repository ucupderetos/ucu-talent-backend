package ucu.retojulio2026.talent.common;

public class CompanyNotApprovedException extends RuntimeException {

    public CompanyNotApprovedException() {
        super("La empresa todavia no fue aprobada por un administrador");
    }
}
