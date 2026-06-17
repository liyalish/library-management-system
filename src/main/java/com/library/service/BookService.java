package com.library.service;

import com.library.model.Book;

import java.util.List;

public interface BookService {
    Book addBook(Book book);

    Book getBook(int bookId);

    List<Book> getBooks(String search, int page, int pageSize);

    int getBookCount(String search);

    void updateBook(Book book);

    void deleteBook(int bookId);
}