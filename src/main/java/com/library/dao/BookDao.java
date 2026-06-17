package com.library.dao;

import com.library.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookDao {
    Book create(Book book);
    Optional<Book> findById(int bookId);
    List<Book> findAll(String search, int limit, int offset);
    int count(String search);
    void update(Book book);
    void delete(int bookId);
}