package com.zack.grockglasses;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class VoiceWakeService extends Service {
    private static final String CHANNEL_ID = "grock_glasses_voice";
    private TextToSpeech tts;

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) tts.setLanguage(Locale.US);
        });
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(101, buildNotification("Voice service is running"));
        return START_STICKY;
    }

    @Override public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }

    public static void speakStatic(Context c, String text) {
        TextToSpeech localTts = new TextToSpeech(c.getApplicationContext(), status -> {});
        new android.os.Handler(c.getMainLooper()).postDelayed(() -> {
            try {
                localTts.setLanguage(Locale.US);
                localTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "typed_reply");
            } catch(Exception ignored) {}
        }, 500);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Grock Glasses Voice", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Shows when Grock Glasses voice features are active.");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private Notification buildNotification(String text) {
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, open, Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, CHANNEL_ID) : new Notification.Builder(this);
        return b.setContentTitle("Grock Glasses")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();
    }
}
