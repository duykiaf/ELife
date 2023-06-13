package t3h.android.elife.repositories;

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
import t3h.android.elife.models.Topic;

public class MainRepository {
    private MainApi mainApi;

    public MainRepository() {
        mainApi = ApiProvider.getMainApi();
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
}
