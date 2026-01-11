package com.katorabian.ollama

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object OllamaClient {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun chat(prompt: String): String {
        val response = client.post("http://localhost:11434/api/generate") {
            contentType(ContentType.Application.Json)
            setBody(
                OllamaRequest(
                    model = "llama3.2:3b",
                    prompt = prompt,
                    stream = false
                )
            )
        }.body<OllamaResponse>()

        return response.response
    }

    suspend fun chatStream(
        prompt: String,
        onToken: (String) -> Unit
    ) {
        val channel = client.post("http://localhost:11434/api/generate") {
            contentType(ContentType.Application.Json)
            setBody(
                OllamaRequest(
                    model = "llama3.2:3b",
                    prompt = prompt,
                    stream = true
                )
            )
        }.bodyAsChannel()

        val json = Json { ignoreUnknownKeys = true }

        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line(DEFAULT_BUFFER_SIZE)?: continue
            val chunk = json.decodeFromString<OllamaResponse>(line)

            onToken(chunk.response)

            if (chunk.done) break
        }
    }
}