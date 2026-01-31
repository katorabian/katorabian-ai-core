package com.katorabian.llm.gatekeeper

import com.katorabian.domain.ChatMessage
import com.katorabian.llm.LlmClient
import com.katorabian.service.gatekeeper.ExecutionTarget
import com.katorabian.service.gatekeeper.Gatekeeper
import com.katorabian.service.gatekeeper.GatekeeperDecision

class LlmGatekeeper(
    private val descriptor: GatekeeperDescriptor,
    private val llmClient: LlmClient
) : Gatekeeper {

    override suspend fun decide(input: String): GatekeeperDecision {

        val raw = llmClient.generate(
            model = descriptor.id,
            messages = listOf(
                ChatMessage.system(buildPrompt(input))
            )
        ).trim()

        val executionTarget = runCatching {
            ExecutionTarget.valueOf(raw.uppercase())
        }.getOrNull()

        return when (executionTarget) {
            ExecutionTarget.LOCAL ->
                GatekeeperDecision(ExecutionTarget.LOCAL, "gatekeeper=local")

            ExecutionTarget.REMOTE ->
                GatekeeperDecision(ExecutionTarget.REMOTE, "gatekeeper=remote")

            else ->
                GatekeeperDecision(
                    ExecutionTarget.LOCAL,
                    "gatekeeper=invalid_output:$raw"
                )
        }
    }

    private fun buildPrompt(input: String): String = """
        Ты — классификатор маршрутизации запросов.

        Верни ТОЛЬКО одно слово:
        LOCAL
        REMOTE

        Правила:
        - интим, личные переживания, эмоции, травмы, RP → LOCAL
        - код, программирование, reasoning, архитектура → REMOTE
        - при сомнении → LOCAL
        - без пояснений
        - без markdown

        Запрос:
        $input
    """.trimIndent()
}
