package com.katorabian.service.gatekeeper

enum class ExecutionTarget {
    LOCAL,
    REMOTE
}

data class GatekeeperDecision(
    val target: ExecutionTarget,
    val reason: String
)
