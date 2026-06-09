package com.library.service;

import com.library.dao.BookDao;
import com.library.exception.ServiceException;
import com.library.model.Book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BookServiceImpl}. The DAO is mocked to test catalog business
 * logic (retrieval, not-found handling, pagination offset) in isolation.
 */
@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookDao bookDao;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    void getBook_existing_returnsBook() {
        Book book = new Book();
        book.setBookId(1);
        book.setTitle("1984");
        when(bookDao.findById(1)).thenReturn(Optional.of(book));

        Book result = bookService.getBook(1);

        assertEquals("1984", result.getTitle());
    }

    @Test
    void getBook_missing_throwsException() {
        when(bookDao.findById(99)).thenReturn(Optional.empty());

        assertThrows(ServiceException.class, () -> bookService.getBook(99));
    }

    @Test
    void addBook_delegatesToDao() {
        Book book = new Book();
        book.setTitle("New Book");
        when(bookDao.create(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.addBook(book);

        assertEquals("New Book", result.getTitle());
        verify(bookDao).create(book);
    }

    @Test
    void getBooks_computesOffsetFromPage() {
        when(bookDao.findAll("java", 5, 10)).thenReturn(List.of());

        bookService.getBooks("java", 3, 5);

        // page 3, size 5 -> offset 10
        verify(bookDao).findAll("java", 5, 10);
    }

    @Test
    void updateBook_delegatesToDao() {
        Book book = new Book();
        book.setBookId(2);
        bookService.updateBook(book);
        verify(bookDao).update(book);
    }

    @Test
    void deleteBook_delegatesToDao() {
        bookService.deleteBook(5);
        verify(bookDao).delete(5);
    }

    @Test
    void getBookCount_delegatesToDao() {
        when(bookDao.count("x")).thenReturn(7);
        assertEquals(7, bookService.getBookCount("x"));
    }
}