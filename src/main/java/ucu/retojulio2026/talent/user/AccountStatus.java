package ucu.retojulio2026.talent.user;

// Estado de admision de una cuenta. Aplica a los tres roles: el ALUMNO se aprueba
// (a futuro, verificando la cedula contra UniversityRegistry), la EMPRESA la aprueba
// un Admin, y el ADMIN nace APROBADO porque se crea por seed.
//
// Tres estados y no un booleano: false mezclaria "todavia no lo revisamos" con
// "lo revisamos y no pasa", y el front necesita mensajes distintos para cada caso.
//
// NO se expone como claim del JWT: cambia dentro de la vida del token (4h) y no hay
// revocacion, asi que quedaria desactualizado. Se lee fresco de la BD en el service.
// Ver docs/ADR/0005-separacion-user-perfiles.md
public enum AccountStatus {
    PENDIENTE,
    APROBADO,
    RECHAZADO
}
