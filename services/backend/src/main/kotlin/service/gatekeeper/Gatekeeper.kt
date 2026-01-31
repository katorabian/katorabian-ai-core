package com.katorabian.service.gatekeeper

interface Gatekeeper {
    suspend fun decide(input: String): GatekeeperDecision
}