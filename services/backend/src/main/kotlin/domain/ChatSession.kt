package com.katorabian.domain

import com.katorabian.prompt.BehaviorPrompt
import java.time.Instant
import java.util.*

data class ChatSession(
    val id: UUID,
    val model: String,
    val behaviorPreset: BehaviorPrompt.Preset,
    val createdAt: Instant
)