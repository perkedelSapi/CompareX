package com.al.comparex.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response dari DeepSeek Chat API (OpenAI-compatible format).
 *
 * JSON yang diterima:
 * {
 *   "choices": [{
 *     "message": { "role": "assistant", "content": "..." },
 *     "finish_reason": "stop"
 *   }],
 *   "error": { "message": "...", "code": "..." }
 * }
 *
 * Nama class tetap GeminiResponse agar tidak perlu refactor semua caller.
 */
public class GeminiResponse {

    @SerializedName("choices")
    private List<Choice> choices;

    @SerializedName("error")
    private GeminiError error;

    public List<Choice> getCandidates() { return choices; }   // alias untuk backward compat
    public GeminiError  getError()      { return error;   }

    public boolean isSuccess() {
        return error == null && choices != null && !choices.isEmpty();
    }

    /** Ambil teks balasan dari choice pertama. */
    public String getFirstText() {
        if (!isSuccess()) return "";
        Choice first = choices.get(0);
        if (first.getMessage() == null) return "";
        String content = first.getMessage().getContent();
        return content != null ? content : "";
    }

    // ── Nested: Choice ───────────────────────────────────────────────────────
    public static class Choice {
        @SerializedName("message")
        private ChoiceMessage message;

        @SerializedName("finish_reason")
        private String finishReason;

        /** Alias agar ViewModel lama yang pakai getContent() tetap compile. */
        public CandidateContent getContent() {
            return new CandidateContent(message);
        }

        public ChoiceMessage getMessage()     { return message;      }
        public String        getFinishReason(){ return finishReason; }
    }

    // ── Nested: ChoiceMessage ────────────────────────────────────────────────
    public static class ChoiceMessage {
        @SerializedName("role")
        private String role;

        @SerializedName("content")
        private String content;

        public String getRole()    { return role;    }
        public String getContent() { return content; }
    }

    // ── Nested: CandidateContent (backward-compat wrapper) ───────────────────
    public static class CandidateContent {
        private final ChoiceMessage message;

        public CandidateContent(ChoiceMessage message) {
            this.message = message;
        }

        public List<GeminiRequest.Part> getParts() {
            if (message == null || message.getContent() == null) return null;
            return java.util.Collections.singletonList(
                    new GeminiRequest.Part(message.getContent()));
        }
    }

    // ── Nested: GeminiError ──────────────────────────────────────────────────
    public static class GeminiError {
        @SerializedName("code")
        private Object code;     // bisa int atau string di DeepSeek

        @SerializedName("message")
        private String message;

        @SerializedName("status")
        private String status;

        public String getMessage() { return message; }
        public String getStatus()  { return status;  }
    }
}
