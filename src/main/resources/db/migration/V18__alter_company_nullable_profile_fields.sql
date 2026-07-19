-- Company ahora se crea "placeholder" en el mismo paso que el signup del User (rol EMPRESA),
-- antes de que la empresa complete sus datos reales con un PUT /company/{id} posterior.
-- Estas columnas ya no pueden ser NOT NULL en el momento de la creacion.
ALTER TABLE company
    ALTER COLUMN industry DROP NOT NULL,
    ALTER COLUMN description DROP NOT NULL,
    ALTER COLUMN web_url DROP NOT NULL,
    ALTER COLUMN linkedin_url DROP NOT NULL,
    ALTER COLUMN location DROP NOT NULL;
