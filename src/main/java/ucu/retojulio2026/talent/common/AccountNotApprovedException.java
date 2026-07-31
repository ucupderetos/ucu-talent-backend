package ucu.retojulio2026.talent.common;

public class AccountNotApprovedException extends RuntimeException {

    public AccountNotApprovedException() {
        super("La cuenta todavia no fue aprobada por un administrador");
    }

    public AccountNotApprovedException(String message) {
        super(message);
    }
}
