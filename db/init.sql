CREATE TABLE IF NOT EXISTS items (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO items (name, description) VALUES
    ('Reverse proxy',  'Nginx terminating TLS at the edge'),
    ('Backend API',    'Spring Boot service exposing /api/items'),
    ('Database',       'MySQL 8 reachable only from the internal network'),
    ('SFTP gateway',   'Chrooted upload endpoint sharing the static volume');
