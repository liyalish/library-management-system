-- Library Management System — seed data
-- Run after schema.sql.
-- password_hash below is a BCrypt hash of the word "password" (jBCrypt-compatible $2a$).

-- Users (password for all demo accounts = "password")
INSERT INTO users (username, password_hash, full_name, email, role)
VALUES ('admin', '$2a$10$C8I2fB50JCQppReWLOxpDOniP31RwfLNlmWKvCFntWQrz8YytiLL6', 'System Admin', 'admin@library.local',
        'ADMIN'),
       ('librarian', '$2a$10$C8I2fB50JCQppReWLOxpDOniP31RwfLNlmWKvCFntWQrz8YytiLL6', 'Main Librarian',
        'librarian@library.local', 'LIBRARIAN'),
       ('reader', '$2a$10$C8I2fB50JCQppReWLOxpDOniP31RwfLNlmWKvCFntWQrz8YytiLL6', 'Test Reader', 'reader@library.local',
        'READER');

-- Authors
INSERT INTO authors (full_name, country)
VALUES ('George Orwell', 'United Kingdom'),
       ('Jane Austen', 'United Kingdom'),
       ('Fyodor Dostoevsky', 'Russia'),
       ('Leo Tolstoy', 'Russia'),
       ('Mark Twain', 'United States'),
       ('Ernest Hemingway', 'United States'),
       ('Gabriel Garcia Marquez', 'Colombia'),
       ('Franz Kafka', 'Austria-Hungary'),
       ('Agatha Christie', 'United Kingdom'),
       ('J.R.R. Tolkien', 'United Kingdom');

-- Genres
INSERT INTO genres (name)
VALUES ('Fiction'),
       ('Classic'),
       ('Dystopia'),
       ('Romance'),
       ('Philosophy'),
       ('Mystery'),
       ('Fantasy'),
       ('Adventure'),
       ('Drama'),
       ('Historical');

-- Books
INSERT INTO books (title, author_id, description, publish_year)
VALUES ('1984', 1, 'A dystopian novel about totalitarian surveillance.', 1949),
       ('Animal Farm', 1, 'A satirical allegory of revolution and power.', 1945),
       ('Pride and Prejudice', 2, 'A romantic novel of manners.', 1813),
       ('Emma', 2, 'A comedy of manners about misguided matchmaking.', 1815),
       ('Crime and Punishment', 3, 'A psychological novel about morality and guilt.', 1866),
       ('The Brothers Karamazov', 3, 'A philosophical novel on faith, doubt, and reason.', 1880),
       ('War and Peace', 4, 'An epic of Russian society during the Napoleonic era.', 1869),
       ('Anna Karenina', 4, 'A tragic story of love and society.', 1877),
       ('The Adventures of Tom Sawyer', 5, 'A boy''s adventures along the Mississippi.', 1876),
       ('The Old Man and the Sea', 6, 'A fisherman''s epic struggle with a giant marlin.', 1952),
       ('One Hundred Years of Solitude', 7, 'The multi-generational story of the Buendia family.', 1967),
       ('The Trial', 8, 'A man is prosecuted by an inaccessible authority.', 1925),
       ('Murder on the Orient Express', 9, 'Detective Poirot solves a murder on a train.', 1934),
       ('The Hobbit', 10, 'A hobbit''s unexpected journey to a lonely mountain.', 1937),
       ('The Lord of the Rings', 10, 'An epic quest to destroy a powerful ring.', 1954);

-- Book genres
INSERT INTO book_genres (book_id, genre_id)
VALUES (1, 1),
       (1, 3),
       (2, 1),
       (2, 2),
       (3, 2),
       (3, 4),
       (4, 2),
       (4, 4),
       (5, 2),
       (5, 5),
       (6, 2),
       (6, 5),
       (7, 2),
       (7, 10),
       (8, 2),
       (8, 9),
       (9, 8),
       (9, 1),
       (10, 1),
       (10, 9),
       (11, 1),
       (11, 2),
       (12, 1),
       (12, 5),
       (13, 6),
       (13, 1),
       (14, 7),
       (14, 8),
       (15, 7),
       (15, 8);

-- Book copies (each physical copy has a unique inventory number and a status)
INSERT INTO book_copies (book_id, inventory_number, status)
VALUES (1, 'INV-1984-001', 'AVAILABLE'),
       (1, 'INV-1984-002', 'AVAILABLE'),
       (2, 'INV-AF-001', 'AVAILABLE'),
       (3, 'INV-PP-001', 'AVAILABLE'),
       (3, 'INV-PP-002', 'AVAILABLE'),
       (4, 'INV-EM-001', 'AVAILABLE'),
       (5, 'INV-CP-001', 'AVAILABLE'),
       (5, 'INV-CP-002', 'AVAILABLE'),
       (6, 'INV-BK-001', 'AVAILABLE'),
       (7, 'INV-WP-001', 'AVAILABLE'),
       (7, 'INV-WP-002', 'AVAILABLE'),
       (8, 'INV-AK-001', 'AVAILABLE'),
       (9, 'INV-TS-001', 'AVAILABLE'),
       (10, 'INV-OM-001', 'AVAILABLE'),
       (11, 'INV-HY-001', 'AVAILABLE'),
       (11, 'INV-HY-002', 'AVAILABLE'),
       (12, 'INV-TR-001', 'AVAILABLE'),
       (13, 'INV-OE-001', 'AVAILABLE'),
       (14, 'INV-HB-001', 'AVAILABLE'),
       (14, 'INV-HB-002', 'AVAILABLE'),
       (15, 'INV-LR-001', 'AVAILABLE');