-- Perfil del ADMIN. Mismo patron de PK compartida que student_profile / company:
-- admin_id es a la vez PK y FK a "user"(user_id), nunca un id generado aparte.
--
-- Existe por la auditoria de moderacion: cuando se agregue vacancy.reviewed_by (y el
-- equivalente para cuentas), la UI va a querer mostrar "Rechazada por <nombre>", y ese
-- nombre sale de aca. Ver docs/ADR/0005-separacion-user-perfiles.md
CREATE TABLE admin (
    admin_id VARCHAR(12) NOT NULL,
    name     VARCHAR(50) NOT NULL,
    surname  VARCHAR(50) NOT NULL,

    CONSTRAINT pk_admin PRIMARY KEY (admin_id),

    CONSTRAINT fk_admin_user
        FOREIGN KEY (admin_id) REFERENCES "user"(user_id) ON DELETE CASCADE
);
