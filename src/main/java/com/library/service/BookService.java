package com.library.service;

import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Genre;

import java.util.List;

public interface BookService {
    Book addBook(Book book);

    Book getBook(int bookId);

    List<Book> getBooks(String search, Integer authorId, Integer genreId, int page, int pageSize);

    int getBookCount(String search, Integer authorId, Integer genreId);

    List<Author> getAuthors();

    List<Genre> getGenres();

    void updateBook(Book book);

    void deleteBook(int bookId);
}