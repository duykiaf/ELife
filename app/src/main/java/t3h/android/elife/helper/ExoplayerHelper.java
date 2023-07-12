package t3h.android.elife.helper;

import android.os.Bundle;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

import java.util.ArrayList;
import java.util.List;

import t3h.android.elife.models.Audio;

public class ExoplayerHelper {
    public static List<MediaItem> getMediaItems(List<Audio> audioList) {
        List<MediaItem> mediaItems = new ArrayList<>();
        for (Audio audio : audioList) {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(audio.getAudioFile())
                    .setMediaMetadata(getMetadata(audio))
                    .build();
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }

    public static MediaMetadata getMetadata(Audio audio) {
        Bundle bundle = new Bundle();
        bundle.putInt("audioId", audio.getId());
        return new MediaMetadata.Builder()
                .setExtras(bundle)
                .setTitle(audio.getTitle())
                .setDescription(audio.getLyrics())
                .build();
    }
}
