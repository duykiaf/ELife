package t3h.android.elife.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import t3h.android.elife.dao.AudioDao;
import t3h.android.elife.models.Audio;

@Database(entities = {Audio.class}, version = 1, exportSchema = false)
public abstract class AudioDatabase extends RoomDatabase {
    public abstract AudioDao audioDao();

    private static AudioDatabase INSTANCE;

    public static synchronized AudioDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, AudioDatabase.class, "audio_db.db").build();
        }
        return INSTANCE;
    }
}
