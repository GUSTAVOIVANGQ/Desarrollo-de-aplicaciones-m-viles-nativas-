package com.example.pushnotificationapp

data class FCMResponse(
    val multicast_id: Long,
    val success: Int,
    val failure: Int,
    val canonical_ids: Int,
    val results: List<FCMResult>
)

data class FCMResult(
    val message_id: String?,
    val registration_id: String?,
    val error: String?
)
