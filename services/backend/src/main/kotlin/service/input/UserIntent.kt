package com.katorabian.service.input

import com.katorabian.prompt.BehaviorPrompt

sealed interface UserIntent {

    object Chat : UserIntent

    object Code : UserIntent

    data class ChangeStyle(
        val preset: BehaviorPrompt.Preset
    ) : UserIntent

    data class Command(
        val name: String
    ) : UserIntent
}
