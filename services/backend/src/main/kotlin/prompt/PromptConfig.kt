package com.katorabian.prompt

data class PromptConfig(
    private val base: List<String>,
    private val behavior: List<String>,
    private val constraints: List<String>,
    private val taskHints: List<String>,
) {

    fun render(): String =
        buildString {
            appendBlock("BASE", base)
            appendBlock("BEHAVIOR", behavior)
            appendBlock("CONSTRAINTS", constraints)
            appendBlock("TASK", taskHints)
        }.trim()

    private fun StringBuilder.appendBlock(
        key: String,
        lines: List<String>
    ) {
        if (lines.isEmpty()) return

        appendLine("$key:")
        lines.forEach { appendLine("- $it") }
        appendLine()
    }
}