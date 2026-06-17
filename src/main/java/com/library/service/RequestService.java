package com.library.service;

import com.library.model.BookRequest;

import java.util.List;

public interface RequestService {
    BookRequest submitRequest(int readerId, int bookId, String requestType);

    List<BookRequest> getReaderRequests(int readerId);

    List<BookRequest> getAllRequests();

    void cancelRequest(int requestId, int readerId);

    void requestReturn(int requestId, int readerId);

    void issueBook(int requestId, String returnDate);

    void returnBook(int requestId);
}