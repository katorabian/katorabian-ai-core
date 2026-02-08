package com.katorabian.service.prompt

import com.katorabian.domain.ChatMessage
import com.katorabian.domain.ChatSession
import com.katorabian.domain.enum.Role
import com.katorabian.prompt.PromptConfigFactory
import java.time.Instant
import java.util.UUID

class PromptAssembler(
    private val promptConfigFactory: PromptConfigFactory
) {

    fun assemble(
        session: ChatSession,
        history: List<ChatMessage>,
        taskHints: List<String> = emptyList()
    ): List<ChatMessage> {

        val systemPrompt = promptConfigFactory
            .build(
                behaviorPreset = session.behaviorPreset,
                taskHints = taskHints
            )
            .render()

        val systemMessage = ChatMessage(
            id = UUID.randomUUID(),
            sessionId = session.id,
            role = Role.SYSTEM,
            content = systemPrompt,
            createdAt = Instant.EPOCH
        )

        val conversation = history
            .filter { it.role == Role.USER || it.role == Role.ASSISTANT }
            .sortedBy { it.createdAt }

        val normalizedConversation = normalizeConversation(conversation)

        return buildList {
            add(
                ChatMessage.system("[System]\n${systemPrompt}")
            )

            normalizedConversation.forEach { msg ->
                val roleTag = when (msg.role) {
                    Role.USER -> "[User]"
                    Role.ASSISTANT -> "[Assistant]"
                    Role.SYSTEM -> return@forEach // на всякий случай
                }

                add(
                    msg.copy(
                        content = "$roleTag\n${msg.content}"
                    )
                )
            }
        }
    }

    fun assemblePrompt(
        session: ChatSession,
        history: List<ChatMessage>,
        taskHints: List<String> = emptyList()
    ): String {
        val system = promptConfigFactory
            .build(session.behaviorPreset, taskHints)
            .render()

        val conversation = history
            .filter { it.role == Role.USER || it.role == Role.ASSISTANT }
            .sortedBy { it.createdAt }
            .joinToString("\n\n") {
                "[${it.role.name.lowercase().replaceFirstChar(Char::uppercase)}]\n${it.content}"
            }

        return """
        [System]
        $system

        $conversation

        [Assistant]
    """.trimIndent()
    }


    /**
     * Гарантирует:
     * - нет подряд одинаковых ролей
     * - порядок сохранён
     */
    private fun normalizeConversation(
        conversation: List<ChatMessage>
    ): List<ChatMessage> {
        val result = ArrayList<ChatMessage>(conversation.size)

        for (msg in conversation) {
            val last = result.lastOrNull()
            if (last?.role == msg.role) {
                // удаляем предыдущий, оставляем последний по времени
                result.removeAt(result.lastIndex)
            }
            result.add(msg)
        }

        return result
    }
}
