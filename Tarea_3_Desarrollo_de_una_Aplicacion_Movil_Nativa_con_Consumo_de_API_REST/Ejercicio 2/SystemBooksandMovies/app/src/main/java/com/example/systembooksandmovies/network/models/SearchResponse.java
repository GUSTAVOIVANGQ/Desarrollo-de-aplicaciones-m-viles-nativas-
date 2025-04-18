package com.example.systembooksandmovies.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchResponse {
    
    @SerializedName("numFound")
    private int numFound;
    
    @SerializedName("start")
    private int start;
    
    @SerializedName("docs")
    private List<BookDoc> docs;
    
    public static class BookDoc {
        @SerializedName("key")
        private String key;
        
        @SerializedName("title")
        private String title;
        
        @SerializedName("author_name")
        private List<String> authorNames;
        
        @SerializedName("cover_i")
        private Long coverId;
        
        @SerializedName("first_publish_year")
        private Integer firstPublishYear;
        
        @SerializedName("publisher")
        private List<String> publishers;
        
        public String getKey() {
            return key;
        }
        
        public String getTitle() {
            return title;
        }
        
        public List<String> getAuthorNames() {
            return authorNames;
        }
        
        public Long getCoverId() {
            return coverId;
        }
        
        public Integer getFirstPublishYear() {
            return firstPublishYear;
        }
        
        public List<String> getPublishers() {
            return publishers;
        }
        
        public String getPrimaryAuthor() {
            return authorNames != null && !authorNames.isEmpty() ? authorNames.get(0) : "Desconocido";
        }
    }
    
    public int getNumFound() {
        return numFound;
    }
    
    public int getStart() {
        return start;
    }
    
    public List<BookDoc> getDocs() {
        return docs;
    }
}
