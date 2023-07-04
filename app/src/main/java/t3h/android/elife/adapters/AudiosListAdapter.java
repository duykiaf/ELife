package t3h.android.elife.adapters;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import t3h.android.elife.R;
import t3h.android.elife.databinding.AudioItemLayoutBinding;
import t3h.android.elife.helper.AppConstant;
import t3h.android.elife.helper.AudioHelper;
import t3h.android.elife.models.Audio;

public class AudiosListAdapter extends RecyclerView.Adapter<AudiosListAdapter.AudioItemViewHolder> {
    private List<Audio> dataSource;
    private List<Audio> audioList;
    private List<Integer> bookmarkAudioIds;
    private OnAudioClickListener onAudioClickListener;
    private Timer timer;
    private int currentIndex;
    public int status;
    private String imgContentDesc = "";
    private int iconColor = 0;
    private int textColor = 0;
    private int imageResource = 0;
    private int audioBgResource = 0;
    private ExoPlayer player;

    public AudiosListAdapter() {
        audioList = new ArrayList<>();
        bookmarkAudioIds = new ArrayList<>();
    }

    public void setAudioList(List<Audio> audioList) {
        this.audioList = audioList;
        dataSource = audioList;
        notifyDataSetChanged();
    }

    public void setPlayer(ExoPlayer player) {
        this.player = player;
    }

    public void setBookmarkAudioIds(List<Integer> audioIds) {
        List<Integer> previousBookmarkAudioIds = new ArrayList<>(bookmarkAudioIds);
        bookmarkAudioIds = audioIds;
        // Find the changed position
        int changedPosition = 0;
        for (int i = 0; i < audioList.size(); i++) {
            Audio audio = audioList.get(i);
            if (previousBookmarkAudioIds.contains(audio.getId()) != bookmarkAudioIds.contains(audio.getId())) {
                changedPosition = i;
                break;
            }
        }
        // Notify item changes for the updated position
        notifyItemChanged(changedPosition);
    }

    public void setOnAudioClickListener(OnAudioClickListener listener) {
        onAudioClickListener = listener;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    @NonNull
    @Override
    public AudioItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AudioItemViewHolder(
                AudioItemLayoutBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull AudioItemViewHolder holder, int position) {
        holder.bindView(audioList.get(position));
    }

    @Override
    public int getItemCount() {
        return audioList != null ? audioList.size() : 0;
    }

    public class AudioItemViewHolder extends RecyclerView.ViewHolder {
        private final AudioItemLayoutBinding binding;

        public AudioItemViewHolder(@NonNull AudioItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.audioItemLayout.setOnClickListener(v -> {
                if (onAudioClickListener != null) {
                    onAudioClickListener.onItemClick(audioList.get(getAdapterPosition()), getAdapterPosition());
                }
            });

            binding.playIcon.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                MediaItem mediaItem = getMediaItem(audioList.get(getAdapterPosition()));
                if (player.isPlaying()) {
                    if (currentIndex == pos) {
                        player.pause();
                    } else {
                        player.seekTo(pos, 0);
                        player.prepare();
                        player.play();
                    }
                } else {
                    if (currentIndex != pos) {
                        player.seekTo(pos, 0);
                    }
                    player.prepare();
                    player.play();
                }

                if (onAudioClickListener != null) {
                    onAudioClickListener.onIconClick(binding, binding.playIcon,
                            audioList.get(getAdapterPosition()), getAdapterPosition(), audioList);
                }
            });

            binding.bookmarkIcon.setOnClickListener(v -> {
                if (onAudioClickListener != null) {
                    onAudioClickListener.onIconClick(binding, binding.bookmarkIcon, audioList.get(getAdapterPosition()),
                            getAdapterPosition(), audioList);
                }
            });
        }

        private MediaItem getMediaItem(Audio audio) {
            return new MediaItem.Builder()
                    .setUri(audio.getAudioFile())
                    .setMediaMetadata(getMetadata(audio))
                    .build();
        }

        private MediaMetadata getMetadata(Audio audio) {
            return new MediaMetadata.Builder()
                    .setTitle(audio.getTitle())
                    .build();
        }

        public void bindView(Audio audio) {
            resetAudioView();
            binding.setAudio(audio);
            bindAudioDurationTxt(audio);
            initBookmarkIcon(audio);
            initAudioView();
        }

        private void resetAudioView() {
            binding.durationTxt.setText(AppConstant.DURATION_DEFAULT);
            binding.bookmarkIcon.setContentDescription(AppConstant.BOOKMARK_BORDER_ICON);
            binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_border);
        }

        private void bindAudioDurationTxt(Audio audio) {
            AudioHelper.getAudioDuration(audio.getAudioFile(), new AudioHelper.DurationCallback() {
                @Override
                public void onDurationReceived(String durationStr) {
                    binding.durationTxt.setText(durationStr);
                }

                @Override
                public void onDurationError() {
                    binding.durationTxt.setText(AppConstant.DURATION_DEFAULT);
                }
            });
        }

        private void initBookmarkIcon(Audio audio) {
            if (bookmarkAudioIds != null) {
                for (Integer id : bookmarkAudioIds) {
                    if (audio.getId() == id) {
                        binding.bookmarkIcon.setContentDescription(AppConstant.BOOKMARK_ICON);
                        binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark);
                        break;
                    }
                }
            }
        }

        private void initAudioView() {
            if (currentIndex == getAdapterPosition()) {
                switch (status) {
                    case AppConstant.NONE:
                        initAudioStatusDefault();
                        break;
                    case AppConstant.PLAY:
                        imgContentDesc = AppConstant.PAUSE_ICON;
                        imageResource = R.drawable.ic_pause_circle_outline;
                        initAudioSelectedView(Color.WHITE);
                        break;
                    case AppConstant.STOP:
                        imgContentDesc = AppConstant.PLAY_ICON;
                        imageResource = R.drawable.ic_play_circle_outline;
                        initAudioSelectedView(Color.WHITE);
                        break;
                }
            } else {
                initAudioStatusDefault();
            }
            binding.playIcon.setContentDescription(imgContentDesc);
            binding.playIcon.setColorFilter(iconColor);
            binding.bookmarkIcon.setColorFilter(iconColor);
            binding.playIcon.setImageResource(imageResource);
            binding.audioItemLayout.setBackgroundResource(audioBgResource);
            binding.audioTitle.setTextColor(textColor);
            binding.durationTxt.setTextColor(textColor);
        }

        private void initAudioStatusDefault() {
            imgContentDesc = AppConstant.PLAY_ICON;
            iconColor = Color.parseColor("#1C89DA");
            textColor = Color.parseColor("#000000");
            imageResource = R.drawable.ic_play_circle_outline;
            audioBgResource = R.drawable.card_background;
        }

        private void initAudioSelectedView(int colorCode) {
            iconColor = textColor = colorCode;
            audioBgResource = R.drawable.blue_btn_background;
        }
    }

    public interface OnAudioClickListener {
        void onItemClick(Audio audio, int position);

        void onIconClick(AudioItemLayoutBinding binding, ImageView imageView, Audio audio,
                         int position, List<Audio> audioList);
    }

    public void searchList(String keyword) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (dataSource == null) {
                    return;
                } else {
                    if (keyword.isEmpty()) {
                        audioList = dataSource;
                    } else {
                        List<Audio> temp = new ArrayList<>();
                        for (Audio item : dataSource) {
                            if (item.getTitle().toLowerCase().contains(keyword)) {
                                temp.add(item);
                            }
                        }
                        audioList = temp;
                    }
                    new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());
                }
            }
        }, 500);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
