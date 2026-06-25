package com.library.dao;

import com.library.exception.DaoException;
import com.library.model.BookRequest;
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
public class BookRequestDaoImpl implements BookRequestDao {
    private final ConnectionPool pool = ConnectionPool.getInstance();

    private static final String SELECT_BASE = """
            SELECT r.request_id,
                   r.reader_id,
                   r.copy_id,
                   r.book_id,
                   r.request_type,
                   r.status,
                   r.request_date,
                   r.return_date,
                   u.full_name AS reader_name,
                   b.title AS book_title
            FROM book_requests r
            JOIN users u ON r.reader_id = u.user_id
            JOIN books b ON r.book_id = b.book_id
            """;

    @Override
    public BookRequest create(BookRequest request) {
        String insertSql = """
                INSERT INTO book_requests (reader_id, copy_id, book_id, request_type, status)
                VALUES (?, ?, ?, ?, 'PENDING')
                """;

        try (Connection conn = pool.getConnection()) {
            try {
                conn.setAutoCommit(false);

                int copyId = findAvailableCopyForUpdate(conn, request.getBookId());

                if (copyId == -1) {
                    throw new DaoException("No available copy for this book");
                }

                markCopy(conn, copyId, "RESERVED");

                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, request.getReaderId());
                    ps.setInt(2, copyId);
                    ps.setInt(3, request.getBookId());
                    ps.setString(4, request.getRequestType());
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            request.setRequestId(keys.getInt(1));
                        }
                    }
                }

                request.setCopyId(copyId);
                request.setStatus("PENDING");

                conn.commit();
                return request;

            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new DaoException("Failed to create book request", e);
        }
    }

    @Override
    public Optional<BookRequest> findById(int requestId) {
        String sql = SELECT_BASE + " WHERE r.request_id = ?";

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, requestId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new DaoException("Failed to find request by id: " + requestId, e);
        }
    }

    @Override
    public List<BookRequest> findByReader(int readerId) {
        String sql = SELECT_BASE + " WHERE r.reader_id = ? ORDER BY r.request_date DESC";
        return queryList(sql, readerId);
    }

    @Override
    public List<BookRequest> findAll() {
        String sql = SELECT_BASE + " ORDER BY r.request_date DESC";
        return queryList(sql, null);
    }

    @Override
    public int countActiveByReader(int readerId) {
        String sql = """
                SELECT COUNT(*)
                FROM book_requests
                WHERE reader_id = ?
                  AND status IN ('PENDING', 'ISSUED', 'PENDING_RETURN')
                """;

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, readerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (SQLException e) {
            throw new DaoException("Failed to count active requests for reader: " + readerId, e);
        }
    }

    @Override
    public void update(BookRequest request) {
        String sql = """
                UPDATE book_requests
                SET status = ?, copy_id = ?, return_date = ?
                WHERE request_id = ?
                """;

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, request.getStatus());

            if (request.getCopyId() == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, request.getCopyId());
            }

            if (request.getReturnDate() == null) {
                ps.setNull(3, java.sql.Types.DATE);
            } else {
                ps.setDate(3, java.sql.Date.valueOf(request.getReturnDate()));
            }

            ps.setInt(4, request.getRequestId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DaoException("Failed to update request id: " + request.getRequestId(), e);
        }
    }

    private int findAvailableCopyForUpdate(Connection conn, int bookId) throws SQLException {
        String sql = """
                SELECT copy_id
                FROM book_copies
                WHERE book_id = ?
                  AND status = 'AVAILABLE'
                ORDER BY copy_id
                LIMIT 1
                FOR UPDATE
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("copy_id") : -1;
            }
        }
    }

    private void markCopy(Connection conn, int copyId, String status) throws SQLException {
        String sql = "UPDATE book_copies SET status = ? WHERE copy_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, copyId);
            ps.executeUpdate();
        }
    }

    private List<BookRequest> queryList(String sql, Integer readerId) {
        List<BookRequest> list = new ArrayList<>();

        try (Connection conn = pool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (readerId != null) {
                ps.setInt(1, readerId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

            return list;

        } catch (SQLException e) {
            throw new DaoException("Failed to query requests", e);
        }
    }

    private BookRequest mapRow(ResultSet rs) throws SQLException {
        BookRequest request = new BookRequest();

        request.setRequestId(rs.getInt("request_id"));
        request.setReaderId(rs.getInt("reader_id"));

        int copyId = rs.getInt("copy_id");
        request.setCopyId(rs.wasNull() ? null : copyId);

        request.setBookId(rs.getInt("book_id"));
        request.setRequestType(rs.getString("request_type"));
        request.setStatus(rs.getString("status"));

        java.sql.Timestamp requestDate = rs.getTimestamp("request_date");
        if (requestDate != null) {
            request.setRequestDate(requestDate.toLocalDateTime());
        }

        java.sql.Date returnDate = rs.getDate("return_date");
        if (returnDate != null) {
            request.setReturnDate(returnDate.toLocalDate());
        }

        request.setReaderName(rs.getString("reader_name"));
        request.setBookTitle(rs.getString("book_title"));

        return request;
    }
}