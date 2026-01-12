package com.katorabian.api.chat

import com.katorabian.service.ChatService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.UUID

fun Route.chatSessionRoutes(chatService: ChatService) {

    post("/api/v1/chat/sessions") { _ ->
        val req = call.receive<CreateSessionRequest>()
        val session = chatService.createSession(req.model)
        call.respond(CreateSessionResponse(session.id.toString()))
    }

    post("/api/v1/chat/sessions/{id}/messages") { _ ->
        val sessionId = UUID.fromString(call.parameters["id"])
        val req = call.receive<SendMessageRequest>()

        val response = chatService.sendMessage(sessionId, req.message)
        call.respond(mapOf("content" to response.content))
    }
}

@Serializable
data class CreateSessionRequest(val model: String)

@Serializable
data class CreateSessionResponse(val sessionId: String)

@Serializable
data class SendMessageRequest(val message: String)
