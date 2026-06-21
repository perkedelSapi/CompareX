package com.al.comparex.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.al.comparex.data.model.SpekUser;

public class SpekPrefs {

    private static final String PREFS_NAME    = "comparex_spek_prefs";
    private static final String KEY_CPU_NAME  = "cpu_name";
    private static final String KEY_CPU_SCORE = "cpu_score";
    private static final String KEY_GPU_NAME  = "gpu_name";
    private static final String KEY_GPU_SCORE = "gpu_score";
    private static final String KEY_VRAM_GB   = "vram_gb";   // ← baru
    private static final String KEY_RAM_GB    = "ram_gb";
    private static final String KEY_IS_SET    = "is_spek_set";

    public static boolean isSpekSet(Context ctx) {
        return getPrefs(ctx).getBoolean(KEY_IS_SET, false);
    }

    public static void saveSpek(Context ctx, SpekUser spek) {
        getPrefs(ctx).edit()
                .putString(KEY_CPU_NAME,  spek.getCpuName())
                .putInt(KEY_CPU_SCORE,    spek.getCpuScore())
                .putString(KEY_GPU_NAME,  spek.getGpuName())
                .putInt(KEY_GPU_SCORE,    spek.getGpuScore())
                .putInt(KEY_VRAM_GB,      spek.getVramGb())
                .putInt(KEY_RAM_GB,       spek.getRamGb())
                .putBoolean(KEY_IS_SET,   true)
                .apply();
    }

    public static SpekUser loadSpek(Context ctx) {
        SharedPreferences prefs = getPrefs(ctx);
        if (!prefs.getBoolean(KEY_IS_SET, false)) return null;

        String gpuName  = prefs.getString(KEY_GPU_NAME, "");
        int    gpuScore = prefs.getInt(KEY_GPU_SCORE, 0);

        // Backward compat: jika KEY_VRAM_GB belum tersimpan, estimasi dari score
        int vramGb = prefs.getInt(KEY_VRAM_GB, -1);
        if (vramGb < 0) vramGb = SpekUser.estimateVramFromScore(gpuScore);

        return new SpekUser(
                prefs.getString(KEY_CPU_NAME, ""),
                prefs.getInt(KEY_CPU_SCORE, 0),
                gpuName, gpuScore, vramGb,
                prefs.getInt(KEY_RAM_GB, 4)
        );
    }

    public static void clearSpek(Context ctx) {
        getPrefs(ctx).edit().clear().apply();
    }

    private static SharedPreferences getPrefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
