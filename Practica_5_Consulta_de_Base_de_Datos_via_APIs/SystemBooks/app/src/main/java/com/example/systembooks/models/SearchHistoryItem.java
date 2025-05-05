package com.example.systembooks.models;

import java.util.Date;

/**
 * Model class representing an item in the user's search history.
 */
public class SearchHistoryItem {
    private long id;
    private long userId;
    private String query;
    private Date searchDate;

    public SearchHistoryItem() {
    }

    public SearchHistoryItem(long userId, String query) {
        this.userId = userId;
        this.query = query;
        this.searchDate = new Date();
    }

    public SearchHistoryItem(long id, long userId, String query, Date searchDate) {
        this.id = id;
        this.userId = userId;
        this.query = query;
        this.searchDate = searchDate;
    }

    // Getters and Setters
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

    public Date getSearchDate() {
        return searchDate;
    }

    public void setSearchDate(Date searchDate) {
        this.searchDate = searchDate;
    }
}