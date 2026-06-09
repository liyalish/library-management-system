package com.library.dao;

import com.library.model.BookRequest;

import java.util.List;
import java.util.Optional;

/**
 * Data-access operations for {@link BookRequest} entities.
 */
public interface BookRequestDao {

    /**
     * Creates a new book request.
     *
     * @param request the request to insert
     * @return the request with its generated id
     */
    BookRequest create(BookRequest request);

    /**
     * Finds a request by id.
     *
     * @param requestId the request id
     * @return an Optional containing the request, or empty if not found
     */
    Optional<BookRequest> findById(int requestId);

    /**
     * Returns all requests made by a specific reader, newest first.
     *
     * @param readerId the reader's user id
     * @return the reader's requests
     */
    List<BookRequest> findByReader(int readerId);

    /**
     * Returns all requests in the system (librarian view), newest first.
     *
     * @return all requests
     */
    List<BookRequest> findAll();

    /**
     * Updates the status, assigned copy, and return date of a request.
     *
     * @param request the request carrying new values
     */
    void update(BookRequest request);
}