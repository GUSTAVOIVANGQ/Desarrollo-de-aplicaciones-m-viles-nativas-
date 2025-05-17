package com.example.pushnotificationapp

data class FCMNotificationRequest(
    val to: String? = null,
    val registration_ids: List<String>? = null,
    val notification: NotificationData,
    val data: Map<String, String>? = null
)

data class NotificationData(
    val title: String,
    val body: String
)