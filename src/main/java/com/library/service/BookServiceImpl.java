package com.library.service;

import com.library.dao.BookDao;
import com.library.exception.ServiceException;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Genre;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookDao bookDao;

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
    public List<Book> getBooks(String search, Integer authorId, Integer genreId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return bookDao.findAll(search, authorId, genreId, pageSize, offset);
    }

    @Override
    public int getBookCount(String search, Integer authorId, Integer genreId) {
        return bookDao.count(search, authorId, genreId);
    }

    @Override
    public List<Author> getAuthors() {
        return bookDao.findAllAuthors();
    }

    @Override
    public List<Genre> getGenres() {
        return bookDao.findAllGenres();
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