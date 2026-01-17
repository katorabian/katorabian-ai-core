package com.katorabian.prompt

class PromptConfigFactory {

    fun build(
        behaviorPreset: BehaviorPrompt.Preset,
        taskHints: List<String>
    ): PromptConfig =
        PromptConfig(
            base = BasePrompt.rules,
            behavior = BehaviorPrompt.forPreset(behaviorPreset),
            constraints = ConstraintsPrompt.rules,
            taskHints = taskHints
        )
}