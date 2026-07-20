package ucu.retojulio2026.talent.common;

// Cuenta con el rol correcto pero sin aprobar (User.status != APROBADO).
// Aplica tanto a la EMPRESA (no puede publicar vacantes) como al ALUMNO (no puede
// postularse). Antes se llamaba CompanyNotApprovedException, cuando la aprobacion era
// exclusiva de la empresa. Ver docs/ADR/0005-separacion-user-perfiles.md
public class AccountNotApprovedException extends RuntimeException {

    public AccountNotApprovedException() {
        super("La cuenta todavia no fue aprobada por un administrador");
    }
}
