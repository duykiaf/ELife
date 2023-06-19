package t3h.android.elife.helper;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import t3h.android.elife.models.Audio;
import t3h.android.elife.repositories.MainRepository;

public class AudioHelper {
    public interface DurationCallback {
        void onDurationReceived(String durationStr);

        void onDurationError();
    }

    public static void getAudioDuration(String audioUrl, DurationCallback callback) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.setOnPreparedListener(mp -> {
                int duration = mp.getDuration();
                callback.onDurationReceived(milliSecondsToTimer(duration));
                mp.release();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                callback.onDurationError();
                mp.release(); // release the MediaPlayer resources
                return true;
            });
            mediaPlayer.prepareAsync(); // prepare the MediaPlayer asynchronously
        } catch (Exception e) {
            e.printStackTrace();
            callback.onDurationError();
        }
    }

    private static String milliSecondsToTimer(int milliSeconds) {
        StringBuilder timerStr = new StringBuilder("");
        StringBuilder secondsStr = new StringBuilder("");
        int hours = milliSeconds / (1000 * 60 * 60);
        int minutes = (milliSeconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (milliSeconds % (1000 * 60 * 60)) % (1000 * 60) / 1000;
        if (hours > 0) {
            timerStr.append(hours).append(":");
        }
        if (seconds < 10) {
            secondsStr.append("0").append(seconds);
        } else {
            secondsStr.append(seconds);
        }
        timerStr.append(minutes).append(":").append(secondsStr);
        return timerStr.toString();
    }

    public void addBookmark(Activity activity, Audio audio, OnAddListener onAddListener) {
        new AddBookmarkTask(activity, audio, onAddListener).forceLoad();
    }

    private class AddBookmarkTask extends AsyncTaskLoader<Long> {
        private final Activity activity;
        private final Audio data;
        private final OnAddListener onAddListener;

        public AddBookmarkTask(@NonNull Activity activity, Audio data, OnAddListener onAddListener) {
            super(activity);
            this.activity = activity;
            this.data = data;
            this.onAddListener = onAddListener;
        }

        @Nullable
        @Override
        public Long loadInBackground() {
            return getAudioId();
        }

        @Override
        public void deliverResult(@Nullable Long data) {
            super.deliverResult(data);
            if (onAddListener != null) {
                onAddListener.onFinish(data);
            }
        }

        private Long getAudioId() {
            MainRepository mainRepository = new MainRepository(activity.getApplication());
            return mainRepository.addBookmark(data);
        }
    }

    public interface OnAddListener{
        void onFinish(long audioId);
    }

    public void removeBookmark(Activity activity, Audio audio, OnRemoveListener onRemoveListener) {
        new RemoveBookmark(activity, audio, onRemoveListener).forceLoad();
    }

    private class RemoveBookmark extends AsyncTaskLoader<Integer> {
        private final Activity activity;
        private final Audio data;
        private final OnRemoveListener onRemoveListener;

        public RemoveBookmark(@NonNull Activity activity, Audio data, OnRemoveListener onRemoveListener) {
            super(activity);
            this.activity = activity;
            this.data = data;
            this.onRemoveListener = onRemoveListener;
        }

        @Nullable
        @Override
        public Integer loadInBackground() {
            return getDeletedRow();
        }

        @Override
        public void deliverResult(@Nullable Integer data) {
            super.deliverResult(data);
            if (onRemoveListener != null) {
                onRemoveListener.onFinish(data);
            }
        }

        private int getDeletedRow() {
            MainRepository mainRepository = new MainRepository(activity.getApplication());
            return mainRepository.removeBookmark(data);
        }
    }

    public interface OnRemoveListener{
        void onFinish(int deletedRow);
    }
}
