package com.zack.grockglasses;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

public class Prefs {
    private static final String NAME = "grock_glasses_prefs";
    private static final String KEY_API = "xai_api_key";
    private static final String KEY_MEMORY = "memory_json";

    public static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static String getApiKey(Context c) {
        return sp(c).getString(KEY_API, "");
    }

    public static void setApiKey(Context c, String key) {
        sp(c).edit().putString(KEY_API, key == null ? "" : key.trim()).apply();
    }

    public static JSONArray getMemory(Context c) {
        try { return new JSONArray(sp(c).getString(KEY_MEMORY, "[]")); }
        catch (Exception e) { return new JSONArray(); }
    }

    public static void saveMemory(Context c, JSONArray arr) {
        sp(c).edit().putString(KEY_MEMORY, arr.toString()).apply();
    }

    public static void addMemory(Context c, String role, String content) {
        try {
            JSONArray arr = getMemory(c);
            JSONObject o = new JSONObject();
            o.put("role", role);
            o.put("content", content);
            arr.put(o);
            while (arr.length() > 40) arr.remove(0);
            saveMemory(c, arr);
        } catch (Exception ignored) {}
    }

    public static void clearMemory(Context c) { saveMemory(c, new JSONArray()); }
}
