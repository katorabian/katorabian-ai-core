package com.katorabian.service.input

import kotlinx.serialization.Serializable

@Serializable
data class ParsedCommand(
    val name: String,
    val args: List<String>
)
