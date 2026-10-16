package app.kuranokuma.mobile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

@CapacitorPlugin(name="IslamNative", permissions={
 @Permission(alias="location", strings={Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}),
 @Permission(alias="notifications", strings={Manifest.permission.POST_NOTIFICATIONS})
})
public class IslamNativePlugin extends Plugin {
 @PluginMethod public void getLocation(PluginCall call){
  if(getPermissionState("location")!=PermissionState.GRANTED){requestPermissionForAlias("location",call,"locationReady");return;}
  resolveLocation(call);
 }
 @PermissionCallback private void locationReady(PluginCall call){if(getPermissionState("location")==PermissionState.GRANTED)resolveLocation(call);else call.reject("Konum izni verilmedi");}
 private void resolveLocation(PluginCall call){
  LocationManager lm=(LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
  try{
   if(Build.VERSION.SDK_INT>=30){lm.getCurrentLocation(LocationManager.GPS_PROVIDER,null,getContext().getMainExecutor(),l->{if(l==null)fallback(call,lm);else ok(call,l);});}
   else fallback(call,lm);
  }catch(Exception e){fallback(call,lm);}
 }
 private void fallback(PluginCall call,LocationManager lm){try{Location a=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER),b=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);Location l=a!=null?a:b;if(l==null)call.reject("Konum bulunamadı; GPS'i açın");else ok(call,l);}catch(SecurityException e){call.reject("Konum izni gerekli");}}
 private void ok(PluginCall call,Location l){JSObject o=new JSObject();o.put("latitude",l.getLatitude());o.put("longitude",l.getLongitude());o.put("accuracy",l.getAccuracy());call.resolve(o);}
 @PluginMethod public void setPrayerNotification(PluginCall call){
  if(Build.VERSION.SDK_INT>=33&&getPermissionState("notifications")!=PermissionState.GRANTED){requestPermissionForAlias("notifications",call,"notificationReady");return;}
  startNotification(call);
 }
 @PermissionCallback private void notificationReady(PluginCall call){if(getPermissionState("notifications")==PermissionState.GRANTED)startNotification(call);else call.reject("Bildirim izni verilmedi");}
 private void startNotification(PluginCall call){PrayerNotificationService.start(getContext(),call.getString("mode","next"),call.getString("nextName","Vakit"),call.getString("nextTime",""),call.getLong("nextAt",System.currentTimeMillis()),call.getString("times",""));call.resolve();}
 @PluginMethod public void stopPrayerNotification(PluginCall call){PrayerNotificationService.stopAndClear(getContext());call.resolve();}
 @PluginMethod public void setWidgetPreferences(PluginCall call){boolean ar=call.getBoolean("showArabic",true),tf=call.getBoolean("showTafsir",false);String meal=call.getString("mealEdition","tr.yazir");getContext().getSharedPreferences("hourly_widget",Context.MODE_PRIVATE).edit().putBoolean("showArabic",ar).putBoolean("showTafsir",tf).putString("mealEdition",meal).apply();HourlyVerseWidgetProvider.refreshAll(getContext());call.resolve();}
 @PluginMethod public void updateHourlyVerse(PluginCall call){HourlyVerseWidgetProvider.applyVerse(getContext(),call.getString("hourKey",""),call.getInt("globalNumber",1),call.getString("reference","Saatlik ayet"),call.getString("arabic",""),call.getString("turkish",""));call.resolve();}
}

