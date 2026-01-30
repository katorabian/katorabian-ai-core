package com.katorabian.service.chat

import com.katorabian.domain.ChatMessage
import com.katorabian.domain.ChatSession
import com.katorabian.domain.Constants.MAX_SSE_CHUNK_SIZE
import com.katorabian.domain.chat.ChatEvent
import com.katorabian.service.gatekeeper.ExecutionTarget
import com.katorabian.service.gatekeeper.Gatekeeper
import com.katorabian.service.gatekeeper.RuleBasedGatekeeper
import com.katorabian.service.input.UserInputProcessor
import com.katorabian.service.message.ChatMessageService
import com.katorabian.service.model.ModelDescriptor
import com.katorabian.service.model.ModelRouter
import com.katorabian.service.model.ModelService
import com.katorabian.service.prompt.PromptService
import com.katorabian.service.session.ChatSessionService
import java.util.*

class ChatService(
    private val sessionService: ChatSessionService,
    private val messageService: ChatMessageService,
    private val promptService: PromptService,
    private val modelRouter: ModelRouter,
    private val modelService: ModelService,
    private val inputProcessor: UserInputProcessor
) {
    private val gatekeeper: Gatekeeper = RuleBasedGatekeeper()

    fun createSession(): ChatSession = sessionService.create()

    suspend fun sendMessage(
        sessionId: UUID,
        userQuery: String
    ): ChatMessage {

        val session = sessionService.get(sessionId)

        return inputProcessor.process(
            session = session,
            input = userQuery
        ).fold(
            onSystemResponse = { userMessage ->
                messageService.addAssistantMessage(session.id, userMessage)
            },
            onForwardToLlm = { userMessage ->
                messageService.addUserMessage(session.id, userMessage)

                val prompt = promptService.buildPromptForSession(session)
                val model = defineModel(gatekeeper, userQuery, modelService, session)
                val response = modelService.withInference(model) {
                    model.client.generate(
                        model = model.id,
                        messages = prompt
                    )
                }

                messageService.addAssistantMessage(
                    sessionId = session.id,
                    content = response
                )
            }
        )
    }

    suspend fun streamMessage(
        sessionId: UUID,
        userQuery: String,
        emit: suspend (ChatEvent) -> Unit
    ) {
        val session = sessionService.get(sessionId)

        inputProcessor.process(
            session = session,
            input = userQuery
        ).fold(
            onSystemResponse = { text ->
                emit(ChatEvent.SystemMessage(text))
            },
            onForwardToLlm = { userMessage ->

                messageService.addUserMessage(session.id, userMessage)

                emit(ChatEvent.Thinking())

                val buffer = StringBuilder()
                val prompt = promptService.buildPromptForSession(session)

                runCatching {
                    val model = defineModel(gatekeeper, userQuery, modelService, session)
                    modelService.withInference(model) {
                        model.client.stream(
                            model = model.id,
                            messages = prompt
                        ) { chunk ->
                            buffer.append(chunk)
                            splitForSse(chunk).forEach { safePart ->
                                emit(ChatEvent.Token(safePart))
                            }
                        }
                    }
                    emit(ChatEvent.Completed)

                    val full = buffer.toString()
                    messageService.addAssistantMessage(session.id, full)

                }.getOrElse {
                    emit(ChatEvent.Error(it.message ?: "Unknown error"))
                }
            }
        )
    }

    private suspend fun defineModel(
        gatekeeper: Gatekeeper,
        userQuery: String,
        modelService: ModelService,
        session: ChatSession,
    ): ModelDescriptor {

        val decision = gatekeeper.decide(
            input = userQuery,
            history = messageService.getMessages(session.id)
        )

        val model = when (decision.target) {
            ExecutionTarget.REMOTE -> runCatching {
                modelRouter.resolveRemote(
                    input = userQuery,
                    modelService = modelService
                )
            }.getOrElse {
                // fallback если remote умер / не реализован
                modelRouter.resolveLocal(
                    input = userQuery,
                    modelService = modelService
                )
            }
            ExecutionTarget.LOCAL -> {
                modelRouter.resolveLocal(
                    input = userQuery,
                    modelService = modelService
                )
            }
        }

        return model.also {
            println("Using model: ${it.id} (${it.role}) | decision=${decision.reason}")
        }
    }

    fun getAllSessions(): List<ChatSession> =
        sessionService.list()

    fun getSession(sessionId: UUID): ChatSession =
        sessionService.get(sessionId)

    fun getSessionMessages(sessionId: UUID): List<ChatMessage> =
        messageService.getMessages(sessionId)

    private fun splitForSse(text: String): List<String> =
        text.chunked(MAX_SSE_CHUNK_SIZE)
}
