package com.katorabian.service.gatekeeper

import com.katorabian.domain.ChatMessage

interface Gatekeeper {
    suspend fun decide(
        input: String,
        history: List<ChatMessage>
    ): GatekeeperDecision
}