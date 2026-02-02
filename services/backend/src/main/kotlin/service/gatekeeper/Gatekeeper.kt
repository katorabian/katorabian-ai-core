package com.katorabian.service.gatekeeper

interface Gatekeeper {

    suspend fun interpret(
        input: String
    ): GatekeeperDecision
}