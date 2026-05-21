package com.drash.familysafetynet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

public class AutoStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            Intent serviceIntent = new Intent(context, LocationTrackingService.class);
            ContextCompat.startForegroundService(context, serviceIntent);
        }
    }
}
