package com.example.systembooks.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class for friend requests
 */
public class FriendRequest {
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ACCEPTED = "accepted";
    public static final String STATUS_REJECTED = "rejected";

    @DocumentId
    private String id;
    private String senderId;
    private String senderName;
    private String senderEmail;
    private String senderPhotoUrl;
    private String receiverId;
    private String receiverName;
    private String receiverEmail;
    private String receiverPhotoUrl;
    private String status;
    private Long createdAt;
    private Long updatedAt;

    // Constructor vacío requerido por Firestore
    public FriendRequest() {
    }

    public FriendRequest(String senderId, String senderName, String senderEmail, String senderPhotoUrl,
                        String receiverId, String receiverName, String receiverEmail, String receiverPhotoUrl) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
        this.senderPhotoUrl = senderPhotoUrl;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.receiverEmail = receiverEmail;
        this.receiverPhotoUrl = receiverPhotoUrl;
        this.status = STATUS_PENDING;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("senderId", senderId);
        result.put("senderName", senderName);
        result.put("senderEmail", senderEmail);
        result.put("senderPhotoUrl", senderPhotoUrl);
        result.put("receiverId", receiverId);
        result.put("receiverName", receiverName);
        result.put("receiverEmail", receiverEmail);
        result.put("receiverPhotoUrl", receiverPhotoUrl);
        result.put("status", status);
        result.put("createdAt", createdAt);
        result.put("updatedAt", updatedAt);
        return result;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderPhotoUrl() {
        return senderPhotoUrl;
    }

    public void setSenderPhotoUrl(String senderPhotoUrl) {
        this.senderPhotoUrl = senderPhotoUrl;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getReceiverPhotoUrl() {
        return receiverPhotoUrl;
    }

    public void setReceiverPhotoUrl(String receiverPhotoUrl) {
        this.receiverPhotoUrl = receiverPhotoUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Exclude
    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    @Exclude
    public boolean isAccepted() {
        return STATUS_ACCEPTED.equals(status);
    }

    @Exclude
    public boolean isRejected() {
        return STATUS_REJECTED.equals(status);
    }
}
