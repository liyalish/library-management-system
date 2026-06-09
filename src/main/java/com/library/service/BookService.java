package com.library.service;

import com.library.model.Book;

import java.util.List;

/**
 * Business operations for managing the book catalog.
 */
public interface BookService {

    /**
     * Adds a new book to the catalog.
     *
     * @param book the book to add
     * @return the created book with its generated id
     */
    Book addBook(Book book);

    /**
     * Retrieves a single book by id.
     *
     * @param bookId the book id
     * @return the book
     * @throws com.library.exception.ServiceException if no book has the given id
     */
    Book getBook(int bookId);

    /**
     * Returns a page of books, optionally filtered by a title search term.
     *
     * @param search   title search term (may be null or blank for no filter)
     * @param page     1-based page number
     * @param pageSize number of books per page
     * @return the books on the requested page
     */
    List<Book> getBooks(String search, int page, int pageSize);

    /**
     * Counts books matching an optional title search term (for pagination).
     *
     * @param search title search term (may be null or blank)
     * @return number of matching books
     */
    int getBookCount(String search);

    /**
     * Updates an existing book.
     *
     * @param book the book carrying updated values
     */
    void updateBook(Book book);

    /**
     * Deletes a book from the catalog.
     *
     * @param bookId the id of the book to delete
     */
    void deleteBook(int bookId);
}