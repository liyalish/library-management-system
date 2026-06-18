package com.library.service;

import com.library.dao.BookDao;
import com.library.exception.ServiceException;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Genre;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookDao bookDao;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    void addBookShouldCreateBook() {
        Book book = new Book();
        book.setTitle("1984");

        when(bookDao.create(book)).thenReturn(book);

        Book result = bookService.addBook(book);

        assertSame(book, result);
        verify(bookDao).create(book);
    }

    @Test
    void getBookShouldReturnBookWhenExists() {
        Book book = new Book();
        book.setBookId(1);
        book.setTitle("1984");

        when(bookDao.findById(1)).thenReturn(Optional.of(book));

        Book result = bookService.getBook(1);

        assertSame(book, result);
        verify(bookDao).findById(1);
    }

    @Test
    void getBookShouldThrowWhenNotFound() {
        when(bookDao.findById(99)).thenReturn(Optional.empty());

        assertThrows(ServiceException.class, () -> bookService.getBook(99));

        verify(bookDao).findById(99);
    }

    @Test
    void getBooksShouldUsePaginationAndFilters() {
        Book book = new Book();
        book.setBookId(1);
        book.setTitle("1984");

        List<Book> books = List.of(book);

        when(bookDao.findAll("java", 2, 3, 5, 5)).thenReturn(books);

        List<Book> result = bookService.getBooks("java", 2, 3, 2, 5);

        assertSame(books, result);
        verify(bookDao).findAll("java", 2, 3, 5, 5);
    }

    @Test
    void getBookCountShouldUseFilters() {
        when(bookDao.count("java", 2, 3)).thenReturn(7);

        int result = bookService.getBookCount("java", 2, 3);

        assertEquals(7, result);
        verify(bookDao).count("java", 2, 3);
    }

    @Test
    void getAuthorsShouldReturnAuthors() {
        List<Author> authors = List.of(new Author(1, "George Orwell"));

        when(bookDao.findAllAuthors()).thenReturn(authors);

        List<Author> result = bookService.getAuthors();

        assertSame(authors, result);
        verify(bookDao).findAllAuthors();
    }

    @Test
    void getGenresShouldReturnGenres() {
        List<Genre> genres = List.of(new Genre(1, "Fiction"));

        when(bookDao.findAllGenres()).thenReturn(genres);

        List<Genre> result = bookService.getGenres();

        assertSame(genres, result);
        verify(bookDao).findAllGenres();
    }

    @Test
    void updateBookShouldCallDao() {
        Book book = new Book();
        book.setBookId(1);
        book.setTitle("Updated");

        bookService.updateBook(book);

        verify(bookDao).update(book);
    }

    @Test
    void deleteBookShouldCallDao() {
        bookService.deleteBook(1);

        verify(bookDao).delete(1);
    }
}