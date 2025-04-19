package com.example.systembooks.models;

import java.util.Date;

public class FavoriteBook {
    private long id;
    private long userId;
    private String bookId;
    private String title;
    private String author;
    private String coverUrl;
    private Date dateAdded;

    public FavoriteBook() {
    }

    public FavoriteBook(long userId, String bookId, String title, String author, String coverUrl) {
        this.userId = userId;
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.coverUrl = coverUrl;
        this.dateAdded = new Date();
    }

    public FavoriteBook(long id, long userId, String bookId, String title, String author, 
                        String coverUrl, Date dateAdded) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.coverUrl = coverUrl;
        this.dateAdded = dateAdded;
    }

    // Convert to Book object for display purposes
    public Book toBook() {
        Book book = new Book(this.bookId, this.title, this.author);
        book.setCoverUrl(this.coverUrl);
        return book;
    }

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

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }
}