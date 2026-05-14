package com.zack.grockglasses;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GrokClient {
    public interface Callback { void onResult(String text); }

    public static void ask(Context context, String prompt, Callback callback) {
        new Thread(() -> {
            String key = Prefs.getApiKey(context);
            if (key.isEmpty()) { callback.onResult("Please open Settings and save your xAI API key first."); return; }
            try {
                Prefs.addMemory(context, "user", prompt);
                JSONArray memory = Prefs.getMemory(context);
                JSONArray input = new JSONArray();
                JSONObject system = new JSONObject();
                system.put("role", "system");
                system.put("content", "You are Grock Glasses, Zack's hands-free assistant. Be direct, accurate, and helpful. Use web_search for current facts, prices, schedules, and anything that may have changed recently. Remember user-taught information from prior messages when relevant.");
                input.put(system);
                for (int i = 0; i < memory.length(); i++) input.put(memory.getJSONObject(i));
                JSONArray tools = new JSONArray();
                JSONObject web = new JSONObject();
                web.put("type", "web_search");
                tools.put(web);
                JSONObject body = new JSONObject();
                body.put("model", "grok-4.3");
                body.put("input", input);
                body.put("tools", tools);
                URL url = new URL("https://api.x.ai/v1/responses");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + key);
                conn.setDoOutput(true);
                byte[] out = body.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) { os.write(out); }
                int code = conn.getResponseCode();
                InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
                String raw = readAll(is);
                if (code < 200 || code >= 300) { callback.onResult("Grok API error " + code + ": " + raw); return; }
                JSONObject data = new JSONObject(raw);
                String reply = data.optString("output_text", "");
                if (reply.isEmpty()) reply = parseOutputText(data);
                if (reply.isEmpty()) reply = "I received a response, but could not read the answer text.";
                Prefs.addMemory(context, "assistant", reply);
                callback.onResult(reply);
            } catch (Exception e) {
                callback.onResult("Connection error: " + e.getMessage());
            }
        }).start();
    }

    private static String readAll(InputStream is) throws Exception {
        if (is == null) return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    private static String parseOutputText(JSONObject data) {
        try {
            StringBuilder sb = new StringBuilder();
            JSONArray output = data.optJSONArray("output");
            if (output == null) return "";
            for (int i = 0; i < output.length(); i++) {
                JSONObject item = output.optJSONObject(i);
                if (item == null) continue;
                JSONArray content = item.optJSONArray("content");
                if (content == null) continue;
                for (int j = 0; j < content.length(); j++) {
                    JSONObject c = content.optJSONObject(j);
                    if (c != null) sb.append(c.optString("text", ""));
                }
            }
            return sb.toString();
        } catch (Exception e) { return ""; }
    }
}
