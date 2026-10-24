package app.kuranokuma.mobile;

import android.app.*;
import android.content.*;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PrayerAlarmReceiver extends BroadcastReceiver {
 public static final String CHANNEL="prayer_alerts_custom_v3";
 private static Uri sound(Context c){return Uri.parse("android.resource://"+c.getPackageName()+"/"+R.raw.ezan_notification);}
 public static void ensureChannel(Context c){if(Build.VERSION.SDK_INT<26)return;NotificationManager nm=c.getSystemService(NotificationManager.class);NotificationChannel old=nm.getNotificationChannel(CHANNEL);if(old!=null)return;NotificationChannel ch=new NotificationChannel(CHANNEL,"Ezan vakti bildirimleri",NotificationManager.IMPORTANCE_HIGH);ch.setDescription("Öğle, ikindi, akşam ve yatsı vakitlerinde özel sesli bildirim");ch.enableVibration(true);ch.setBypassDnd(false);ch.setSound(sound(c),new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());nm.createNotificationChannel(ch);}
 @Override public void onReceive(Context context,Intent intent){
  android.content.SharedPreferences p=context.getSharedPreferences("prayer_notification_state",Context.MODE_PRIVATE);if(!p.getBoolean("enabled",false))return;
  String name=intent.getStringExtra("name");if(name==null)name="Namaz";String key=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date())+"-"+name;
  if(key.equals(p.getString("lastAlertKey",""))){reschedule(context,name,p);return;}p.edit().putString("lastAlertKey",key).apply();ensureChannel(context);
  Intent launch=context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());PendingIntent open=PendingIntent.getActivity(context,42,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  NotificationCompat.Builder b=new NotificationCompat.Builder(context,CHANNEL).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(name+" vakti girdi").setContentText("Namaz vaktiniz geldi.").setPriority(NotificationCompat.PRIORITY_MAX).setCategory(NotificationCompat.CATEGORY_ALARM).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setAutoCancel(true).setSound(sound(context)).setContentIntent(open);
  try{NotificationManagerCompat.from(context).notify(4200+Math.abs(name.hashCode()%500),b.build());}catch(SecurityException ignored){}
  reschedule(context,name,p);
 }
 private void reschedule(Context c,String name,android.content.SharedPreferences p){String time=PrayerAlarmScheduler.findTime(p.getString("times",""),name);if(time!=null)PrayerAlarmScheduler.scheduleNext(c,name,time);PrayerNotificationService.startSaved(c);}
}
