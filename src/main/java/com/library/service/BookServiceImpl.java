package com.library.service;

import com.library.dao.BookDao;
import com.library.exception.ServiceException;
import com.library.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Default implementation of {@link BookService}. Contains catalog business rules and
 * delegates persistence to {@link BookDao}.
 */
public class BookServiceImpl implements BookService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookDao bookDao;

    /**
     * Creates the service with the given DAO (constructor injection).
     *
     * @param bookDao the book data-access object
     */
    public BookServiceImpl(BookDao bookDao) {
        this.bookDao = bookDao;
    }

    @Override
    public Book addBook(Book book) {
        Book created = bookDao.create(book);
        LOGGER.info("Added book: {}", book.getTitle());
        return created;
    }

    @Override
    public Book getBook(int bookId) {
        return bookDao.findById(bookId)
                .orElseThrow(() -> new ServiceException("Book not found: " + bookId));
    }

    @Override
    public List<Book> getBooks(String search, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return bookDao.findAll(search, pageSize, offset);
    }

    @Override
    public int getBookCount(String search) {
        return bookDao.count(search);
    }

    @Override
    public void updateBook(Book book) {
        bookDao.update(book);
        LOGGER.info("Updated book id: {}", book.getBookId());
    }

    @Override
    public void deleteBook(int bookId) {
        bookDao.delete(bookId);
        LOGGER.info("Deleted book id: {}", bookId);
    }
}