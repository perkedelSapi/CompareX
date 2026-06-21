package com.al.comparex.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;

/**
 * Request body untuk DeepSeek Chat API (OpenAI-compatible format).
 *
 * Endpoint:
 *   POST https://api.deepseek.com/v1/chat/completions
 *   Header: Authorization: Bearer <DEEPSEEK_API_KEY>
 *
 * JSON yang dikirim:
 * {
 *   "model": "DeepSeek-V4-Flash",
 *   "messages": [
 *     { "role": "system", "content": "system prompt..." },
 *     { "role": "user",   "content": "user message..." }
 *   ],
 *   "max_tokens": 1024,
 *   "temperature": 0.7
 * }
 *
 * Nama class tetap GeminiRequest agar tidak perlu refactor semua caller.
 */
public class GeminiRequest {

    @SerializedName("model")
    private String model;

    @SerializedName("messages")
    private List<Message> messages;

    @SerializedName("max_tokens")
    private int maxTokens;

    @SerializedName("temperature")
    private float temperature;

    /**
     * Buat request dengan system prompt + satu user message.
     *
     * @param systemPrompt  Instruksi sistem
     * @param userPrompt    Pesan / pertanyaan pengguna
     */
    public GeminiRequest(String systemPrompt, String userPrompt) {
        this.model       = com.al.comparex.data.network.ApiClient.AI_MODEL;
        this.maxTokens   = 2048;
        this.temperature = 0.7f;
        this.messages    = Arrays.asList(
                new Message("system", systemPrompt),
                new Message("user",   userPrompt)
        );
    }

    // ── Nested: Message ──────────────────────────────────────────────────────
    public static class Message {
        @SerializedName("role")
        private String role;

        @SerializedName("content")
        private String content;

        public Message(String role, String content) {
            this.role    = role;
            this.content = content;
        }

        public String getRole()    { return role;    }
        public String getContent() { return content; }
    }

    // ── Nested: Part (dipertahankan agar GeminiResponse tetap compile) ────────
    public static class Part {
        private String text;
        public Part(String text)  { this.text = text; }
        public String getText()   { return text; }
    }
}
