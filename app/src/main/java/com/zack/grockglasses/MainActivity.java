package com.zack.grockglasses;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Set;

public class MainActivity extends Activity {
    private LinearLayout chatBox;
    private TextView status;
    private TextView metaStatus;
    private EditText input;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        requestNeededPermissions();
        buildUi();
        refreshMetaStatus();
        addBubble("Ready. Save your xAI API key, pair your Meta glasses, then tap Start Background Voice.", false);
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(26, 26, 26, 26);
        root.setBackgroundColor(Color.rgb(10,10,10));

        TextView title = tv("👓 Grock Glasses", 32, true);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        metaStatus = tv("Meta Glasses: checking...", 18, true);
        metaStatus.setGravity(Gravity.CENTER);
        metaStatus.setPadding(0, 18, 0, 18);
        root.addView(metaStatus);

        LinearLayout row1 = row();
        row1.addView(btn("Refresh Meta", v -> refreshMetaStatus()));
        row1.addView(btn("Bluetooth Settings", v -> startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS))));
        root.addView(row1);

        status = tv("Voice service stopped", 18, true);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 14, 0, 14);
        root.addView(status);

        LinearLayout row2 = row();
        row2.addView(btn("Start Background Voice", v -> startVoiceService()));
        row2.addView(btn("Stop Voice", v -> stopVoiceService()));
        root.addView(row2);

        LinearLayout row3 = row();
        row3.addView(btn("Save API Key", v -> saveApiKeyDialog()));
        row3.addView(btn("Clear Memory", v -> { Prefs.clearMemory(this); chatBox.removeAllViews(); addBubble("Memory cleared.", false); }));
        root.addView(row3);

        ScrollView scroll = new ScrollView(this);
        chatBox = new LinearLayout(this);
        chatBox.setOrientation(LinearLayout.VERTICAL);
        chatBox.setPadding(10,10,10,10);
        scroll.addView(chatBox);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        input = new EditText(this);
        input.setHint("Type message...");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.GRAY);
        input.setSingleLine(false);
        input.setMinLines(1);
        input.setMaxLines(3);
        root.addView(input);

        root.addView(btn("Send Typed Message", v -> sendTyped()));
        setContentView(root);
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setGravity(Gravity.CENTER);
        l.setOrientation(LinearLayout.HORIZONTAL);
        return l;
    }

    private TextView tv(String s, int sp, boolean white) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(white ? Color.WHITE : Color.LTGRAY);
        return t;
    }

    private Button btn(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setAllCaps(false);
        b.setOnClickListener(l);
        return b;
    }

    private void addBubble(String text, boolean user) {
        runOnUiThread(() -> {
            TextView bubble = tv(text, 16, true);
            bubble.setPadding(18, 14, 18, 14);
            bubble.setBackgroundColor(user ? Color.rgb(0,122,255) : Color.rgb(55,55,55));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.setMargins(0, 8, 0, 8);
            lp.gravity = user ? Gravity.RIGHT : Gravity.LEFT;
            bubble.setLayoutParams(lp);
            chatBox.addView(bubble);
        });
    }

    private void sendTyped() {
        String text = input.getText().toString().trim();
        if (text.isEmpty()) return;
        input.setText("");
        addBubble(text, true);
        status.setText("Thinking...");
        GrokClient.ask(this, text, reply -> {
            status.setText("Ready");
            addBubble(reply, false);
            VoiceWakeService.speakStatic(this, reply);
        });
    }

    private void saveApiKeyDialog() {
        final EditText e = new EditText(this);
        e.setText(Prefs.getApiKey(this));
        e.setSingleLine(true);
        new android.app.AlertDialog.Builder(this)
                .setTitle("xAI API Key")
                .setMessage("Your key is saved only on this phone.")
                .setView(e)
                .setPositiveButton("Save", (d,w) -> { Prefs.setApiKey(this, e.getText().toString()); addBubble("API key saved.", false); })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startVoiceService() {
        requestNeededPermissions();
        Intent i = new Intent(this, VoiceWakeService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i); else startService(i);
        status.setText("Voice service running. Say: Hey Grok");
    }

    private void stopVoiceService() {
        stopService(new Intent(this, VoiceWakeService.class));
        status.setText("Voice service stopped");
    }

    private void requestNeededPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            java.util.ArrayList<String> p = new java.util.ArrayList<>();
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) p.add(Manifest.permission.RECORD_AUDIO);
            if (Build.VERSION.SDK_INT >= 31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) p.add(Manifest.permission.BLUETOOTH_CONNECT);
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) p.add(Manifest.permission.POST_NOTIFICATIONS);
            if (!p.isEmpty()) requestPermissions(p.toArray(new String[0]), 10);
        }
    }

    private void refreshMetaStatus() {
        try {
            boolean found = false;
            String name = "";
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                if (Build.VERSION.SDK_INT < 31 || checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    Set<BluetoothDevice> devices = adapter.getBondedDevices();
                    for (BluetoothDevice d : devices) {
                        String n = d.getName() == null ? "" : d.getName();
                        String low = n.toLowerCase();
                        if (low.contains("meta") || low.contains("ray-ban") || low.contains("rayban")) { found = true; name = n; break; }
                    }
                }
            }
            AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
            boolean headset = adapter != null && (adapter.getProfileConnectionState(BluetoothProfile.HEADSET) == BluetoothProfile.STATE_CONNECTED || adapter.getProfileConnectionState(BluetoothProfile.A2DP) == BluetoothProfile.STATE_CONNECTED);
            String route = am != null && (am.isBluetoothScoOn() || am.isBluetoothA2dpOn()) ? "Bluetooth audio active" : "Bluetooth audio not active";
            metaStatus.setText(found ? "Meta Glasses: paired - " + name + " • " + route : "Meta Glasses: not found in paired Bluetooth devices" + (headset ? " • headset connected" : ""));
            metaStatus.setTextColor(found ? Color.rgb(48,209,88) : Color.rgb(255,69,58));
        } catch (Exception e) {
            metaStatus.setText("Meta status unavailable: " + e.getMessage());
            metaStatus.setTextColor(Color.YELLOW);
        }
    }
}
