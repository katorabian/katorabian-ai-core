package com.katorabian.api.chat

import com.katorabian.domain.chat.ChatEvent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ChatEventEncoder {

    private val json = Json { encodeDefaults = true }

    fun encode(event: ChatEvent): String =
        json.encodeToString(event)
}