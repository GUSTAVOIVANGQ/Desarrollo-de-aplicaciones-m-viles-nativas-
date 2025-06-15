package com.example.systembooks.models;

import java.util.Date;

/**
 * Model class for favorite books
 */
public class Favorite {
    private long id;
    private long userId;
    private String bookId;
    private String title;
    private String author;
    private String coverUrl;
    private long timestamp;

    // Constructors
    public Favorite() {
        // Default constructor
    }

    public Favorite(long userId, String bookId, String title, String author, String coverUrl) {
        this.userId = userId;
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.coverUrl = coverUrl;
        this.timestamp = new Date().getTime(); // Current time in milliseconds
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Date getDate() {
        return new Date(timestamp);
    }
}