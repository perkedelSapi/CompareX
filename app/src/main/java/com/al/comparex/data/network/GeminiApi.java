package com.al.comparex.data.network;

import com.al.comparex.data.model.GeminiRequest;
import com.al.comparex.data.model.GeminiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Retrofit interface untuk DeepSeek Chat API (OpenAI-compatible).
 * Base URL: https://api.deepseek.com/
 *
 * Daftar API key di: https://platform.deepseek.com/api_keys
 * Model tersedia:
 *   - DeepSeek-V4-Flash  → cepat, default thinking mode (aktif sekarang)
 *   - DeepSeek-V4-Pro    → lebih akurat, ganti AI_MODEL di ApiClient jika perlu
 *
 * Nama interface tetap GeminiApi agar tidak perlu refactor ViewModel.
 */
public interface GeminiApi {

    /**
     * Kirim chat completion request ke DeepSeek.
     *
     * @param authorization  "Bearer sk-xxxxxxxx"
     * @param body           Request berisi model, messages, dll.
     */
    @POST("v1/chat/completions")
    Call<GeminiResponse> generate(
            @Header("Authorization") String authorization,
            @Body GeminiRequest body
    );
}
