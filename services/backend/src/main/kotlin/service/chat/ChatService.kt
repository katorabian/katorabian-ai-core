package com.katorabian.service.chat

import com.katorabian.domain.ChatMessage
import com.katorabian.domain.ChatSession
import com.katorabian.llm.LlmClient
import java.util.UUID

class ChatService(
    private val sessionService: ChatSessionService,
    private val messageService: ChatMessageService,
    private val promptService: PromptService,
    private val llmClient: LlmClient
) {

    fun createSession(model: String): ChatSession = sessionService.create(model)

    suspend fun sendMessage(
        sessionId: UUID,
        userQuery: String
    ): ChatMessage {

        val session = sessionService.get(sessionId)

        messageService.addUserMessage(
            sessionId = session.id,
            content = userQuery
        )

        val prompt = promptService.buildPromptForSession(session)
        val response = llmClient.generate(
            model = session.model,
            messages = prompt
        )

        return messageService.addAssistantMessage(
            sessionId = session.id,
            content = response
        )
    }

    suspend fun streamMessage(
        sessionId: UUID,
        userQuery: String,
        onToken: suspend (String) -> Unit
    ) {
        val session = sessionService.get(sessionId)

        messageService.addUserMessage(
            sessionId = session.id,
            content = userQuery
        )

        val prompt = promptService.buildPromptForSession(session)
        val buffer = StringBuilder()

        llmClient.stream(
            model = session.model,
            messages = prompt
        ) { token ->
            buffer.append(token)
            onToken(token)
        }

        messageService.addAssistantMessage(
            sessionId = session.id,
            content = buffer.toString()
        )
    }

    fun getAllSessions(): List<ChatSession> =
        sessionService.list()

    fun getSession(sessionId: UUID): ChatSession =
        sessionService.get(sessionId)

    fun getSessionMessages(sessionId: UUID): List<ChatMessage> =
        messageService.getMessages(sessionId)
}
