package com.katorabian.api.chat

import com.katorabian.service.chat.ChatService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

fun Route.chatStreamRoute(chatService: ChatService) {

    get("/api/v1/chat/sessions/{id}/stream") {

        val sessionId = UUID.fromString(call.parameters["id"])
        val message = call.request.queryParameters["message"]
            ?: return@get call.respondText("message required")

        call.respondTextWriter(
            contentType = ContentType.Text.EventStream
        ) {
            chatService.streamMessage(
                sessionId = sessionId,
                userQuery = message
            ) { event ->
                val json = ChatEventEncoder.encode(event)
                write("event: message\n")
                write("""data: $json""")
                write("\n\n")
                flush()
            }

            write("event: done\ndata: {}\n\n")
            flush()
        }
    }
}
