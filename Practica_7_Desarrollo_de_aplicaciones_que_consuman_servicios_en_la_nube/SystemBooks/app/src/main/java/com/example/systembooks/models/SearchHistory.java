package com.example.systembooks.models;

import java.util.Date;

/**
 * Model class for search history entries
 */
public class SearchHistory {
    private long id;
    private long userId;
    private String query;
    private long timestamp;
    private int resultCount;

    // Constructors
    public SearchHistory() {
        // Default constructor
    }

    public SearchHistory(long userId, String query, int resultCount) {
        this.userId = userId;
        this.query = query;
        this.resultCount = resultCount;
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

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
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

    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }
}