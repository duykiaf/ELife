package t3h.android.elife.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.exoplayer2.ExoPlayer;

import t3h.android.elife.models.Audio;

public class MainViewModel extends ViewModel {
    private MutableLiveData<ExoPlayer> player = new MutableLiveData<>();
    private MutableLiveData<String> audioLyrics = new MutableLiveData<>();

    public void setPlayer(ExoPlayer player) {
        this.player.setValue(player);
    }

    public LiveData<ExoPlayer> getPlayer() {
        return player;
    }

    public void setAudioLyrics(String audioLyrics) {
        this.audioLyrics.setValue(audioLyrics);
    }

    public LiveData<String> getAudioLyrics() {
        return audioLyrics;
    }
}
