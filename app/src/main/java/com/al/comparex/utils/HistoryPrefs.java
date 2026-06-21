package com.al.comparex.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.al.comparex.data.model.HistoryEntry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Persists recent game history (max 10 entries) using SharedPreferences + JSON.
 *
 * Stores per entry: gameId, gameName, backgroundImage, metacritic,
 *                   released, compatPercent, compatSetting, checkedAt (epoch ms)
 */
public class HistoryPrefs {

    private static final String PREFS_NAME  = "comparex_history";
    private static final String KEY_HISTORY = "history_json";
    private static final int    MAX_ENTRIES = 10;

    /** Prepend a new entry (or bump existing to top if same gameId). */
    public static void add(Context ctx, HistoryEntry entry) {
        List<HistoryEntry> list = load(ctx);

        // Remove existing entry with same gameId (deduplicate)
        list.removeIf(e -> e.getGameId() == entry.getGameId());

        // Insert at front
        list.add(0, entry);

        // Trim to max
        if (list.size() > MAX_ENTRIES) list = list.subList(0, MAX_ENTRIES);

        save(ctx, list);
    }

    public static List<HistoryEntry> load(Context ctx) {
        String json = prefs(ctx).getString(KEY_HISTORY, "[]");
        List<HistoryEntry> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                HistoryEntry e = new HistoryEntry(
                        o.getInt("gameId"),
                        o.optString("gameName", ""),
                        o.optString("backgroundImage", null),
                        o.has("metacritic") && !o.isNull("metacritic")
                                ? o.getInt("metacritic") : null,
                        o.optString("released", null),
                        o.optInt("compatPercent", 0),
                        o.optString("compatSetting", ""),
                        o.optLong("checkedAt", System.currentTimeMillis())
                );
                list.add(e);
            }
        } catch (Exception ignored) {}
        return list;
    }

    public static void clear(Context ctx) {
        prefs(ctx).edit().remove(KEY_HISTORY).apply();
    }

    // ── private ───────────────────────────────────────────────────────────────

    private static void save(Context ctx, List<HistoryEntry> list) {
        try {
            JSONArray arr = new JSONArray();
            for (HistoryEntry e : list) {
                JSONObject o = new JSONObject();
                o.put("gameId",          e.getGameId());
                o.put("gameName",        e.getGameName());
                o.put("backgroundImage", e.getBackgroundImage());
                if (e.getMetacritic() != null) o.put("metacritic", e.getMetacritic());
                else o.put("metacritic", JSONObject.NULL);
                o.put("released",        e.getReleased() != null ? e.getReleased() : "");
                o.put("compatPercent",   e.getCompatPercent());
                o.put("compatSetting",   e.getCompatSetting());
                o.put("checkedAt",       e.getCheckedAt());
                arr.put(o);
            }
            prefs(ctx).edit().putString(KEY_HISTORY, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
