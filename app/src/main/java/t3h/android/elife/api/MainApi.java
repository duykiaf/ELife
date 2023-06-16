package t3h.android.elife.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import t3h.android.elife.models.Audio;
import t3h.android.elife.models.Topic;

public interface MainApi {
    @GET("/topic/get-active-topics")
    Call<List<Topic>> getActiveTopicsList();

    @GET("/audio/get-list-by-topic/{topicId}")
    Call<List<Audio>> getActiveAudiosListByTopic(@Path("topicId") int topicId);
}
