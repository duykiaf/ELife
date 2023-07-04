package t3h.android.elife.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import t3h.android.elife.R;
import t3h.android.elife.helper.AppConstant;
import t3h.android.elife.helper.MainReceiver;
import t3h.android.elife.models.Audio;

public class MainService extends Service {
    private MediaPlayer mediaPlayer;
    private boolean isPlaying;
    private Audio audio, oldAudio;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("DNV", "onCreate-service");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("DNV", "onBind-service");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("DNV", "onStartCommand-service");
        // get intent form fragment
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            oldAudio = audio;
            Audio getAudio = (Audio) bundle.get("audio");
            if (getAudio != null) {
                audio = getAudio;
                startAudio();
                sendNotification();
            } else {
                // get intent from BroadcastReceiver or fragment when click control audio icon
                int audioAction = intent.getIntExtra("AudioActionToService", 0);
                audio = (Audio) intent.getSerializableExtra("AudioInfoToService");
                handleAudioAction(audioAction);
            }
        }
        return START_NOT_STICKY;
    }

    private void sendNotification() {
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(this, "tag");
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, AppConstant.CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.elife_logo)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2, 3, 4)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setContentTitle("Topic name")
                .setContentText(audio.getTitle());
        if (isPlaying) {
            notificationBuilder.addAction(R.drawable.ic_bookmark, "Bookmark", null) // #0
                    .addAction(R.drawable.ic_skip_previous, "Previous", null) // #1
                    .addAction(R.drawable.ic_pause_circle_outline, "Pause",
                            getPendingIntent(this, AppConstant.ACTION_PAUSE))  // #2
                    .addAction(R.drawable.ic_skip_next, "Next", null)     // #3
                    .addAction(R.drawable.ic_close, "Close", null); // #4
        } else {
            notificationBuilder.addAction(R.drawable.ic_bookmark, "Bookmark", null) // #0
                    .addAction(R.drawable.ic_skip_previous, "Previous", null) // #1
                    .addAction(R.drawable.ic_play_circle_outline, "Play",
                            getPendingIntent(this, AppConstant.ACTION_RESUME))  // #2
                    .addAction(R.drawable.ic_skip_next, "Next", null)     // #3
                    .addAction(R.drawable.ic_close, "Close", null); // #4
        }
        startForeground(AppConstant.NOTIFICATION_ID, notificationBuilder.build());
    }

    private void startAudio() {
//        if (mediaPlayer == null) {
//            mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(audio.getAudioFile()));
//        }
        if (oldAudio != null && oldAudio.equals(audio) && mediaPlayer != null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(oldAudio.getAudioFile()));
        } else {
            pauseAudio();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(audio.getAudioFile()));
        }
        mediaPlayer.start();
        isPlaying = true;
        sendActionToFragment(AppConstant.ACTION_START);
    }

    private PendingIntent getPendingIntent(Context context, int action) {
        Bundle bundle = new Bundle();
        bundle.putInt("AudioAction", action);
        bundle.putSerializable("AudioInfo", audio);
        Intent intent = new Intent(this, MainReceiver.class);
        intent.putExtras(bundle);
        return PendingIntent.getBroadcast(context.getApplicationContext(), action, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private void handleAudioAction(int action) {
        switch (action) {
            case AppConstant.ACTION_PAUSE:
                pauseAudio();
                break;
            case AppConstant.ACTION_RESUME:
                resumeAudio();
                break;
            case AppConstant.ACTION_CLEAR:
                stopSelf();
                sendActionToFragment(AppConstant.ACTION_CLEAR);
                break;
        }
    }

    private void pauseAudio() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            sendNotification();
            sendActionToFragment(AppConstant.ACTION_PAUSE);
        }
    }

    private void resumeAudio() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
            sendNotification();
            sendActionToFragment(AppConstant.ACTION_RESUME);
        }
    }

    private void sendActionToFragment(int action) {
        Intent intent = new Intent("DataFromService");
        Bundle bundle = new Bundle();
        bundle.putSerializable("AudioInfo", audio);
        bundle.putBoolean("AudioStatus", isPlaying);
        bundle.putInt("AudioAction", action);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("DNV", "onUnbind-service");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.e("DNV", "onDestroy-service");
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
