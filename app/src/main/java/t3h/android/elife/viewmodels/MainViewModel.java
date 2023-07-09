package t3h.android.elife.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.exoplayer2.ExoPlayer;

public class MainViewModel extends ViewModel {
    private MutableLiveData<ExoPlayer> player = new MutableLiveData<>();
    private MutableLiveData<Boolean> repeatModeOne = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> repeatModeAll = new MutableLiveData<>(true);
    private MutableLiveData<String> audioLyrics = new MutableLiveData<>();

    public void setPlayer(ExoPlayer player) {
        this.player.setValue(player);
    }

    public LiveData<ExoPlayer> getPlayer() {
        return player;
    }

    public void setRepeatModeOne(Boolean repeatModeOne) {
        this.repeatModeOne.setValue(repeatModeOne);
    }

    public LiveData<Boolean> getRepeatModeOne() {
        return repeatModeOne;
    }

    public void setRepeatModeAll(Boolean repeatModeAll) {
        this.repeatModeAll.setValue(repeatModeAll);
    }

    public LiveData<Boolean> getRepeatModeAll() {
        return repeatModeAll;
    }

    public void setAudioLyrics(String audioLyrics) {
        this.audioLyrics.setValue(audioLyrics);
    }

    public LiveData<String> getAudioLyrics() {
        return audioLyrics;
    }
}
