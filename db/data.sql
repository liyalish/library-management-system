-- Library Management System — seed data
-- Run after schema.sql.
-- NOTE: password_hash values below are BCrypt hashes of the word "password".
-- Replace them by registering real users, or generate your own with BCrypt.

-- Users (password for all demo accounts = "password")
INSERT INTO users (username, password_hash, full_name, email, role)
VALUES ('admin', '$2b$10$C8I2fB50JCQppReWLOxpDOniP31RwfLNlmWKvCFntWQrz8YytiLL6', 'System Admin', 'admin@library.local',
        'ADMIN'),
       ('librarian', '$2b$10$C8I2fB50JCQppReWLOxpDOniP31RwfLNlmWKvCFntWQrz8YytiLL6', 'Main Librarian',
        'librarian@library.local', 'LIBRARIAN'),
       ('reader', '$2b$10$C8I2fB50JCQppReWLOxpDOniP31RwfLNlmWKvCFntWQrz8YytiLL6', 'Test Reader', 'reader@library.local',
        'READER');

-- Authors
INSERT INTO authors (full_name, country)
VALUES ('George Orwell', 'United Kingdom'),
       ('Jane Austen', 'United Kingdom'),
       ('Fyodor Dostoevsky', 'Russia');

-- Genres
INSERT INTO genres (name)
VALUES ('Fiction'),
       ('Classic'),
       ('Dystopia'),
       ('Romance'),
       ('Philosophy');

-- Books
INSERT INTO books (title, author_id, description, publish_year)
VALUES ('1984', 1, 'A dystopian novel about totalitarian surveillance.', 1949),
       ('Pride and Prejudice', 2, 'A romantic novel of manners.', 1813),
       ('Crime and Punishment', 3, 'A psychological novel about morality and guilt.', 1866);

-- Book genres
INSERT INTO book_genres (book_id, genre_id)
VALUES (1, 1),
       (1, 3),
       (2, 2),
       (2, 4),
       (3, 2),
       (3, 5);

-- Copies
INSERT INTO book_copies (book_id, inventory_number, status)
VALUES (1, 'INV-1984-001', 'AVAILABLE'),
       (1, 'INV-1984-002', 'AVAILABLE'),
       (2, 'INV-PP-001', 'AVAILABLE'),
       (3, 'INV-CP-001', 'AVAILABLE'),
       (3, 'INV-CP-002', 'AVAILABLE');