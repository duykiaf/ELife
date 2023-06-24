package t3h.android.elife.helper;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import t3h.android.elife.models.Audio;

public class AudioController {
    private MediaPlayer mediaPlayer;
    private int currentIndex;
    private boolean isPreparing;
    private AudioSource audioSource;

    public interface AudioSource {
        int getSize();
        Audio getAudioAtIndex(int index);
    }

    public AudioController(AudioSource audioSource) {
        this.audioSource = audioSource;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(mediaPlayer -> {
            mediaPlayer.start();
            isPreparing = false;
        });
        currentIndex = -1;
        isPreparing = false;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public boolean isPreparing() {
        return isPreparing;
    }

    public void playNext() {
        if (audioSource == null) return;
        if (audioSource.getSize() != 0) {
            if (currentIndex < audioSource.getSize() - 1) {
                currentIndex++;
            } else {
                currentIndex = 0;
            }
            playAudioAt(currentIndex);
        }
    }

    public void playPrev() {
        if (audioSource == null) return;
        if (audioSource.getSize() != 0) {
            if (currentIndex > 0) {
                currentIndex--;
            } else {
                currentIndex = audioSource.getSize() - 1;
            }
            playAudioAt(currentIndex);
        }
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void start() {
        mediaPlayer.start();
    }

    public void playAudioAt(int index) {
        mediaPlayer.reset();
        if (audioSource == null) return;
        try {
            mediaPlayer.setDataSource(audioSource.getAudioAtIndex(index).getAudioFile());
            currentIndex = index;
        } catch (Exception e) {
            Log.e("AUDIO CONTROLLER", "Error starting data source", e);
        }
        mediaPlayer.prepareAsync();
        isPreparing = true;
    }
}
