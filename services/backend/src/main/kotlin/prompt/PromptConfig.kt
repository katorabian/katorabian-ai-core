package com.katorabian.prompt

data class PromptConfig(
    private val base: List<String>,
    private val behavior: List<String>,
    private val constraints: List<String>,
    private val taskHints: List<String>,
) {

    fun render(): String =
        buildString {
            appendSection("Base rules", base)
            appendSection("Behavior", behavior)
            appendSection("Constraints", constraints)
            appendSection("Task", taskHints)
        }.trim()

    private fun StringBuilder.appendSection(
        title: String,
        lines: List<String>
    ) {
        if (lines.isEmpty()) return

        appendLine("## $title")
        lines.forEach { appendLine(it) }
        appendLine()
    }
}