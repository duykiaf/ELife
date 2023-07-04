package t3h.android.elife.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import t3h.android.elife.models.Audio;
import t3h.android.elife.services.MainService;

public class MainReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentToService = new Intent(context, MainService.class);

        Bundle getDataFromIntent = intent.getExtras();
        int audioAction = getDataFromIntent.getInt("AudioAction");
        Audio audioInfo = (Audio) getDataFromIntent.get("AudioInfo");

        intentToService.putExtra("AudioActionToService", audioAction);
        intentToService.putExtra("AudioInfoToService", audioInfo);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentToService);
        } else {
            context.startService(intentToService);
        }
    }
}
