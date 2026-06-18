package com.library.model;

import java.util.ArrayList;
import java.util.List;

public class Book {
    private int bookId;
    private String title;

    private List<Integer> authorIds = new ArrayList<>();
    private String authorIdsText;

    private String authorName;
    private String genreNames;

    private String description;
    private Integer publishYear;

    private int availableCopies;

    public Book() {
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Integer> getAuthorIds() {
        return authorIds;
    }

    public void setAuthorIds(List<Integer> authorIds) {
        this.authorIds = authorIds;
        this.authorIdsText = authorIds == null ? "" : joinIds(authorIds);
    }

    public String getAuthorIdsText() {
        if (authorIdsText != null) {
            return authorIdsText;
        }
        return joinIds(authorIds);
    }

    public void setAuthorIdsText(String authorIdsText) {
        this.authorIdsText = authorIdsText;
        this.authorIds = parseIds(authorIdsText);
    }

    public int getAuthorId() {
        return authorIds == null || authorIds.isEmpty() ? 0 : authorIds.get(0);
    }

    public void setAuthorId(int authorId) {
        this.authorIds = new ArrayList<>();
        if (authorId > 0) {
            this.authorIds.add(authorId);
        }
        this.authorIdsText = joinIds(this.authorIds);
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getGenreNames() {
        return genreNames;
    }

    public void setGenreNames(String genreNames) {
        this.genreNames = genreNames;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(Integer publishYear) {
        this.publishYear = publishYear;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    private List<Integer> parseIds(String text) {
        List<Integer> ids = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return ids;
        }

        String[] parts = text.split(",");

        for (String part : parts) {
            String trimmed = part.trim();

            if (!trimmed.isEmpty()) {
                ids.add(Integer.parseInt(trimmed));
            }
        }

        return ids;
    }

    private String joinIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                result.append(",");
            }

            result.append(ids.get(i));
        }

        return result.toString();
    }
}