package app.kuranokuma.mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PrayerNotificationReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context,Intent intent){PrayerNotificationService.startSaved(context);}
}

