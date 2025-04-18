package com.example.systembooksandmovies.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BookResponse {
    
    @SerializedName("key")
    private String key;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("description")
    private Description description;
    
    @SerializedName("covers")
    private List<Long> covers;
    
    @SerializedName("authors")
    private List<Author> authors;
    
    @SerializedName("publish_date")
    private String publishDate;
    
    @SerializedName("publishers")
    private List<Publisher> publishers;
    
    @SerializedName("number_of_pages")
    private Integer numberOfPages;
    
    public static class Description {
        @SerializedName("value")
        private String value;

        public String getValue() {
            return value;
        }
    }
    
    public static class Author {
        @SerializedName("author")
        private AuthorInfo authorInfo;
        
        public static class AuthorInfo {
            @SerializedName("key")
            private String key;
            
            public String getKey() {
                return key;
            }
        }
        
        public String getAuthorKey() {
            return authorInfo != null ? authorInfo.getKey() : null;
        }
    }
    
    public static class Publisher {
        @SerializedName("name")
        private String name;
        
        public String getName() {
            return name;
        }
    }
    
    // Getters
    public String getKey() {
        return key;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescriptionText() {
        return description != null ? description.getValue() : null;
    }
    
    public List<Long> getCovers() {
        return covers;
    }
    
    public List<Author> getAuthors() {
        return authors;
    }
    
    public String getPublishDate() {
        return publishDate;
    }
    
    public List<Publisher> getPublishers() {
        return publishers;
    }
    
    public Integer getNumberOfPages() {
        return numberOfPages;
    }
}
