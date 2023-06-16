package t3h.android.elife.repositories;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import t3h.android.elife.api.ApiProvider;
import t3h.android.elife.api.MainApi;
import t3h.android.elife.dao.AudioDao;
import t3h.android.elife.database.AudioDatabase;
import t3h.android.elife.models.Audio;
import t3h.android.elife.models.Topic;

public class MainRepository {
    private MainApi mainApi;
    private AudioDao audioDao;

    public MainRepository() {
        mainApi = ApiProvider.getMainApi();
    }

    public MainRepository(Application application) {
        audioDao = AudioDatabase.getInstance(application).audioDao();
    }

    public LiveData<List<Topic>> getActiveTopicsList() {
        MutableLiveData<List<Topic>> liveData = new MutableLiveData<>(new ArrayList<>());
        mainApi.getActiveTopicsList().enqueue(new Callback<List<Topic>>() {
            @Override
            public void onResponse(Call<List<Topic>> call, Response<List<Topic>> response) {
                liveData.setValue(response.body());
            }

            @Override
            public void onFailure(Call<List<Topic>> call, Throwable t) {
                Log.e("ERROR getActiveTopicsList", t.getMessage());
            }
        });
        return liveData;
    }

    public LiveData<List<Audio>> getActiveAudiosListByTopicId(int topicId) {
        MutableLiveData<List<Audio>> liveData = new MutableLiveData<>(new ArrayList<>());
        mainApi.getActiveAudiosListByTopic(topicId).enqueue(new Callback<List<Audio>>() {
            @Override
            public void onResponse(Call<List<Audio>> call, Response<List<Audio>> response) {
                liveData.setValue(response.body());
            }

            @Override
            public void onFailure(Call<List<Audio>> call, Throwable t) {
                Log.e("ERROR getActiveAudiosListByTopicId", t.getMessage());
            }
        });
        return liveData;
    }

    public Long addBookmark(Audio audio) {
        if (audio != null) {
            return audioDao.insert(audio);
        }
        return -1L;
    }

    public LiveData<List<Audio>> getBookmarksList() {
        return audioDao.getAllList();
    }

    public LiveData<List<Integer>> getAudioIds() {
        return audioDao.getAudioIds();
    }

    public int removeBookmark(Audio audio) {
        if (audio != null) {
            return audioDao.delete(audio);
        }
        return 0;
    }
}
