package com.library.dao;

import com.library.exception.DaoException;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Genre;
import com.library.util.ConnectionPool;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class BookDaoImpl implements BookDao {
    private final ConnectionPool pool = ConnectionPool.getInstance();

    private static final String SELECT_BASE = """
            SELECT b.book_id,
                   b.title,
                   b.description,
                   b.publish_year,
                   string_agg(DISTINCT a.full_name, ', ') AS author_name,
                   string_agg(DISTINCT ba.author_id::text, ',') AS author_ids,
                   string_agg(DISTINCT g.name, ', ') AS genre_names,
                   COUNT(DISTINCT CASE WHEN bc.status = 'AVAILABLE' THEN bc.copy_id END) AS available_copies
            FROM books b
            LEFT JOIN book_authors ba ON ba.book_id = b.book_id
            LEFT JOIN authors a ON a.author_id = ba.author_id
            LEFT JOIN book_genres bg ON bg.book_id = b.book_id
            LEFT JOIN genres g ON g.genre_id = bg.genre_id
            LEFT JOIN book_copies bc ON bc.book_id = b.book_id
            """;

    private static final String GROUP_BY_BOOK = """
            GROUP BY b.book_id, b.title, b.description, b.publish_year
            """;

    @Override
    public Book create(Book book) {
        String insertBookSql = """
                INSERT INTO books (title, description, publish_year)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = pool.getConnection()) {
            try {
                conn.setAutoCommit(false);

                try (PreparedStatement ps = conn.prepareStatement(insertBookSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, book.getTitle());
                    ps.setString(2, book.getDescription());
                    setNullableInt(ps, 3, book.getPublishYear());
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            book.setBookId(keys.getInt(1));
                        }
                    }
                }

                insertBookAuthors(conn, book.getBookId(), book.getAuthorIds());

                conn.commit();
                return book;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new DaoException("Failed to create book: " + book.getTitle(), e);
        }
    }

    @Override
    public Optional<Book> findById(int bookId) {
        String sql = SELECT_BASE + """
                WHERE b.book_id = ?
                """ + GROUP_BY_BOOK;

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new DaoException("Failed to find book by id: " + bookId, e);
        }
    }

    @Override
    public List<Book> findAll(String search, Integer authorId, Integer genreId, int limit, int offset) {
        StringBuilder sql = new StringBuilder(SELECT_BASE);
        List<Object> params = new ArrayList<>();

        if (authorId != null && authorId > 0) {
            sql.append("""
                    JOIN book_authors ba_filter
                      ON ba_filter.book_id = b.book_id
                     AND ba_filter.author_id = ?
                    """);
            params.add(authorId);
        }

        if (genreId != null && genreId > 0) {
            sql.append("""
                    JOIN book_genres bg_filter
                      ON bg_filter.book_id = b.book_id
                     AND bg_filter.genre_id = ?
                    """);
            params.add(genreId);
        }

        sql.append(" WHERE 1 = 1 ");

        if (search != null && !search.isBlank()) {
            sql.append(" AND LOWER(b.title) LIKE ? ");
            params.add("%" + search.toLowerCase() + "%");
        }

        sql.append(GROUP_BY_BOOK);
        sql.append(" ORDER BY b.title LIMIT ? OFFSET ? ");

        params.add(limit);
        params.add(offset);

        List<Book> books = new ArrayList<>();

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRow(rs));
                }
            }

            return books;

        } catch (SQLException e) {
            throw new DaoException("Failed to list books", e);
        }
    }

    @Override
    public int count(String search, Integer authorId, Integer genreId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT b.book_id) FROM books b ");
        List<Object> params = new ArrayList<>();

        if (authorId != null && authorId > 0) {
            sql.append("""
                    JOIN book_authors ba_filter
                      ON ba_filter.book_id = b.book_id
                     AND ba_filter.author_id = ?
                    """);
            params.add(authorId);
        }

        if (genreId != null && genreId > 0) {
            sql.append("""
                    JOIN book_genres bg_filter
                      ON bg_filter.book_id = b.book_id
                     AND bg_filter.genre_id = ?
                    """);
            params.add(genreId);
        }

        sql.append(" WHERE 1 = 1 ");

        if (search != null && !search.isBlank()) {
            sql.append(" AND LOWER(b.title) LIKE ? ");
            params.add("%" + search.toLowerCase() + "%");
        }

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (SQLException e) {
            throw new DaoException("Failed to count books", e);
        }
    }

    @Override
    public List<Author> findAllAuthors() {
        String sql = "SELECT author_id, full_name FROM authors ORDER BY full_name";
        List<Author> authors = new ArrayList<>();

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                authors.add(new Author(
                        rs.getInt("author_id"),
                        rs.getString("full_name")
                ));
            }

            return authors;

        } catch (SQLException e) {
            throw new DaoException("Failed to list authors", e);
        }
    }

    @Override
    public List<Genre> findAllGenres() {
        String sql = "SELECT genre_id, name FROM genres ORDER BY name";
        List<Genre> genres = new ArrayList<>();

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                genres.add(new Genre(
                        rs.getInt("genre_id"),
                        rs.getString("name")
                ));
            }

            return genres;

        } catch (SQLException e) {
            throw new DaoException("Failed to list genres", e);
        }
    }

    @Override
    public void update(Book book) {
        String updateBookSql = """
                UPDATE books
                SET title = ?, description = ?, publish_year = ?
                WHERE book_id = ?
                """;

        try (Connection conn = pool.getConnection()) {
            try {
                conn.setAutoCommit(false);

                try (PreparedStatement ps = conn.prepareStatement(updateBookSql)) {
                    ps.setString(1, book.getTitle());
                    ps.setString(2, book.getDescription());
                    setNullableInt(ps, 3, book.getPublishYear());
                    ps.setInt(4, book.getBookId());
                    ps.executeUpdate();
                }

                deleteBookAuthors(conn, book.getBookId());
                insertBookAuthors(conn, book.getBookId(), book.getAuthorIds());

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new DaoException("Failed to update book id: " + book.getBookId(), e);
        }
    }

    @Override
    public void delete(int bookId) {
        String sql = "DELETE FROM books WHERE book_id = ?";

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DaoException("Failed to delete book id: " + bookId, e);
        }
    }

    private void insertBookAuthors(Connection conn, int bookId, List<Integer> authorIds) throws SQLException {
        if (authorIds == null || authorIds.isEmpty()) {
            throw new SQLException("Book must have at least one author");
        }

        String sql = """
                INSERT INTO book_authors (book_id, author_id)
                VALUES (?, ?)
                ON CONFLICT (book_id, author_id) DO NOTHING
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Integer authorId : authorIds) {
                ps.setInt(1, bookId);
                ps.setInt(2, authorId);
                ps.addBatch();
            }

            ps.executeBatch();
        }
    }

    private void deleteBookAuthors(Connection conn, int bookId) throws SQLException {
        String sql = "DELETE FROM book_authors WHERE book_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.executeUpdate();
        }
    }

    private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);

            if (value instanceof Integer) {
                ps.setInt(i + 1, (Integer) value);
            } else {
                ps.setString(i + 1, value.toString());
            }
        }
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book book = new Book();

        book.setBookId(rs.getInt("book_id"));
        book.setTitle(rs.getString("title"));
        book.setAuthorName(emptyIfNull(rs.getString("author_name")));
        book.setAuthorIdsText(emptyIfNull(rs.getString("author_ids")));
        book.setGenreNames(emptyIfNull(rs.getString("genre_names")));
        book.setDescription(rs.getString("description"));

        int year = rs.getInt("publish_year");
        book.setPublishYear(rs.wasNull() ? null : year);

        book.setAvailableCopies(rs.getInt("available_copies"));

        return book;
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }
}