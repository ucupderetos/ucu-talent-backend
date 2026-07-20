-- Tabla de auditoria. Sin FK a "user" a proposito: si el usuario actor se
-- borra despues, el log tiene que seguir mostrando quien hizo la accion en
-- su momento (email/rol quedan "congelados" al momento del registro).
CREATE TABLE audit_log (
    audit_id        VARCHAR(12)   NOT NULL,
    trace_id        VARCHAR(36),
    actor_user_id   VARCHAR(12)   NOT NULL,
    actor_email     VARCHAR(255)  NOT NULL,
    actor_role      VARCHAR(20)   NOT NULL,
    module          VARCHAR(50)   NOT NULL,
    action          VARCHAR(100)  NOT NULL,
    entity_id       VARCHAR(50),
    outcome         VARCHAR(20)   NOT NULL,
    message         TEXT          NOT NULL,
    detail          TEXT,
    created_at      TIMESTAMP     NOT NULL, --Igual que antes, se tiene que setear mediante timezone de Montevideo

    CONSTRAINT pk_audit_log PRIMARY KEY (audit_id),
    CONSTRAINT ck_audit_log_actor_role CHECK (actor_role IN ('ALUMNO', 'EMPRESA', 'ADMIN')),
    CONSTRAINT ck_audit_log_outcome CHECK (outcome IN ('SUCCESS', 'ERROR'))
);

CREATE INDEX idx_audit_log_actor_user_id ON audit_log (actor_user_id);
CREATE INDEX idx_audit_log_module_action ON audit_log (module, action);
CREATE INDEX idx_audit_log_created_at ON audit_log (created_at);
