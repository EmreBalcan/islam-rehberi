package app.kuranokuma.mobile;

import android.app.*;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.*;
import android.media.RingtoneManager;
import android.media.AudioAttributes;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.Calendar;

public class PrayerNotificationService extends Service {
 private static final String PREFS="prayer_notification_state";
 private final Handler handler=new Handler(Looper.getMainLooper()); private String mode,nextName,nextTime,times; private long nextAt; private String lastAlertKey="";
 private final Runnable tick=new Runnable(){public void run(){show();maybeAlertPrayer();handler.postDelayed(this,15000);}};
 private Uri prayerSound(){return Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.prayer_notification);}
 @Override public void onCreate(){super.onCreate();if(Build.VERSION.SDK_INT>=26){NotificationManager nm=getSystemService(NotificationManager.class);NotificationChannel c=new NotificationChannel("prayer_times","Namaz vakitleri",NotificationManager.IMPORTANCE_LOW);c.setDescription("Sıradaki namaz vaktine kalan süre");nm.createNotificationChannel(c);NotificationChannel alert=new NotificationChannel("prayer_alerts_v2","Namaz vakti sesi",NotificationManager.IMPORTANCE_HIGH);alert.setDescription("Öğle, ikindi, akşam ve yatsı vakitlerinde uygulamaya özel sesli uyarı");alert.enableVibration(true);AudioAttributes audio=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();alert.setSound(prayerSound(),audio);nm.createNotificationChannel(alert);}}
 public static void start(Context context,String mode,String nextName,String nextTime,long nextAt,String times){
  context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putBoolean("enabled",true).putString("mode",mode).putString("nextName",nextName).putString("nextTime",nextTime).putLong("nextAt",nextAt).putString("times",times).apply();
  startSaved(context);
 }
 public static void startSaved(Context context){
  SharedPreferences p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);if(!p.getBoolean("enabled",false)||p.getString("times","").isEmpty())return;
  Intent i=new Intent(context,PrayerNotificationService.class).putExtra("restore",true);
  try{if(Build.VERSION.SDK_INT>=26)context.startForegroundService(i);else context.startService(i);}catch(Exception ignored){}
 }
 public static void stopAndClear(Context context){context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putBoolean("enabled",false).apply();context.stopService(new Intent(context,PrayerNotificationService.class));}
 private boolean restore(Intent i){
  SharedPreferences p=getSharedPreferences(PREFS,MODE_PRIVATE);if(!p.getBoolean("enabled",false))return false;
  mode=p.getString("mode","next");nextName=p.getString("nextName","Vakit");nextTime=p.getString("nextTime","");nextAt=p.getLong("nextAt",System.currentTimeMillis());times=p.getString("times","");lastAlertKey=p.getString("lastAlertKey","");return times!=null&&!times.isEmpty();
 }
 @Override public int onStartCommand(Intent i,int flags,int id){if(!restore(i)){stopSelf();return START_NOT_STICKY;}handler.removeCallbacks(tick);show();maybeAlertPrayer();handler.postDelayed(tick,15000);return START_REDELIVER_INTENT;}
 private void maybeAlertPrayer(){if(times==null)return;Calendar now=Calendar.getInstance();String hhmm=String.format(java.util.Locale.US,"%02d:%02d",now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE));String day=String.format(java.util.Locale.US,"%04d-%02d-%02d",now.get(Calendar.YEAR),now.get(Calendar.MONTH)+1,now.get(Calendar.DAY_OF_MONTH));for(String row:times.split(" · ")){int k=row.lastIndexOf(' ');if(k<1)continue;String name=row.substring(0,k),time=row.substring(k+1);if(!time.equals(hhmm)||!(name.equals("Öğle")||name.equals("İkindi")||name.equals("Akşam")||name.equals("Yatsı")))continue;String key=day+"-"+name;if(key.equals(lastAlertKey))return;lastAlertKey=key;getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("lastAlertKey",key).apply();Intent launch=getPackageManager().getLaunchIntentForPackage(getPackageName());PendingIntent pi=PendingIntent.getActivity(this,42,launch,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);NotificationCompat.Builder b=new NotificationCompat.Builder(this,"prayer_alerts_v2").setSmallIcon(R.mipmap.ic_launcher).setContentTitle(name+" vakti girdi").setContentText("Namaz vaktiniz geldi.").setPriority(NotificationCompat.PRIORITY_HIGH).setCategory(NotificationCompat.CATEGORY_ALARM).setAutoCancel(true).setSound(prayerSound()).setContentIntent(pi);try{NotificationManagerCompat.from(this).notify(42,b.build());}catch(SecurityException ignored){}return;}}
 private void refreshNext(){if(times==null||times.isEmpty()||nextAt>System.currentTimeMillis())return;String[] rows=times.split(" · ");long best=Long.MAX_VALUE;for(String row:rows){int k=row.lastIndexOf(' ');if(k<1)continue;String name=row.substring(0,k),time=row.substring(k+1);String[] hm=time.split(":");if(hm.length!=2)continue;try{Calendar c=Calendar.getInstance();c.set(Calendar.HOUR_OF_DAY,Integer.parseInt(hm[0]));c.set(Calendar.MINUTE,Integer.parseInt(hm[1]));c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);if(c.getTimeInMillis()<=System.currentTimeMillis())c.add(Calendar.DAY_OF_MONTH,1);if(c.getTimeInMillis()<best){best=c.getTimeInMillis();nextName=name;nextTime=time;}}catch(Exception ignored){}}if(best<Long.MAX_VALUE){nextAt=best;getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString("nextName",nextName).putString("nextTime",nextTime).putLong("nextAt",nextAt).apply();}}
 private void show(){refreshNext();long min=Math.max(0,(long)Math.ceil((nextAt-System.currentTimeMillis())/60000d));String duration=min>=60?(min/60)+" saat "+(min%60)+" dk":min+" dk";String title=nextName+" vaktine "+duration;String text="all".equals(mode)?times:nextName+" · "+nextTime;Intent launch=getPackageManager().getLaunchIntentForPackage(getPackageName());PendingIntent pi=PendingIntent.getActivity(this,0,launch,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);NotificationCompat.Builder b=new NotificationCompat.Builder(this,"prayer_times").setSmallIcon(R.mipmap.ic_launcher).setContentTitle(title).setContentText(text).setStyle(new NotificationCompat.BigTextStyle().bigText(text)).setOngoing(true).setOnlyAlertOnce(true).setContentIntent(pi).setCategory(NotificationCompat.CATEGORY_REMINDER);startForeground(41,b.build());}
 @Override public void onDestroy(){handler.removeCallbacks(tick);super.onDestroy();}
 @Override public android.os.IBinder onBind(Intent i){return null;}
}


