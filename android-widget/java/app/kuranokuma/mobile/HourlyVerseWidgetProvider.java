package app.kuranokuma.mobile;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.View;
import android.util.TypedValue;
import android.widget.RemoteViews;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import org.json.JSONArray;
import org.json.JSONObject;

public class HourlyVerseWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_HOURLY = "app.kuranokuma.mobile.HOURLY_VERSE";
    private static final String[] SURAH_NAMES = {
        "Fâtiha","Bakara","Âl-i İmrân","Nisâ","Mâide","En‘âm","A‘râf","Enfâl","Tevbe","Yûnus","Hûd","Yûsuf","Ra‘d","İbrâhîm","Hicr","Nahl","İsrâ","Kehf","Meryem","Tâhâ","Enbiyâ","Hac","Mü’minûn","Nûr","Furkân","Şuarâ","Neml","Kasas","Ankebût","Rûm","Lokmân","Secde","Ahzâb","Sebe’","Fâtır","Yâsîn","Sâffât","Sâd","Zümer","Mü’min","Fussilet","Şûrâ","Zuhruf","Duhân","Câsiye","Ahkâf","Muhammed","Fetih","Hucurât","Kâf","Zâriyât","Tûr","Necm","Kamer","Rahmân","Vâkıa","Hadîd","Mücâdele","Haşr","Mümtehine","Saf","Cum‘a","Münâfikûn","Tegâbün","Talâk","Tahrîm","Mülk","Kalem","Hâkka","Meâric","Nûh","Cin","Müzzemmil","Müddessir","Kıyâme","İnsân","Mürselât","Nebe’","Nâziât","Abese","Tekvîr","İnfitâr","Mutaffifîn","İnşikâk","Bürûc","Târık","A‘lâ","Gâşiye","Fecr","Beled","Şems","Leyl","Duhâ","İnşirâh","Tîn","Alak","Kadr","Beyyine","Zilzâl","Âdiyât","Kâria","Tekâsür","Asr","Hümeze","Fîl","Kureyş","Mâûn","Kevser","Kâfirûn","Nasr","Tebbet","İhlâs","Felak","Nâs"
    };
    private static final String[] RESCHEDULE_ACTIONS = {
        Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED,
        Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED
    };

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) renderCached(context, manager, id);
        scheduleNext(context);
    }
    @Override public void onEnabled(Context context) { refreshAll(context); scheduleNext(context); }
    @Override public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_HOURLY.equals(action) || AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action) || isRescheduleAction(action)) {
            renderCachedAll(context);
            PendingResult pendingResult = goAsync();
            Context appContext = context.getApplicationContext();
            new Thread(() -> {
                try { if (!fetchAndApply(appContext)) scheduleRetry(appContext); }
                finally { pendingResult.finish(); }
            }, "hourly-verse-widget").start();
            return;
        }
        super.onReceive(context, intent);
    }
    private static boolean isRescheduleAction(String action) {
        if (action == null) return false;
        for (String candidate : RESCHEDULE_ACTIONS) if (candidate.equals(action)) return true;
        return false;
    }
    public static void refreshAll(Context context) {
        renderCachedAll(context);
        Context appContext = context.getApplicationContext();
        new Thread(() -> { if (!fetchAndApply(appContext)) scheduleRetry(appContext); }, "hourly-verse-refresh").start();
    }
    private static void renderCachedAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName name = new ComponentName(context, HourlyVerseWidgetProvider.class);
        for (int id : manager.getAppWidgetIds(name)) renderCached(context, manager, id);
    }
    public static void applyVerse(Context context, String hourKey, int globalNumber,
                                  String reference, String arabic, String turkish) {
        SharedPreferences prefs = context.getSharedPreferences("hourly_widget", Context.MODE_PRIVATE);
        prefs.edit().putString("hourKey", hourKey).putInt("globalNumber", globalNumber)
            .putString("ref", reference).putString("arabic", arabic).putString("turkish", turkish).apply();
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName name = new ComponentName(context, HourlyVerseWidgetProvider.class);
        for (int id : manager.getAppWidgetIds(name)) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.hourly_verse_widget);
            bind(context, views, reference, arabic, turkish,
                prefs.getBoolean("showArabic", true), prefs.getBoolean("showTafsir", false));
            manager.updateAppWidget(id, views);
        }
        scheduleNext(context);
    }
    private static void scheduleNext(Context context) {
        Calendar next = Calendar.getInstance();
        next.add(Calendar.HOUR_OF_DAY, 1); next.set(Calendar.MINUTE, 0); next.set(Calendar.SECOND, 1); next.set(Calendar.MILLISECOND, 0);
        scheduleAt(context, next.getTimeInMillis());
    }
    private static void scheduleRetry(Context context) {
        scheduleAt(context, System.currentTimeMillis() + 10 * 60 * 1000L);
    }
    private static void scheduleAt(Context context, long at) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm == null) return;
        Intent intent = new Intent(context, HourlyVerseWidgetProvider.class).setAction(ACTION_HOURLY);
        PendingIntent pending = PendingIntent.getBroadcast(context, 4107, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (Build.VERSION.SDK_INT >= 31 && alarm.canScheduleExactAlarms()) alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending);
        else if (Build.VERSION.SDK_INT >= 23) alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending);
        else alarm.set(AlarmManager.RTC_WAKEUP, at, pending);
    }
    private static void renderCached(Context context, AppWidgetManager manager, int id) {
        SharedPreferences prefs = context.getSharedPreferences("hourly_widget", Context.MODE_PRIVATE);
        RemoteViews saved = new RemoteViews(context.getPackageName(), R.layout.hourly_verse_widget);
        bind(context, saved, prefs.getString("ref", "Saatlik ayet hazırlanıyor"), prefs.getString("arabic", ""),
            prefs.getString("turkish", "İnternet bağlantısı kurulduğunda ayet gösterilecek."),
            prefs.getBoolean("showArabic", true), prefs.getBoolean("showTafsir", false));
        manager.updateAppWidget(id, saved);
    }
    private static boolean fetchAndApply(Context context) {
        HttpURLConnection connection = null;
        try {
            SharedPreferences prefs = context.getSharedPreferences("hourly_widget", Context.MODE_PRIVATE);
            long hour = System.currentTimeMillis() / 3600000L;
            int number = (int) Math.floorMod(hour * 2654435761L, 6236L) + 1;
            String meal = prefs.getString("mealEdition", "tr.yazir");
            URL url = new URL("https://api.alquran.cloud/v1/ayah/" + number + "/editions/quran-uthmani," + meal);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(12000); connection.setReadTimeout(12000);
            connection.setRequestProperty("Accept", "application/json");
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return false;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder body = new StringBuilder(); String line;
                while ((line = reader.readLine()) != null) body.append(line);
                JSONArray data = new JSONObject(body.toString()).getJSONArray("data");
                JSONObject arabicData = data.getJSONObject(0), turkishData = data.getJSONObject(1);
                JSONObject surah = arabicData.getJSONObject("surah");
                int surahNumber = surah.getInt("number");
                String surahName = surahNumber >= 1 && surahNumber <= SURAH_NAMES.length ? SURAH_NAMES[surahNumber - 1] : "Sûre " + surahNumber;
                String reference = surahName + " Suresi · " + arabicData.getInt("numberInSurah") + ". ayet";
                applyVerse(context, String.valueOf(hour), number, reference, arabicData.getString("text"), turkishData.getString("text"));
                return true;
            }
        } catch (Exception ignored) { return false; }
        finally { if (connection != null) connection.disconnect(); }
    }
    private static void bind(Context context, RemoteViews views, String ref, String arabic, String turkish, boolean showArabic, boolean tafsir) {
        views.setTextViewText(R.id.widget_ref, ref); views.setTextViewText(R.id.widget_arabic, arabic);
        views.setViewVisibility(R.id.widget_arabic, showArabic ? View.VISIBLE : View.GONE);
        float arabicSize = arabic.length() > 220 ? 15f : arabic.length() > 140 ? 17f : arabic.length() > 80 ? 19f : 22f;
        float mealSize = turkish.length() > 420 ? 10f : turkish.length() > 280 ? 11f : turkish.length() > 170 ? 12.5f : 15f;
        views.setTextViewTextSize(R.id.widget_arabic, TypedValue.COMPLEX_UNIT_SP, arabicSize);
        views.setTextViewTextSize(R.id.widget_translation, TypedValue.COMPLEX_UNIT_SP, mealSize);
        views.setInt(R.id.widget_translation, "setMaxLines", 14);
        views.setTextViewText(R.id.widget_translation, turkish + (tafsir ? "\nTefsir ve ayetin tamamı için dokunun." : ""));
        Intent open = new Intent(context, MainActivity.class); open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pending);
    }
}

