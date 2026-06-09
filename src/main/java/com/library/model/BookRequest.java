package com.library.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a reader's request to borrow a book. Mirrors the {@code book_requests} table.
 * The {@code bookTitle} and {@code readerName} fields are filled by joins for display.
 */
public class BookRequest {

    private int requestId;
    private int readerId;
    private String readerName;
    private Integer copyId;
    private int bookId;
    private String bookTitle;
    private String requestType;   // HOME or READING_ROOM
    private String status;        // PENDING, ISSUED, RETURNED, CANCELLED
    private LocalDateTime requestDate;
    private LocalDate returnDate;

    public BookRequest() {
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

    /**
     * Creates a new builder for constructing a {@link BookRequest} step by step.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link BookRequest} (Builder design pattern). Allows readable,
     * step-by-step construction of a request without a long constructor.
     */
    public static class Builder {
        private final BookRequest request = new BookRequest();

        /**
         * Sets the reader id.
         *
         * @param readerId the reader's user id
         * @return this builder
         */
        public Builder readerId(int readerId) {
            request.readerId = readerId;
            return this;
        }

        /**
         * Sets the book id.
         *
         * @param bookId the requested book id
         * @return this builder
         */
        public Builder bookId(int bookId) {
            request.bookId = bookId;
            return this;
        }

        /**
         * Sets the request type (HOME or READING_ROOM).
         *
         * @param requestType the request type
         * @return this builder
         */
        public Builder requestType(String requestType) {
            request.requestType = requestType;
            return this;
        }

        /**
         * Sets the status.
         *
         * @param status the request status
         * @return this builder
         */
        public Builder status(String status) {
            request.status = status;
            return this;
        }

        /**
         * Returns the fully constructed {@link BookRequest}.
         *
         * @return the built request
         */
        public BookRequest build() {
            return request;
        }
    }
}