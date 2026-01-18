package com.katorabian.service.chat

import com.katorabian.domain.ChatMessage
import com.katorabian.domain.ChatSession
import com.katorabian.domain.enum.Role
import com.katorabian.llm.LlmClient
import com.katorabian.storage.ChatSessionStore
import java.time.Instant
import java.util.UUID

class ChatService(
    private val llmClient: LlmClient,
    private val store: ChatSessionStore,
    private val promptService: PromptService
) {

    fun createSession(model: String): ChatSession {
        val session = ChatSession(
            id = UUID.randomUUID(),
            model = model,
            behaviorPreset = com.katorabian.prompt.BehaviorPrompt.Preset.NEUTRAL,
            createdAt = Instant.now()
        )
        store.createSession(session)
        return session
    }

    suspend fun sendMessage(
        sessionId: UUID,
        userQuery: String
    ): ChatMessage {
        val session = store.getSession(sessionId)
            ?: error("Session not found")

        val userMessage = ChatMessage(
            id = UUID.randomUUID(),
            sessionId = session.id,
            role = Role.USER,
            content = userQuery,
            createdAt = Instant.now()
        )
        store.addMessage(userMessage)

        val prompt = promptService.buildPromptForSession(session)

        val responseText = llmClient.generate(
            model = session.model,
            messages = prompt
        )

        val assistantMessage = ChatMessage(
            id = UUID.randomUUID(),
            sessionId = session.id,
            role = Role.ASSISTANT,
            content = responseText,
            createdAt = Instant.now()
        )
        store.addMessage(assistantMessage)

        return assistantMessage
    }

    suspend fun streamMessage(
        sessionId: UUID,
        userQuery: String,
        onToken: suspend (String) -> Unit
    ) {
        val session = store.getSession(sessionId)
            ?: error("Session not found")

        store.addMessage(
            ChatMessage(
                id = UUID.randomUUID(),
                sessionId = session.id,
                role = Role.USER,
                content = userQuery,
                createdAt = Instant.now()
            )
        )

        val prompt = promptService.buildPromptForSession(session)
        val assistantBuffer = StringBuilder()

        llmClient.stream(
            model = session.model,
            messages = prompt
        ) { token ->
            assistantBuffer.append(token)
            onToken(token)
        }

        store.addMessage(
            ChatMessage(
                id = UUID.randomUUID(),
                sessionId = session.id,
                role = Role.ASSISTANT,
                content = assistantBuffer.toString(),
                createdAt = Instant.now()
            )
        )
    }

    fun getAllSessions(): List<ChatSession> =
        store.getAllSessions()

    fun getSession(sessionId: UUID): ChatSession? =
        store.getSession(sessionId)

    fun getSessionMessages(sessionId: UUID): List<ChatMessage> =
        store.getMessages(sessionId)
}