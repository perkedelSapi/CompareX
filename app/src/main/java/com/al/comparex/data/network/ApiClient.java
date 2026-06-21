package com.al.comparex.data.network;

import com.al.comparex.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Singleton Retrofit factory untuk semua API CompareX.
 *
 * Ganti key berikut sebelum menjalankan aplikasi:
 *
 *  RAWG_API_KEY   → https://rawg.io/apidocs        (gratis)
 *  AI_API_KEY     → https://platform.deepseek.com/api_keys
 *
 * Model DeepSeek:
 *   deepseek-chat      → V3, cepat dan hemat (default)
 *   deepseek-reasoner  → R1, lebih akurat untuk analisis kompleks
 */
public class ApiClient {

    // ── API Keys ──────────────────────────────────────────────────────────────
    // ── API Keys — dibaca dari BuildConfig (sumber: local.properties) ──────────
    public static final String RAWG_API_KEY = BuildConfig.RAWG_API_KEY;
    public static final String AI_API_KEY   = BuildConfig.AI_API_KEY;

    // Model DeepSeek — deepseek-chat (V3, cepat) atau deepseek-reasoner (R1, lebih akurat)
    public static final String AI_MODEL     = "deepseek-chat";

    // ── Base URLs ─────────────────────────────────────────────────────────────
    public static final String RAWG_BASE_URL        = "https://api.rawg.io/api/";
    public static final String STEAM_UNOFFICIAL_URL = "https://fadel.nasiwebhost.com/";
    public static final String STEAM_STORE_URL      = "https://store.steampowered.com/api/";
    public static final String DEEPSEEK_BASE_URL    = "https://api.deepseek.com/";

    // ── Retrofit instances ────────────────────────────────────────────────────
    private static Retrofit rawgRetrofit;
    private static Retrofit steamUnofficialRetrofit;
    private static Retrofit steamStoreRetrofit;
    private static Retrofit aiRetrofit;

    // Client terpisah: general (RAWG & AI) vs Steam (timeout lebih ketat)
    private static OkHttpClient generalClient;
    private static OkHttpClient steamClient;

    /** Client untuk RAWG dan DeepSeek — timeout standar. Logging body hanya di debug build. */
    private static OkHttpClient getGeneralClient() {
        if (generalClient == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.DEBUG
                    ? HttpLoggingInterceptor.Level.BODY
                    : HttpLoggingInterceptor.Level.NONE);
            generalClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(90, TimeUnit.SECONDS)   // DeepSeek R1 bisa lebih lambat
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
        }
        return generalClient;
    }

    /**
     * Client khusus Steam (unofficial & store search).
     * Timeout lebih ketat (10 detik connect, 12 detik read) agar
     * ViewModel timeout handler bisa bekerja sebelum OkHttp timeout system.
     * Jika Steam lambat/down, koneksi gagal cepat → fallback ke RAWG.
     */
    private static OkHttpClient getSteamClient() {
        if (steamClient == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.DEBUG
                    ? HttpLoggingInterceptor.Level.BASIC
                    : HttpLoggingInterceptor.Level.NONE);
            steamClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(12, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();
        }
        return steamClient;
    }

    public static RawgApi getRawgApi() {
        if (rawgRetrofit == null)
            rawgRetrofit = new Retrofit.Builder()
                    .baseUrl(RAWG_BASE_URL).client(getGeneralClient())
                    .addConverterFactory(GsonConverterFactory.create()).build();
        return rawgRetrofit.create(RawgApi.class);
    }

    public static SteamApi getSteamApi() {
        if (steamUnofficialRetrofit == null)
            steamUnofficialRetrofit = new Retrofit.Builder()
                    .baseUrl(STEAM_UNOFFICIAL_URL).client(getSteamClient())
                    .addConverterFactory(GsonConverterFactory.create()).build();
        return steamUnofficialRetrofit.create(SteamApi.class);
    }

    public static SteamStoreApi getSteamStoreApi() {
        if (steamStoreRetrofit == null)
            steamStoreRetrofit = new Retrofit.Builder()
                    .baseUrl(STEAM_STORE_URL).client(getSteamClient())
                    .addConverterFactory(GsonConverterFactory.create()).build();
        return steamStoreRetrofit.create(SteamStoreApi.class);
    }

    /** Returns DeepSeek AI client (nama tetap getGeminiApi agar ViewModel tidak perlu diubah). */
    public static GeminiApi getGeminiApi() {
        if (aiRetrofit == null)
            aiRetrofit = new Retrofit.Builder()
                    .baseUrl(DEEPSEEK_BASE_URL).client(getGeneralClient())
                    .addConverterFactory(GsonConverterFactory.create()).build();
        return aiRetrofit.create(GeminiApi.class);
    }
}
