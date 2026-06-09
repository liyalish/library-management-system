package com.library.service;

import com.library.model.BookRequest;

import java.util.List;

/**
 * Business operations for book requests (borrowing workflow).
 */
public interface RequestService {

    /**
     * Submits a new request for a book by a reader.
     *
     * @param readerId    the reader's user id
     * @param bookId      the requested book id
     * @param requestType "HOME" or "READING_ROOM"
     * @return the created request
     */
    BookRequest submitRequest(int readerId, int bookId, String requestType);

    /**
     * Returns the requests belonging to a reader.
     *
     * @param readerId the reader's user id
     * @return the reader's requests
     */
    List<BookRequest> getReaderRequests(int readerId);

    /**
     * Returns all requests (librarian view).
     *
     * @return all requests
     */
    List<BookRequest> getAllRequests();

    /**
     * Cancels a pending request. Only the owning reader may cancel, and only while the
     * request is still PENDING (not yet issued).
     *
     * @param requestId the request id
     * @param readerId  the id of the reader attempting to cancel
     */
    void cancelRequest(int requestId, int readerId);

    /**
     * Issues a book for a pending request: assigns an available copy, sets the return date,
     * and marks the copy as issued. Performed in a single database transaction.
     *
     * @param requestId  the request id
     * @param returnDate the due date for returning the book (ISO yyyy-MM-dd)
     */
    void issueBook(int requestId, String returnDate);

    /**
     * Marks an issued request as returned and frees the copy. Done in a transaction.
     *
     * @param requestId the request id
     */
    void returnBook(int requestId);
}