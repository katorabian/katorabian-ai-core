package com.katorabian.domain.chat

import kotlinx.serialization.Serializable


@Serializable
data class CreateSessionResponse(val sessionId: String)

@Serializable
data class SendMessageRequest(val message: String)
