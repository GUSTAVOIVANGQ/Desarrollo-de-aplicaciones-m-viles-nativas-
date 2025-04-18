package com.example.systembooksandmovies.network.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CategoryResponse {
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("work_count")
    private int workCount;
    
    @SerializedName("works")
    private List<Work> works;
    
    public static class Work {
        @SerializedName("key")
        private String key;
        
        @SerializedName("title")
        private String title;
        
        @SerializedName("edition_count")
        private int editionCount;
        
        @SerializedName("cover_id")
        private Long coverId;
        
        @SerializedName("cover_edition_key")
        private String coverEditionKey;
        
        @SerializedName("authors")
        private List<Author> authors;
        
        public static class Author {
            @SerializedName("name")
            private String name;
            
            @SerializedName("key")
            private String key;
            
            public String getName() {
                return name;
            }
            
            public String getKey() {
                return key;
            }
        }
        
        public String getKey() {
            return key;
        }
        
        public String getTitle() {
            return title;
        }
        
        public int getEditionCount() {
            return editionCount;
        }
        
        public Long getCoverId() {
            return coverId;
        }
        
        public String getCoverEditionKey() {
            return coverEditionKey;
        }
        
        public List<Author> getAuthors() {
            return authors;
        }
        
        public String getAuthorName() {
            return authors != null && !authors.isEmpty() ? authors.get(0).getName() : "Desconocido";
        }
    }
    
    public String getName() {
        return name;
    }
    
    public int getWorkCount() {
        return workCount;
    }
    
    public List<Work> getWorks() {
        return works;
    }
}
