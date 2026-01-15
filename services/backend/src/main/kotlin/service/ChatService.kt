package com.katorabian.service

import com.katorabian.domain.ChatMessage
import com.katorabian.domain.ChatSession
import com.katorabian.domain.enum.Role
import com.katorabian.llm.LlmClient
import com.katorabian.llm.SystemPrompts
import java.time.Instant
import java.util.UUID

class ChatService(
    private val llmClient: LlmClient,
    private val store: ChatSessionStore
) {

    fun createSession(
        model: String,
        systemPrompt: String?
    ): ChatSession {
        val session = ChatSession(
            id = UUID.randomUUID(),
            model = model,
            systemPrompt = systemPrompt ?: SystemPrompts.DEFAULT,
            createdAt = Instant.now()
        )
        store.createSession(session)
        return session
    }

    suspend fun sendMessage(
        sessionId: UUID,
        userContent: String
    ): ChatMessage {
        val session = store.getSession(sessionId)
            ?: error("Session not found")

        val userMessage = ChatMessage(
            id = UUID.randomUUID(),
            sessionId = session.id,
            role = Role.USER,
            content = userContent,
            createdAt = Instant.now()
        )

        store.addMessage(userMessage)

        val history = store.getMessages(session.id)

        val messagesForLlm = buildList {
            add(
                ChatMessage(
                    id = UUID.randomUUID(),
                    sessionId = session.id,
                    role = Role.SYSTEM,
                    content = session.systemPrompt,
                    createdAt = Instant.EPOCH
                )
            )
            addAll(history)
        }

        val responseText = llmClient.generate(
            model = session.model,
            messages = messagesForLlm
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

        // 1. Сохраняем сообщение пользователя
        store.addMessage(
            ChatMessage(
                id = UUID.randomUUID(),
                sessionId = sessionId,
                role = Role.USER,
                content = userQuery,
                createdAt = Instant.now()
            )
        )

        // 2. Стримим токены и копим текст
        val assistantBuffer = StringBuilder()
        val history = store.getMessages(sessionId)

        val messagesForLlm = buildList {
            add(
                ChatMessage(
                    id = UUID.randomUUID(),
                    sessionId = sessionId,
                    role = Role.SYSTEM,
                    content = session.systemPrompt,
                    createdAt = Instant.EPOCH
                )
            )
            addAll(history)
        }

        llmClient.stream(
            model = session.model,
            messages = messagesForLlm
        ) { token ->
            assistantBuffer.append(token)
            onToken(token)
        }

        // 3. Сохраняем результрующий ответ ИИ
        store.addMessage(
            ChatMessage(
                id = UUID.randomUUID(),
                sessionId = sessionId,
                role = Role.ASSISTANT,
                content = assistantBuffer.toString(),
                createdAt = Instant.now()
            )
        )
    }

    fun listSessions(): List<ChatSession> =
        store.getAllSessions()

    fun getSession(sessionId: UUID): ChatSession? =
        store.getSession(sessionId)

    fun getSessionMessages(sessionId: UUID): List<ChatMessage> =
        store.getMessages(sessionId)

}
