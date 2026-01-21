package com.katorabian.domain.chat

import kotlinx.serialization.Serializable

@Serializable
sealed interface ChatEvent {

    @Serializable
    data class Thinking(
        val message: String = "thinking"
    ) : ChatEvent

    @Serializable
    data class Token(
        val text: String
    ) : ChatEvent

    @Serializable
    data class Completed(
        val reason: String = "done"
    ) : ChatEvent

    @Serializable
    data class SystemMessage(
        val text: String
    ) : ChatEvent

    @Serializable
    data class Error(
        val message: String
    ) : ChatEvent
}
