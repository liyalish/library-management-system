package com.library.dao;

import com.library.model.Book;

import java.util.List;
import java.util.Optional;

/**
 * Data-access operations for {@link Book} entities.
 */
public interface BookDao {

    /**
     * Persists a new book.
     *
     * @param book the book to insert
     * @return the book with its generated id populated
     */
    Book create(Book book);

    /**
     * Finds a book by id, including the author's name.
     *
     * @param bookId the book id
     * @return an Optional containing the book, or empty if none found
     */
    Optional<Book> findById(int bookId);

    /**
     * Returns a page of books, optionally filtered by a title search term.
     *
     * @param search title search term (may be null or blank for no filter)
     * @param limit  maximum number of rows
     * @param offset number of rows to skip
     * @return the list of books for the requested page
     */
    List<Book> findAll(String search, int limit, int offset);

    /**
     * Counts books matching an optional title search term (used for pagination).
     *
     * @param search title search term (may be null or blank)
     * @return number of matching books
     */
    int count(String search);

    /**
     * Updates an existing book.
     *
     * @param book the book carrying updated values
     */
    void update(Book book);

    /**
     * Deletes a book by id.
     *
     * @param bookId the id of the book to delete
     */
    void delete(int bookId);
}