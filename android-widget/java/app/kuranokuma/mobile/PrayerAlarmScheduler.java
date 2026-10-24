package app.kuranokuma.mobile;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;

public final class PrayerAlarmScheduler {
 private static final String[] ALERT_NAMES={"Öğle","İkindi","Akşam","Yatsı"};
 private PrayerAlarmScheduler(){}

 public static void schedule(Context context,String times){
  if(times==null||times.isEmpty())return;
  context.getSharedPreferences("prayer_notification_state",Context.MODE_PRIVATE).edit().putString("times",times).apply();
  for(String name:ALERT_NAMES){String time=findTime(times,name);if(time!=null)scheduleOne(context,name,time);}
 }

 public static void rescheduleSaved(Context context){
  android.content.SharedPreferences p=context.getSharedPreferences("prayer_notification_state",Context.MODE_PRIVATE);
  if(p.getBoolean("enabled",false))schedule(context,p.getString("times",""));
 }

 public static void cancelAll(Context context){
  AlarmManager manager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
  for(String name:ALERT_NAMES)manager.cancel(pending(context,name));
 }

 static void scheduleNext(Context context,String name,String time){scheduleOne(context,name,time);}

 private static void scheduleOne(Context context,String name,String time){
  String[] hm=time.split(":");if(hm.length!=2)return;
  try{
   Calendar at=Calendar.getInstance();at.set(Calendar.HOUR_OF_DAY,Integer.parseInt(hm[0]));at.set(Calendar.MINUTE,Integer.parseInt(hm[1]));at.set(Calendar.SECOND,0);at.set(Calendar.MILLISECOND,0);
   if(at.getTimeInMillis()<=System.currentTimeMillis())at.add(Calendar.DAY_OF_MONTH,1);
   AlarmManager manager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);PendingIntent pi=pending(context,name);
   if(Build.VERSION.SDK_INT>=31){
    if(manager.canScheduleExactAlarms())manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at.getTimeInMillis(),pi);
    else manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at.getTimeInMillis(),pi);
   }else if(Build.VERSION.SDK_INT>=23)manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at.getTimeInMillis(),pi);
   else manager.setExact(AlarmManager.RTC_WAKEUP,at.getTimeInMillis(),pi);
  }catch(Exception ignored){}
 }

 private static PendingIntent pending(Context context,String name){
  Intent i=new Intent(context,PrayerAlarmReceiver.class).setAction("app.kuranokuma.mobile.PRAYER_"+name).putExtra("name",name);
  return PendingIntent.getBroadcast(context,1000+Math.abs(name.hashCode()%8000),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
 }

 static String findTime(String times,String wanted){for(String row:times.split(" · ")){int k=row.lastIndexOf(' ');if(k>0&&wanted.equals(row.substring(0,k)))return row.substring(k+1);}return null;}
}
