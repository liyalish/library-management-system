package com.library.dao;

import com.library.exception.DaoException;
import com.library.model.Book;
import com.library.util.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link BookDao}. Uses {@link PreparedStatement} for every query
 * (including the search filter) to prevent SQL injection. Joins the authors table so that
 * the author name is available for display.
 */
public class BookDaoImpl implements BookDao {

    private final ConnectionPool pool = ConnectionPool.getInstance();

    private static final String SELECT_BASE =
            "SELECT b.book_id, b.title, b.author_id, b.description, b.publish_year, "
                    + "a.full_name AS author_name "
                    + "FROM books b JOIN authors a ON b.author_id = a.author_id ";

    @Override
    public Book create(Book book) {
        String sql = "INSERT INTO books (title, author_id, description, publish_year) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.getTitle());
            ps.setInt(2, book.getAuthorId());
            ps.setString(3, book.getDescription());
            setNullableInt(ps, 4, book.getPublishYear());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    book.setBookId(keys.getInt(1));
                }
            }
            return book;
        } catch (SQLException e) {
            throw new DaoException("Failed to create book: " + book.getTitle(), e);
        }
    }

    @Override
    public Optional<Book> findById(int bookId) {
        String sql = SELECT_BASE + "WHERE b.book_id = ?";
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
                + "ORDER BY b.title LIMIT ? OFFSET ?";
        List<Book> books = new ArrayList<>();
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (hasSearch) {
                ps.setString(idx++, "%" + search.toLowerCase() + "%");
            }
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);
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
        String sql = "UPDATE books SET title = ?, author_id = ?, description = ?, "
                + "publish_year = ? WHERE book_id = ?";
        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setInt(2, book.getAuthorId());
            ps.setString(3, book.getDescription());
            setNullableInt(ps, 4, book.getPublishYear());
            ps.setInt(5, book.getBookId());
            ps.executeUpdate();
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

    /**
     * Sets an integer parameter that may be null.
     *
     * @param ps    the prepared statement
     * @param index the parameter index
     * @param value the value, possibly null
     * @throws SQLException if the parameter cannot be set
     */
    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    /**
     * Maps the current row of a ResultSet to a {@link Book} object.
     *
     * @param rs the result set positioned on a row
     * @return the mapped book
     * @throws SQLException if a column cannot be read
     */
    private Book mapRow(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setBookId(rs.getInt("book_id"));
        book.setTitle(rs.getString("title"));
        book.setAuthorId(rs.getInt("author_id"));
        book.setAuthorName(rs.getString("author_name"));
        book.setDescription(rs.getString("description"));
        int year = rs.getInt("publish_year");
        book.setPublishYear(rs.wasNull() ? null : year);
        return book;
    }
}