package com.library.service;

import com.library.dao.BookRequestDao;
import com.library.exception.ServiceException;
import com.library.model.BookRequest;
import com.library.util.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Service
public class RequestServiceImpl implements RequestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestServiceImpl.class);
    private static final int MAX_ACTIVE_REQUESTS = 5;

    private final BookRequestDao requestDao;
    private final ConnectionPool pool = ConnectionPool.getInstance();

    public RequestServiceImpl(BookRequestDao requestDao) {
        this.requestDao = requestDao;
    }

    @Override
    public BookRequest submitRequest(int readerId, int bookId, String requestType) {
        if (!"HOME".equals(requestType) && !"READING_ROOM".equals(requestType)) {
            throw new ServiceException("Invalid request type: " + requestType);
        }

        int activeCount = requestDao.countActiveByReader(readerId);
        if (activeCount >= MAX_ACTIVE_REQUESTS) {
            throw new ServiceException("You cannot have more than 5 active book requests");
        }

        BookRequest request = BookRequest.builder()
                .readerId(readerId)
                .bookId(bookId)
                .requestType(requestType)
                .status("PENDING")
                .build();

        BookRequest created = requestDao.create(request);
        LOGGER.info("Reader {} requested book {}", readerId, bookId);
        return created;
    }

    @Override
    public List<BookRequest> getReaderRequests(int readerId) {
        return requestDao.findByReader(readerId);
    }

    @Override
    public List<BookRequest> getAllRequests() {
        return requestDao.findAll();
    }

    @Override
    public void cancelRequest(int requestId, int readerId) {
        BookRequest request = requestDao.findById(requestId)
                .orElseThrow(() -> new ServiceException("Request not found: " + requestId));

        if (request.getReaderId() != readerId) {
            throw new ServiceException("You can only cancel your own requests");
        }

        if (!"PENDING".equals(request.getStatus())) {
            throw new ServiceException("Only pending requests can be cancelled");
        }

        request.setStatus("CANCELLED");
        requestDao.update(request);

        LOGGER.info("Request {} cancelled by reader {}", requestId, readerId);
    }

    @Override
    public void requestReturn(int requestId, int readerId) {
        BookRequest request = requestDao.findById(requestId)
                .orElseThrow(() -> new ServiceException("Request not found: " + requestId));

        if (request.getReaderId() != readerId) {
            throw new ServiceException("You can only return your own books");
        }

        if (!"ISSUED".equals(request.getStatus())) {
            throw new ServiceException("Only issued books can be returned");
        }

        request.setStatus("PENDING_RETURN");
        requestDao.update(request);

        LOGGER.info("Reader {} requested return for request {}", readerId, requestId);
    }

    @Override
    public void issueBook(int requestId, String returnDate) {
        try (Connection conn = pool.getConnection()) {
            try {
                conn.setAutoCommit(false);

                String status = loadStatus(conn, requestId);
                if (!"PENDING".equals(status)) {
                    throw new ServiceException("Only pending requests can be issued");
                }

                int bookId = loadBookId(conn, requestId);
                int copyId = findAvailableCopy(conn, bookId);

                if (copyId == -1) {
                    throw new ServiceException("No available copy for this book");
                }

                markCopy(conn, copyId, "ISSUED");
                updateRequestIssued(conn, requestId, copyId, returnDate);

                conn.commit();
                LOGGER.info("Issued request {} with copy {}", requestId, copyId);

            } catch (SQLException | ServiceException e) {
                conn.rollback();
                LOGGER.error("Issue failed for request {}, rolled back", requestId, e);
                throw new ServiceException("Failed to issue book: " + e.getMessage(), e);

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new ServiceException("Transaction error while issuing book", e);
        }
    }

    @Override
    public void returnBook(int requestId) {
        try (Connection conn = pool.getConnection()) {
            try {
                conn.setAutoCommit(false);

                String status = loadStatus(conn, requestId);
                if (!"PENDING_RETURN".equals(status)) {
                    throw new ServiceException("Only pending return requests can be approved");
                }

                Integer copyId = loadCopyId(conn, requestId);

                if (copyId != null) {
                    markCopy(conn, copyId, "AVAILABLE");
                }

                updateRequestStatus(conn, requestId, "RETURNED");

                conn.commit();
                LOGGER.info("Request {} returned", requestId);

            } catch (SQLException | ServiceException e) {
                conn.rollback();
                LOGGER.error("Return failed for request {}, rolled back", requestId, e);
                throw new ServiceException("Failed to return book: " + e.getMessage(), e);

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new ServiceException("Transaction error while returning book", e);
        }
    }

    private int loadBookId(Connection conn, int requestId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT book_id FROM book_requests WHERE request_id = ?")) {
            ps.setInt(1, requestId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }

                throw new SQLException("Request not found: " + requestId);
            }
        }
    }

    private String loadStatus(Connection conn, int requestId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT status FROM book_requests WHERE request_id = ?")) {
            ps.setInt(1, requestId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }

                throw new SQLException("Request not found: " + requestId);
            }
        }
    }

    private Integer loadCopyId(Connection conn, int requestId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT copy_id FROM book_requests WHERE request_id = ?")) {
            ps.setInt(1, requestId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int copyId = rs.getInt(1);
                    return rs.wasNull() ? null : copyId;
                }

                throw new SQLException("Request not found: " + requestId);
            }
        }
    }

    private int findAvailableCopy(Connection conn, int bookId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT copy_id FROM book_copies WHERE book_id = ? AND status = 'AVAILABLE' LIMIT 1")) {
            ps.setInt(1, bookId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    private void markCopy(Connection conn, int copyId, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE book_copies SET status = ? WHERE copy_id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, copyId);
            ps.executeUpdate();
        }
    }

    private void updateRequestIssued(Connection conn, int requestId, int copyId, String returnDate)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE book_requests SET status = 'ISSUED', copy_id = ?, return_date = ? WHERE request_id = ?")) {
            ps.setInt(1, copyId);
            ps.setDate(2, java.sql.Date.valueOf(LocalDate.parse(returnDate)));
            ps.setInt(3, requestId);
            ps.executeUpdate();
        }
    }

    private void updateRequestStatus(Connection conn, int requestId, String status)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE book_requests SET status = ? WHERE request_id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, requestId);
            ps.executeUpdate();
        }
    }
}