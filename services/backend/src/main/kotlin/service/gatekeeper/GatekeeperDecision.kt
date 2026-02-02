package com.katorabian.service.gatekeeper

import com.katorabian.service.input.ParsedCommand
import com.katorabian.service.input.UserIntent

data class GatekeeperDecision(
    val executionTarget: ExecutionTarget,
    val intent: UserIntent,
    val command: ParsedCommand?,
    val reason: String
)
