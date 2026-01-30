package com.katorabian.service.gatekeeper

import com.katorabian.domain.ChatMessage

class RuleBasedGatekeeper : Gatekeeper {

    override suspend fun decide(
        input: String,
        history: List<ChatMessage>
    ): GatekeeperDecision {

        val lowered = input.lowercase()

        return when {
            isSensitive(lowered, history) ->
                GatekeeperDecision(
                    target = ExecutionTarget.LOCAL,
                    reason = "sensitive_content"
                )

            isRolePlay(lowered, history) ->
                GatekeeperDecision(
                    target = ExecutionTarget.LOCAL,
                    reason = "role_play"
                )

            isCodeOrReasoning(lowered) ->
                GatekeeperDecision(
                    target = ExecutionTarget.REMOTE,
                    reason = "code_or_reasoning"
                )

            else ->
                GatekeeperDecision(
                    target = ExecutionTarget.LOCAL,
                    reason = "default_chat"
                )
        }
    }

    private fun isSensitive(
        input: String,
        history: List<ChatMessage>
    ): Boolean {
        val keywords = listOf(
            "секс", "чувствую", "боюсь", "страшно",
            "люблю", "ненавижу", "стыдно"
        )

        return keywords.any { input.contains(it) }
    }

    private fun isRolePlay(
        input: String,
        history: List<ChatMessage>
    ): Boolean {
        return input.contains("*") ||
                input.contains("ролев") ||
                input.contains("представь") ||
                history.any { it.content.contains("*") }
    }

    private fun isCodeOrReasoning(input: String): Boolean {
        val keywords = listOf(
            "напиши код",
            "kotlin",
            "java",
            "архитектура",
            "алгоритм",
            "почему",
            "объясни",
            "оптимизируй"
        )

        return keywords.any { input.contains(it) }
    }
}
