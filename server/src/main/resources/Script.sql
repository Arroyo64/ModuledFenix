DROP DATABASE IF EXISTS fenixdb;

CREATE DATABASE IF NOT EXISTS fenixdb;

USE fenixdb;

CREATE TABLE client
(
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(25) UNIQUE,
    email VARCHAR(50),
    password_hashed VARCHAR(128),
    bio VARCHAR(250)
);

CREATE TABLE tag
(
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(30) UNIQUE,
    description VARCHAR(250)
);

CREATE TABLE game
(
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(50) UNIQUE,
    dev_id INT,
    description TEXT,
    tamano_mb DECIMAL(10,2),
    downloads INT,
    price DECIMAL(6,2),
    CONSTRAINT fk_game_client
        FOREIGN KEY (dev_id)
            REFERENCES client(id)
            ON DELETE SET NULL
            ON UPDATE CASCADE
);

CREATE TABLE game_tag
(
    game_id INT NOT NULL,
    tag_id INT NOT NULL,
    PRIMARY KEY(game_id, tag_id),
    CONSTRAINT fk_game_tag_game
        FOREIGN KEY (game_id)
            REFERENCES game(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,
    CONSTRAINT fk_game_tag_tag
        FOREIGN KEY (tag_id)
            REFERENCES tag(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);

CREATE TABLE purchase
(
    id INT PRIMARY KEY AUTO_INCREMENT,
    client_id INT NOT NULL,
    game_id INT NOT NULL,
    CONSTRAINT fk_purchase_client
        FOREIGN KEY (client_id)
            REFERENCES client(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,
    CONSTRAINT fk_purchase_game
        FOREIGN KEY (game_id)
            REFERENCES game(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
);

CREATE TABLE teaser
(
    id INT AUTO_INCREMENT PRIMARY KEY,
    game_id INT NOT NULL,
    object_key VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    CONSTRAINT fk_teaser_game
        FOREIGN KEY (game_id)
            REFERENCES game(id)
            ON DELETE CASCADE
);

CREATE TABLE auth_token
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(120) NOT NULL UNIQUE,
    client_id INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    CONSTRAINT fk_auth_token_client
        FOREIGN KEY (client_id)
            REFERENCES client(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

CREATE INDEX idx_auth_token_client_id
    ON auth_token(client_id);

ALTER TABLE client
    ADD COLUMN profile_image_key VARCHAR(255);

ALTER TABLE game
    ADD COLUMN game_file_key VARCHAR(255),
	ADD COLUMN logo_image_key VARCHAR(255),
	ADD COLUMN vertical_image_key VARCHAR(255),
	ADD COLUMN horizontal_image_one_key VARCHAR(255),
	ADD COLUMN horizontal_image_two_key VARCHAR(255);

INSERT INTO tag (name, description) VALUES
                                        ('Romance', 'Romantic stories and relationships'),
                                        ('Mystery', 'Stories focused on secrets and investigation'),
                                        ('Sci-fi', 'Science fiction and futuristic stories'),
                                        ('Fantasy', 'Magic, mythical worlds and fantasy elements'),
                                        ('Horror', 'Scary, dark or disturbing stories'),
                                        ('Drama', 'Emotional and dramatic stories'),
                                        ('Comedy', 'Light-hearted and funny stories'),
                                        ('Adventure', 'Exploration and journey-focused stories'),
                                        ('Slice of life', 'Everyday life stories'),
                                        ('Thriller', 'Suspense and tension-focused stories');







