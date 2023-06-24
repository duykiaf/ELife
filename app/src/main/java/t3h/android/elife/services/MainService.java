package t3h.android.elife.services;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LiveData;

import java.util.List;

import t3h.android.elife.R;
import t3h.android.elife.helper.AppConstant;
import t3h.android.elife.helper.AudioController;
import t3h.android.elife.models.Audio;
import t3h.android.elife.repositories.MainRepository;

public class MainService extends Service {
    private AudioController audioController;
    private MainRepository mainRepository;
    private LiveData<List<Audio>> audioList;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("DNV", "onCreate-service");
        mainRepository = new MainRepository();
        audioController = new AudioController(new AudioController.AudioSource() {
            @Override
            public int getSize() {
                return 0;
            }

            @Override
            public Audio getAudioAtIndex(int index) {
                return null;
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("DNV", "onBind-service");
        return new AudioBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("DNV", "onStartCommand-service");
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            audioList = mainRepository.getActiveAudiosListByTopicId((Integer) bundle.get("TopicId"));
        }

        audioController = new AudioController(new AudioController.AudioSource() {
            @Override
            public int getSize() {
                return audioList.getValue() == null ? 0 : audioList.getValue().size();
            }

            @Override
            public Audio getAudioAtIndex(int index) {
                return audioList.getValue() != null ? audioList.getValue().get(index) : null;
            }
        });

        sendNotification();
        return START_NOT_STICKY;
    }

    private void sendNotification() {
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(this, "tag");
        Notification notification = new NotificationCompat.Builder(this, AppConstant.CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.elife_logo)
                .addAction(R.drawable.ic_bookmark, "Bookmark", null) // #0
                .addAction(R.drawable.ic_skip_previous, "Previous", null) // #1
                .addAction(R.drawable.ic_pause_circle_outline, "Pause", null)  // #2
                .addAction(R.drawable.ic_skip_next, "Next", null)     // #3
                .addAction(R.drawable.ic_close, "Close", null) // #4
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2, 3, 4)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setContentTitle("Topic name")
                .setContentText("Audio title")
                .build();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        managerCompat.notify(AppConstant.NOTIFICATION_ID, notification);
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
    }

    public void playAudioAt(int index) {
        audioController.playAudioAt(index);
    }

    public void pause() {
        audioController.pause();
    }

    public class AudioBinder extends Binder {
        public MainService getMainService() {
            return MainService.this;
        }

        public LiveData<List<Audio>> getAudioList() {
            return audioList;
        }

        public AudioController getAudioController() {
            return audioController;
        }
    }
}
