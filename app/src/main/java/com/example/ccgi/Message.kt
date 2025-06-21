package com.example.ccgi

import java.util.*

data class Message(
    val senderId: String,
    val content: String,
    val timestamp: Date? = null
)
