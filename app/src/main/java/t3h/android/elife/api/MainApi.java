package t3h.android.elife.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import t3h.android.elife.models.Audio;
import t3h.android.elife.models.Topic;

public interface ApiHelper {
    @GET("/topic/list")
    Call<List<Topic>> getActiveTopicsList();

    @GET("/audio/get-list-by-topic")
    Call<List<Audio>> getActiveAudiosListByTopic();
}
