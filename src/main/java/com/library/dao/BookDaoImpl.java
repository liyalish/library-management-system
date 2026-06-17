package com.library.dao;

import com.library.exception.DaoException;
import com.library.model.Book;
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
                   COALESCE(string_agg(DISTINCT a.full_name, ', '), '') AS author_name,
                   COALESCE(string_agg(DISTINCT ba.author_id::text, ','), '') AS author_ids
            FROM books b
            LEFT JOIN book_authors ba ON b.book_id = ba.book_id
            LEFT JOIN authors a ON ba.author_id = a.author_id
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
                GROUP BY b.book_id, b.title, b.description, b.publish_year
                """;

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
    public List<Book> findAll(String search, int limit, int offset) {
        boolean hasSearch = search != null && !search.isBlank();

        String sql = SELECT_BASE
                + (hasSearch ? "WHERE LOWER(b.title) LIKE ? " : "")
                + """
                  GROUP BY b.book_id, b.title, b.description, b.publish_year
                  ORDER BY b.title
                  LIMIT ? OFFSET ?
                  """;

        List<Book> books = new ArrayList<>();

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int index = 1;

            if (hasSearch) {
                ps.setString(index++, "%" + search.toLowerCase() + "%");
            }

            ps.setInt(index++, limit);
            ps.setInt(index, offset);

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
    public int count(String search) {
        boolean hasSearch = search != null && !search.isBlank();

        String sql = "SELECT COUNT(*) FROM books b"
                + (hasSearch ? " WHERE LOWER(b.title) LIKE ?" : "");

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (hasSearch) {
                ps.setString(1, "%" + search.toLowerCase() + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (SQLException e) {
            throw new DaoException("Failed to count books", e);
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

        String sql = "INSERT INTO book_authors (book_id, author_id) VALUES (?, ?)";

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
        book.setAuthorName(rs.getString("author_name"));
        book.setAuthorIdsText(rs.getString("author_ids"));
        book.setDescription(rs.getString("description"));

        int year = rs.getInt("publish_year");
        book.setPublishYear(rs.wasNull() ? null : year);

        return book;
    }
}