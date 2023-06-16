package t3h.android.elife.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import t3h.android.elife.models.Audio;

@Dao
public interface AudioDao {
    @Query("select * from audio")
    LiveData<List<Audio>> getAllList();

    @Query("select audio.id from audio")
    LiveData<List<Integer>> getAudioIds();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Audio audio);

    @Delete
    int delete(Audio audio);
}
