package com.library.dao;

import com.library.model.BookRequest;

import java.util.List;
import java.util.Optional;

public interface BookRequestDao {
    BookRequest create(BookRequest request);

    Optional<BookRequest> findById(int requestId);

    List<BookRequest> findByReader(int readerId);

    List<BookRequest> findAll();

    int countActiveByReader(int readerId);

    void update(BookRequest request);
}