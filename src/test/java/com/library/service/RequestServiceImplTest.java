package com.library.service;

import com.library.dao.BookRequestDao;
import com.library.exception.DaoException;
import com.library.exception.ServiceException;
import com.library.model.BookRequest;
import com.library.util.ConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private BookRequestDao requestDao;

    @InjectMocks
    private RequestServiceImpl requestService;

    private final List<Integer> requestIds = new ArrayList<>();
    private final List<Integer> copyIds = new ArrayList<>();
    private final List<Integer> bookIds = new ArrayList<>();
    private final List<Integer> userIds = new ArrayList<>();

    @AfterEach
    void cleanDatabaseRows() throws SQLException {
        try (Connection conn = ConnectionPool.getInstance().getConnection()) {
            deleteByIds(conn, "book_requests", "request_id", requestIds);
            deleteByIds(conn, "book_copies", "copy_id", copyIds);
            deleteByIds(conn, "books", "book_id", bookIds);
            deleteByIds(conn, "users", "user_id", userIds);
        }

        requestIds.clear();
        copyIds.clear();
        bookIds.clear();
        userIds.clear();
    }

    @Test
    void submitRequest_homeTypeCreatesPendingRequest() {
        when(requestDao.countActiveByReader(1)).thenReturn(0);
        when(requestDao.create(any(BookRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        BookRequest result = requestService.submitRequest(1, 2, "HOME");

        assertEquals(1, result.getReaderId());
        assertEquals(2, result.getBookId());
        assertEquals("HOME", result.getRequestType());
        assertEquals("PENDING", result.getStatus());

        verify(requestDao).countActiveByReader(1);
        verify(requestDao).create(any(BookRequest.class));
    }

    @Test
    void submitRequest_readingRoomTypeCreatesPendingRequest() {
        when(requestDao.countActiveByReader(1)).thenReturn(0);
        when(requestDao.create(any(BookRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        BookRequest result = requestService.submitRequest(1, 2, "READING_ROOM");

        assertEquals("READING_ROOM", result.getRequestType());
        assertEquals("PENDING", result.getStatus());

        verify(requestDao).countActiveByReader(1);
        verify(requestDao).create(any(BookRequest.class));
    }

    @Test
    void submitRequest_invalidTypeThrowsBeforeDaoUsage() {
        assertThrows(ServiceException.class,
                () -> requestService.submitRequest(1, 2, "INVALID_TYPE"));

        verifyNoInteractions(requestDao);
    }

    @Test
    void submitRequest_whenReaderHasFourActiveRequestsAllowsNewRequest() {
        when(requestDao.countActiveByReader(1)).thenReturn(4);
        when(requestDao.create(any(BookRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        BookRequest result = requestService.submitRequest(1, 2, "HOME");

        assertEquals("PENDING", result.getStatus());
        verify(requestDao).create(any(BookRequest.class));
    }

    @Test
    void submitRequest_whenActiveLimitReachedThrowsException() {
        when(requestDao.countActiveByReader(1)).thenReturn(5);

        assertThrows(ServiceException.class,
                () -> requestService.submitRequest(1, 2, "HOME"));

        verify(requestDao).countActiveByReader(1);
        verify(requestDao, never()).create(any(BookRequest.class));
    }

    @Test
    void submitRequest_whenDaoCannotReserveCopyWrapsException() {
        when(requestDao.countActiveByReader(1)).thenReturn(0);
        when(requestDao.create(any(BookRequest.class)))
                .thenThrow(new DaoException("No available copy for this book"));

        assertThrows(ServiceException.class,
                () -> requestService.submitRequest(1, 2, "HOME"));
    }

    @Test
    void getReaderRequestsShouldDelegateToDao() {
        List<BookRequest> requests = List.of(request(1, 10, 20, null, "PENDING"));
        when(requestDao.findByReader(10)).thenReturn(requests);

        List<BookRequest> result = requestService.getReaderRequests(10);

        assertEquals(requests, result);
        verify(requestDao).findByReader(10);
    }

    @Test
    void getAllRequestsShouldDelegateToDao() {
        List<BookRequest> requests = List.of(request(1, 10, 20, null, "PENDING"));
        when(requestDao.findAll()).thenReturn(requests);

        List<BookRequest> result = requestService.getAllRequests();

        assertEquals(requests, result);
        verify(requestDao).findAll();
    }

    @Test
    void cancelRequest_ownPendingRequestReleasesCopyAndCancels() throws SQLException {
        int readerId = createUser();
        int bookId = createBook();
        int copyId = createCopy(bookId, "RESERVED");
        int requestId = createRequest(readerId, bookId, copyId, "PENDING", null);

        BookRequest req = request(requestId, readerId, bookId, copyId, "PENDING");
        when(requestDao.findById(requestId)).thenReturn(Optional.of(req));

        requestService.cancelRequest(requestId, readerId);

        assertEquals("CANCELLED", requestStatus(requestId));
        assertEquals("AVAILABLE", copyStatus(copyId));
    }

    @Test
    void cancelRequest_otherUsersRequestThrowsException() {
        BookRequest req = request(10, 1, 2, null, "PENDING");
        when(requestDao.findById(10)).thenReturn(Optional.of(req));

        assertThrows(ServiceException.class,
                () -> requestService.cancelRequest(10, 2));
    }

    @Test
    void cancelRequest_alreadyIssuedThrowsException() {
        BookRequest req = request(10, 1, 2, null, "ISSUED");
        when(requestDao.findById(10)).thenReturn(Optional.of(req));

        assertThrows(ServiceException.class,
                () -> requestService.cancelRequest(10, 1));
    }

    @Test
    void cancelRequest_returnedRequestThrowsException() {
        BookRequest req = request(10, 1, 2, null, "RETURNED");
        when(requestDao.findById(10)).thenReturn(Optional.of(req));

        assertThrows(ServiceException.class,
                () -> requestService.cancelRequest(10, 1));
    }

    @Test
    void cancelRequest_notFoundThrowsException() {
        when(requestDao.findById(404)).thenReturn(Optional.empty());

        assertThrows(ServiceException.class,
                () -> requestService.cancelRequest(404, 1));
    }

    @Test
    void requestReturn_ownIssuedRequestSetsPendingReturn() {
        BookRequest req = request(20, 1, 2, 5, "ISSUED");
        when(requestDao.findById(20)).thenReturn(Optional.of(req));

        requestService.requestReturn(20, 1);

        assertEquals("PENDING_RETURN", req.getStatus());
        verify(requestDao).update(req);
    }

    @Test
    void requestReturn_otherUsersRequestThrowsException() {
        BookRequest req = request(20, 1, 2, 5, "ISSUED");
        when(requestDao.findById(20)).thenReturn(Optional.of(req));

        assertThrows(ServiceException.class,
                () -> requestService.requestReturn(20, 2));

        verify(requestDao, never()).update(any(BookRequest.class));
    }

    @Test
    void requestReturn_notIssuedThrowsException() {
        BookRequest req = request(20, 1, 2, 5, "PENDING");
        when(requestDao.findById(20)).thenReturn(Optional.of(req));

        assertThrows(ServiceException.class,
                () -> requestService.requestReturn(20, 1));

        verify(requestDao, never()).update(any(BookRequest.class));
    }

    @Test
    void requestReturn_notFoundThrowsException() {
        when(requestDao.findById(404)).thenReturn(Optional.empty());

        assertThrows(ServiceException.class,
                () -> requestService.requestReturn(404, 1));
    }

    @Test
    void rejectRequest_pendingRequestReleasesCopyAndRejects() throws SQLException {
        int readerId = createUser();
        int bookId = createBook();
        int copyId = createCopy(bookId, "RESERVED");
        int requestId = createRequest(readerId, bookId, copyId, "PENDING", null);

        BookRequest req = request(requestId, readerId, bookId, copyId, "PENDING");
        when(requestDao.findById(requestId)).thenReturn(Optional.of(req));

        requestService.rejectRequest(requestId);

        assertEquals("REJECTED", requestStatus(requestId));
        assertEquals("AVAILABLE", copyStatus(copyId));
    }

    @Test
    void rejectRequest_issuedRequestThrowsException() {
        BookRequest req = request(30, 1, 2, 5, "ISSUED");
        when(requestDao.findById(30)).thenReturn(Optional.of(req));

        assertThrows(ServiceException.class,
                () -> requestService.rejectRequest(30));
    }

    @Test
    void rejectRequest_notFoundThrowsException() {
        when(requestDao.findById(404)).thenReturn(Optional.empty());

        assertThrows(ServiceException.class,
                () -> requestService.rejectRequest(404));
    }

    @Test
    void issueBook_pendingReservedRequestChangesCopyToIssued() throws SQLException {
        int readerId = createUser();
        int bookId = createBook();
        int copyId = createCopy(bookId, "RESERVED");
        int requestId = createRequest(readerId, bookId, copyId, "PENDING", null);

        LocalDate returnDate = LocalDate.now().plusDays(14);

        requestService.issueBook(requestId, returnDate.toString());

        assertEquals("ISSUED", requestStatus(requestId));
        assertEquals("ISSUED", copyStatus(copyId));
        assertEquals(returnDate, requestReturnDate(requestId));
    }

    @Test
    void issueBook_notPendingRequestThrowsException() throws SQLException {
        int readerId = createUser();
        int bookId = createBook();
        int copyId = createCopy(bookId, "ISSUED");
        int requestId = createRequest(readerId, bookId, copyId, "ISSUED", LocalDate.now().plusDays(7));

        assertThrows(ServiceException.class,
                () -> requestService.issueBook(requestId, LocalDate.now().plusDays(10).toString()));
    }

    @Test
    void returnBook_pendingReturnChangesCopyToAvailable() throws SQLException {
        int readerId = createUser();
        int bookId = createBook();
        int copyId = createCopy(bookId, "ISSUED");
        int requestId = createRequest(readerId, bookId, copyId, "PENDING_RETURN", LocalDate.now().plusDays(3));

        requestService.returnBook(requestId);

        assertEquals("RETURNED", requestStatus(requestId));
        assertEquals("AVAILABLE", copyStatus(copyId));
    }

    @Test
    void returnBook_issuedWithoutPendingReturnThrowsException() throws SQLException {
        int readerId = createUser();
        int bookId = createBook();
        int copyId = createCopy(bookId, "ISSUED");
        int requestId = createRequest(readerId, bookId, copyId, "ISSUED", LocalDate.now().plusDays(3));

        assertThrows(ServiceException.class,
                () -> requestService.returnBook(requestId));
    }

    @Test
    void simplePendingRejectWithNullCopyDoesNotThrow() {
        BookRequest req = request(30, 1, 2, null, "PENDING");
        when(requestDao.findById(30)).thenReturn(Optional.of(req));

        assertDoesNotThrow(() -> requestService.rejectRequest(30));

        verify(requestDao).findById(30);
    }

    private BookRequest request(int requestId, int readerId, int bookId, Integer copyId, String status) {
        BookRequest request = new BookRequest();
        request.setRequestId(requestId);
        request.setReaderId(readerId);
        request.setBookId(bookId);
        request.setCopyId(copyId);
        request.setStatus(status);
        return request;
    }

    private int createUser() throws SQLException {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        String sql = """
                INSERT INTO users (username, password_hash, full_name, email, role)
                VALUES (?, ?, ?, ?, 'READER')
                """;

        int id = insert(sql,
                "test_reader_" + suffix,
                "$2a$10$C8I2fB50JCQppReWLOxpDOniP31RwfLNlmWKvCFntWQrz8YytiLL6",
                "Test Reader",
                "reader_" + suffix + "@mail.com");

        userIds.add(id);
        return id;
    }

    private int createBook() throws SQLException {
        String sql = """
                INSERT INTO books (title, description, publish_year)
                VALUES (?, ?, ?)
                """;

        int id = insert(sql,
                "Test Book " + UUID.randomUUID(),
                "Temporary test book",
                2026);

        bookIds.add(id);
        return id;
    }

    private int createCopy(int bookId, String status) throws SQLException {
        String sql = """
                INSERT INTO book_copies (book_id, inventory_number, status)
                VALUES (?, ?, ?)
                """;

        int id = insert(sql,
                bookId,
                "TC-" + UUID.randomUUID().toString().substring(0, 18),
                status);

        copyIds.add(id);
        return id;
    }

    private int createRequest(int readerId,
                              int bookId,
                              Integer copyId,
                              String status,
                              LocalDate returnDate) throws SQLException {
        String sql = """
                INSERT INTO book_requests (reader_id, copy_id, book_id, request_type, status, return_date)
                VALUES (?, ?, ?, 'HOME', ?, ?)
                """;

        try (Connection conn = ConnectionPool.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, readerId);

            if (copyId == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, copyId);
            }

            ps.setInt(3, bookId);
            ps.setString(4, status);

            if (returnDate == null) {
                ps.setNull(5, java.sql.Types.DATE);
            } else {
                ps.setDate(5, Date.valueOf(returnDate));
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                int id = keys.getInt(1);
                requestIds.add(id);
                return id;
            }
        }
    }

    private int insert(String sql, Object... values) throws SQLException {
        try (Connection conn = ConnectionPool.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (int i = 0; i < values.length; i++) {
                Object value = values[i];

                if (value instanceof Integer) {
                    ps.setInt(i + 1, (Integer) value);
                } else {
                    ps.setString(i + 1, value.toString());
                }
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private String requestStatus(int requestId) throws SQLException {
        return queryString("SELECT status FROM book_requests WHERE request_id = ?", requestId);
    }

    private LocalDate requestReturnDate(int requestId) throws SQLException {
        try (Connection conn = ConnectionPool.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT return_date FROM book_requests WHERE request_id = ?")) {

            ps.setInt(1, requestId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDate(1).toLocalDate();
            }
        }
    }

    private String copyStatus(int copyId) throws SQLException {
        return queryString("SELECT status FROM book_copies WHERE copy_id = ?", copyId);
    }

    private String queryString(String sql, int id) throws SQLException {
        try (Connection conn = ConnectionPool.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        }
    }

    private void deleteByIds(Connection conn, String table, String column, List<Integer> ids) throws SQLException {
        if (ids.isEmpty()) {
            return;
        }

        String placeholders = "?,".repeat(ids.size());
        placeholders = placeholders.substring(0, placeholders.length() - 1);

        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM " + table + " WHERE " + column + " IN (" + placeholders + ")")) {

            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }

            ps.executeUpdate();
        }
    }
}
