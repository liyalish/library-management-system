package com.library.dao;

import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Genre;

import java.util.List;
import java.util.Optional;

public interface BookDao {
    Book create(Book book);

    Optional<Book> findById(int bookId);

    List<Book> findAll(String search, Integer authorId, Integer genreId, int limit, int offset);

    int count(String search, Integer authorId, Integer genreId);

    List<Author> findAllAuthors();

    List<Genre> findAllGenres();

    void update(Book book);

    void delete(int bookId);
}