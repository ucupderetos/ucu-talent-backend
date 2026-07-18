CREATE TABLE area (
    area_id        VARCHAR(12)  NOT NULL,
    name           VARCHAR(255) NOT NULL,
    parent_area_id VARCHAR(12),

    CONSTRAINT pk_area PRIMARY KEY (area_id),
    CONSTRAINT fk_area_parent FOREIGN KEY (parent_area_id) REFERENCES area(area_id)
);