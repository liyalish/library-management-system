-- Library Management System — database schema (PostgreSQL)
-- Normalized to 3NF. Run this first, then data.sql.

DROP TABLE IF EXISTS book_requests CASCADE;
DROP TABLE IF EXISTS book_copies CASCADE;
DROP TABLE IF EXISTS book_genres CASCADE;
DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS authors CASCADE;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 1. Users (readers, librarians, administrators)
CREATE TABLE users
(
    user_id       SERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL, -- BCrypt hash, never plain text
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    role          VARCHAR(20)  NOT NULL DEFAULT 'READER'
        CHECK (role IN ('READER', 'LIBRARIAN', 'ADMIN')),
    is_blocked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Authors
CREATE TABLE authors
(
    author_id SERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    country   VARCHAR(60)
);

-- 3. Genres
CREATE TABLE genres
(
    genre_id SERIAL PRIMARY KEY,
    name     VARCHAR(60) NOT NULL UNIQUE
);

-- 4. Books (one author per book for simplicity; many genres via link table)
CREATE TABLE books
(
    book_id      SERIAL PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    author_id    INT          NOT NULL REFERENCES authors (author_id),
    description  TEXT,
    publish_year INT,
    FOREIGN KEY (author_id) REFERENCES authors (author_id)
);

-- 5. Book–Genre link (many-to-many)
CREATE TABLE book_genres
(
    book_id  INT NOT NULL REFERENCES books (book_id) ON DELETE CASCADE,
    genre_id INT NOT NULL REFERENCES genres (genre_id),
    PRIMARY KEY (book_id, genre_id)
);

-- 6. Book copies (each physical copy has a unique inventory number + status)
CREATE TABLE book_copies
(
    copy_id          SERIAL PRIMARY KEY,
    book_id          INT         NOT NULL REFERENCES books (book_id) ON DELETE CASCADE,
    inventory_number VARCHAR(40) NOT NULL UNIQUE,
    status           VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
        CHECK (status IN ('AVAILABLE', 'ISSUED', 'RESERVED'))
);

-- 7. Book requests (a reader requests a book; a librarian processes it)
CREATE TABLE book_requests
(
    request_id   SERIAL PRIMARY KEY,
    reader_id    INT         NOT NULL REFERENCES users (user_id),
    copy_id      INT REFERENCES book_copies (copy_id),
    book_id      INT         NOT NULL REFERENCES books (book_id),
    request_type VARCHAR(20) NOT NULL CHECK (request_type IN ('HOME', 'READING_ROOM')),
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'ISSUED', 'RETURNED', 'CANCELLED')),
    request_date TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    return_date  DATE
);

CREATE INDEX idx_books_title ON books (title);
CREATE INDEX idx_copies_book ON book_copies (book_id);
CREATE INDEX idx_requests_user ON book_requests (reader_id);