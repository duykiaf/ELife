package t3h.android.elife.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity(tableName = "audio")
public class Audio implements Serializable {
    @PrimaryKey
    @SerializedName("id")
    @Expose
    private int id;

    @ColumnInfo(name = "title")
    @SerializedName("title")
    @Expose
    private String title;

    @ColumnInfo(name = "audio_file")
    @SerializedName("audio_file")
    @Expose
    private String audioFile;

    @ColumnInfo(name = "file_name")
    @SerializedName("file_name")
    @Expose
    private String fileName;

    @ColumnInfo(name = "lyrics")
    @SerializedName("lyrics")
    @Expose
    private String lyrics;

    @ColumnInfo(name = "topic_id")
    @SerializedName("topic_id")
    @Expose
    private int topicId;

    @ColumnInfo(name = "status")
    @SerializedName("status")
    @Expose
    private int status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return title;
    }
}
