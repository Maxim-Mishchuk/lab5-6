CREATE TABLE posts_photos
(
    id   BIGINT NOT NULL,
    name VARCHAR(255),
    data OID,
    CONSTRAINT pk_posts_photos PRIMARY KEY (id)
);

ALTER TABLE posts_photos
    ADD CONSTRAINT FK_POSTS_PHOTOS_ON_ID FOREIGN KEY (id) REFERENCES posts (id);