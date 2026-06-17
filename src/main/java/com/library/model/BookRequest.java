package com.library.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookRequest {
    private int requestId;
    private int readerId;
    private String readerName;
    private Integer copyId;
    private int bookId;
    private String bookTitle;
    private String requestType;
    private String status;
    private LocalDateTime requestDate;
    private LocalDate returnDate;

    public BookRequest() {
    }

    public boolean isOverdue() {
        return returnDate != null
                && ("ISSUED".equals(status) || "PENDING_RETURN".equals(status))
                && returnDate.isBefore(LocalDate.now());
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getReaderId() {
        return readerId;
    }

    public void setReaderId(int readerId) {
        this.readerId = readerId;
    }

    public String getReaderName() {
        return readerName;
    }

    public void setReaderName(String readerName) {
        this.readerName = readerName;
    }

    public Integer getCopyId() {
        return copyId;
    }

    public void setCopyId(Integer copyId) {
        this.copyId = copyId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BookRequest request = new BookRequest();

        public Builder readerId(int readerId) {
            request.readerId = readerId;
            return this;
        }

        public Builder bookId(int bookId) {
            request.bookId = bookId;
            return this;
        }

        public Builder requestType(String requestType) {
            request.requestType = requestType;
            return this;
        }

        public Builder status(String status) {
            request.status = status;
            return this;
        }

        public BookRequest build() {
            return request;
        }
    }
}