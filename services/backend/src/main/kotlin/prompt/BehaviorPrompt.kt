package com.katorabian.prompt

object BehaviorPrompt {

    fun forPreset(preset: Preset): List<String> =
        when (preset) {
            Preset.NEUTRAL -> emptyList()

            Preset.SARCASTIC -> listOf(
                "Tone: sarcastic",
                "Manner: ironic, slightly rude",
                "No politeness padding"
            )

            Preset.FORMAL -> listOf(
                "Tone: formal",
                "Manner: professional, restrained",
                "No casual language"
            )

            Preset.CONCISE -> listOf(
                "Style: concise",
                "Minimal explanations",
                "No verbosity"
            )
        }


    enum class Preset {
        NEUTRAL,
        SARCASTIC,
        FORMAL,
        CONCISE
    }
}