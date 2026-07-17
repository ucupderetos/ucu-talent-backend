CREATE TABLE degree (
    degree_id VARCHAR(12) PRIMARY KEY,
    area_id VARCHAR(12) NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_ucu BOOLEAN NOT NULL
);