package com.example.systembooks.models;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for imgbb API
 */
public class ImgbbResponse {
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("data")
    private ImageData data;
    
    @SerializedName("error")
    private ErrorData error;
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public ImageData getData() {
        return data;
    }
    
    public void setData(ImageData data) {
        this.data = data;
    }
    
    public ErrorData getError() {
        return error;
    }
    
    public void setError(ErrorData error) {
        this.error = error;
    }
    
    public static class ImageData {
        @SerializedName("id")
        private String id;
        
        @SerializedName("title")
        private String title;
        
        @SerializedName("url_viewer")
        private String urlViewer;
        
        @SerializedName("url")
        private String url;
        
        @SerializedName("display_url")
        private String displayUrl;
        
        @SerializedName("size")
        private long size;
        
        @SerializedName("time")
        private String time;
        
        @SerializedName("expiration")
        private String expiration;
        
        @SerializedName("image")
        private ImageDetails image;
        
        @SerializedName("thumb")
        private ImageDetails thumb;
        
        @SerializedName("medium")
        private ImageDetails medium;
        
        @SerializedName("delete_url")
        private String deleteUrl;
        
        // Getters and setters
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        public String getUrlViewer() {
            return urlViewer;
        }
        
        public void setUrlViewer(String urlViewer) {
            this.urlViewer = urlViewer;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getDisplayUrl() {
            return displayUrl;
        }
        
        public void setDisplayUrl(String displayUrl) {
            this.displayUrl = displayUrl;
        }
        
        public long getSize() {
            return size;
        }
        
        public void setSize(long size) {
            this.size = size;
        }
        
        public String getTime() {
            return time;
        }
        
        public void setTime(String time) {
            this.time = time;
        }
        
        public String getExpiration() {
            return expiration;
        }
        
        public void setExpiration(String expiration) {
            this.expiration = expiration;
        }
        
        public ImageDetails getImage() {
            return image;
        }
        
        public void setImage(ImageDetails image) {
            this.image = image;
        }
        
        public ImageDetails getThumb() {
            return thumb;
        }
        
        public void setThumb(ImageDetails thumb) {
            this.thumb = thumb;
        }
        
        public ImageDetails getMedium() {
            return medium;
        }
        
        public void setMedium(ImageDetails medium) {
            this.medium = medium;
        }
        
        public String getDeleteUrl() {
            return deleteUrl;
        }
        
        public void setDeleteUrl(String deleteUrl) {
            this.deleteUrl = deleteUrl;
        }
    }
    
    public static class ImageDetails {
        @SerializedName("filename")
        private String filename;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("mime")
        private String mime;
        
        @SerializedName("extension")
        private String extension;
        
        @SerializedName("url")
        private String url;
        
        @SerializedName("size")
        private long size;
        
        // Getters and setters
        public String getFilename() {
            return filename;
        }
        
        public void setFilename(String filename) {
            this.filename = filename;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getMime() {
            return mime;
        }
        
        public void setMime(String mime) {
            this.mime = mime;
        }
        
        public String getExtension() {
            return extension;
        }
        
        public void setExtension(String extension) {
            this.extension = extension;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public long getSize() {
            return size;
        }
        
        public void setSize(long size) {
            this.size = size;
        }
    }
    
    public static class ErrorData {
        @SerializedName("message")
        private String message;
        
        @SerializedName("code")
        private int code;
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public int getCode() {
            return code;
        }
        
        public void setCode(int code) {
            this.code = code;
        }
    }
}
