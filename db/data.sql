-- Library Management System — seed data
-- Users password for all demo accounts = "password"

INSERT INTO users (username, password_hash, full_name, email, role)
VALUES
    ('admin', '$2a$10$C8I2fB50JCQppReWLOxpDOniP31RwfLNlmWKvCFntWQrz8YytiLL6', 'System Admin', 'admin@library.local', 'ADMIN'),
    ('librarian', '$2a$10$C8I2fB50JCQppReWLOxpDOniP31RwfLNlmWKvCFntWQrz8YytiLL6', 'Main Librarian', 'librarian@library.local', 'LIBRARIAN'),
    ('reader', '$2a$10$C8I2fB50JCQppReWLOxpDOniP31RwfLNlmWKvCFntWQrz8YytiLL6', 'Test Reader', 'reader@library.local', 'READER');

INSERT INTO authors (full_name, country)
VALUES
    ('George Orwell', 'United Kingdom'),
    ('Jane Austen', 'United Kingdom'),
    ('Fyodor Dostoevsky', 'Russia'),
    ('Leo Tolstoy', 'Russia'),
    ('Mark Twain', 'United States'),
    ('Ernest Hemingway', 'United States'),
    ('Gabriel Garcia Marquez', 'Colombia'),
    ('Franz Kafka', 'Austria-Hungary'),
    ('Agatha Christie', 'United Kingdom'),
    ('J.R.R. Tolkien', 'United Kingdom');

INSERT INTO genres (name)
VALUES
    ('Fiction'),
    ('Classic'),
    ('Dystopia'),
    ('Romance'),
    ('Philosophy'),
    ('Mystery'),
    ('Fantasy'),
    ('Adventure'),
    ('Drama'),
    ('Historical');

INSERT INTO books (title, description, publish_year)
VALUES
    ('1984', 'A dystopian novel about totalitarian surveillance.', 1949),
    ('Animal Farm', 'A satirical allegory of revolution and power.', 1945),
    ('Pride and Prejudice', 'A romantic novel of manners.', 1813),
    ('Emma', 'A comedy of manners about misguided matchmaking.', 1815),
    ('Crime and Punishment', 'A psychological novel about morality and guilt.', 1866),
    ('The Brothers Karamazov', 'A philosophical novel on faith, doubt, and reason.', 1880),
    ('War and Peace', 'An epic of Russian society during the Napoleonic era.', 1869),
    ('Anna Karenina', 'A tragic story of love and society.', 1877),
    ('The Adventures of Tom Sawyer', 'A boy''s adventures along the Mississippi.', 1876),
    ('The Old Man and the Sea', 'A fisherman''s epic struggle with a giant marlin.', 1952),
    ('One Hundred Years of Solitude', 'The multi-generational story of the Buendia family.', 1967),
    ('The Trial', 'A man is prosecuted by an inaccessible authority.', 1925),
    ('Murder on the Orient Express', 'Detective Poirot solves a murder on a train.', 1934),
    ('The Hobbit', 'A hobbit''s unexpected journey to a lonely mountain.', 1937),
    ('The Lord of the Rings', 'An epic quest to destroy a powerful ring.', 1954);

INSERT INTO book_authors (book_id, author_id)
VALUES
    (1, 1),
    (2, 1),
    (3, 2),
    (4, 2),
    (5, 3),
    (6, 3),
    (7, 4),
    (8, 4),
    (9, 5),
    (10, 6),
    (11, 7),
    (12, 8),
    (13, 9),
    (14, 10),
    (15, 10);

INSERT INTO book_genres (book_id, genre_id)
VALUES
    (1, 1),
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

INSERT INTO book_copies (book_id, inventory_number, status)
VALUES
    (1, 'INV-1984-001', 'AVAILABLE'),
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