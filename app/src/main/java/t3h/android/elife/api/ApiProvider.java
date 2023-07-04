package t3h.android.elife.api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiProvider {
    private static final String BASE_URL = "http://192.168.1.6:3000";
    private static MainApi mainApi;
    private static Retrofit retrofit;

    public static MainApi getMainApi() {
        if (mainApi == null) {
            mainApi = getRetrofit().create(MainApi.class);
        }
        return mainApi;
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            retrofit = new Retrofit.Builder()
                    .client(okHttpClient)
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
