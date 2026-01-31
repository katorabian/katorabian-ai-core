package com.katorabian.application

import com.katorabian.llm.gatekeeper.GatekeeperDescriptor
import com.katorabian.service.model.ModelDescriptor
import com.katorabian.service.model.ModelRole

object ModelPresets {

    val LocalChat = ModelDescriptor(
        id = "GPT-OSS-20B",
        role = ModelRole.CHAT,
        modelPath = "F:/llm/models/Mistral-Nemo-2407-12B-Thinking-Claude-Gemini-GPT5.2-Uncensored-HERETIC.Q8_0.gguf"
    )

    val Gatekeeper = GatekeeperDescriptor(
        id = "qwen3-4b-gatekeeper",
        modelPath = "F:/llm/models/Qwen3-Gatekeeper-4B-f16-Q8_0.gguf"
    )
}