package com.al.comparex.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

public class LangPrefs {

    private static final String PREFS_NAME    = "comparex_lang_prefs";
    private static final String KEY_LANG      = "selected_lang";
    private static final String KEY_HAS_CHOSEN = "has_chosen_lang";

    public static final String LANG_ID = "id"; // Bahasa Indonesia
    public static final String LANG_EN = "en"; // English

    /** Simpan pilihan bahasa dan tandai bahwa user sudah pernah memilih. */
    public static void saveLang(Context ctx, String lang) {
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LANG, lang)
                .putBoolean(KEY_HAS_CHOSEN, true)
                .apply();
    }

    /** True jika user belum pernah memilih bahasa (first launch). */
    public static boolean hasChosenLanguage(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_HAS_CHOSEN, false);
    }

    /** Ambil pilihan bahasa, default Indonesia */
    public static String getLang(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANG, LANG_ID);
    }

    public static boolean isEnglish(Context ctx) {
        return LANG_EN.equals(getLang(ctx));
    }

    /**
     * Terapkan locale ke context — dipanggil di attachBaseContext setiap Activity.
     */
    public static Context applyLocale(Context ctx) {
        String lang = getLang(ctx);
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration(ctx.getResources().getConfiguration());
        config.setLocale(locale);
        return ctx.createConfigurationContext(config);
    }
}
