CREATE TABLE mail_template (
    mail_template_id VARCHAR(12)  NOT NULL,
    code              VARCHAR(30)  NOT NULL,
    subject           VARCHAR(200) NOT NULL,
    body              TEXT         NOT NULL,

    CONSTRAINT pk_mail_template PRIMARY KEY (mail_template_id),
    CONSTRAINT uq_mail_template_code UNIQUE (code),
    CONSTRAINT ck_mail_template_code CHECK (code IN ('NEW_APPLICATION', 'APPLICATION_VISTO', 'VACANCY_CLOSED', 'VACANCY_SELECTED'))
);

INSERT INTO mail_template (mail_template_id, code, subject, body) VALUES
('mt0newappl01', 'NEW_APPLICATION', 'Nueva postulación recibida',
'Hola,

Recibiste una nueva postulación.
Postulante: {{applicantName}}
Puesto: {{vacancyName}}

Saludos,
Equipo Talent'),

('mt0visto0002', 'APPLICATION_VISTO', 'Tu postulación fue vista',
'Hola {{studentName}},

La empresa revisó tu postulación al puesto {{vacancyName}}.
Te avisaremos si hay novedades.

Saludos,
Equipo Talent'),

('mt0closed003', 'VACANCY_CLOSED', 'La oferta laboral ha finalizado',
'Hola {{studentName}},

La oferta laboral {{vacancyName}} ha finalizado.
Gracias por tu interés.

Saludos,
Equipo Talent'),

('mt0select004', 'VACANCY_SELECTED', '¡Has sido seleccionado!',
'Hola {{studentName}},

¡Felicitaciones! Has sido seleccionado para el puesto {{vacancyName}} en {{companyName}}.
La empresa se pondrá en contacto con vos.

Saludos,
Equipo Talent');
