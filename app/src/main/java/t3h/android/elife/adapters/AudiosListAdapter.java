package t3h.android.elife.adapters;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
    private int currentIndex = -1;
    public int status;
    private String imgContentDesc = "";
    private int iconColor = 0;
    private int textColor = 0;
    private int imageResource = 0;
    private int audioBgResource = 0;

    public AudiosListAdapter() {
        audioList = new ArrayList<>();
        bookmarkAudioIds = new ArrayList<>();
    }

    public void setAudioList(List<Audio> audioList) {
        this.audioList = audioList;
        dataSource = audioList;
        notifyDataSetChanged();
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
                if (onAudioClickListener != null) {
                    onAudioClickListener.onIconClick(binding, binding.playIcon, audioList.get(getAdapterPosition()), getAdapterPosition());
                }
            });

            binding.bookmarkIcon.setOnClickListener(v -> {
                if (onAudioClickListener != null) {
                    onAudioClickListener.onIconClick(binding, binding.bookmarkIcon, audioList.get(getAdapterPosition()), getAdapterPosition());
                }
            });
        }

        public void bindView(Audio audio) {
            binding.durationTxt.setText(AppConstant.DURATION_DEFAULT);
            binding.bookmarkIcon.setContentDescription(AppConstant.BOOKMARK_BORDER_ICON);
            binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_border);

            binding.setAudio(audio);
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
            if (bookmarkAudioIds != null) {
                for (Integer id : bookmarkAudioIds) {
                    if (audio.getId() == id) {
                        binding.bookmarkIcon.setContentDescription(AppConstant.BOOKMARK_ICON);
                        binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark);
                        break;
                    }
                }
            }
            if (currentIndex == getAdapterPosition()) {
                switch (status) {
                    case AppConstant.NONE:
                        imgContentDesc = AppConstant.PLAY_ICON;
                        iconColor = Color.parseColor("#1C89DA");
                        textColor = Color.parseColor("#000000");
                        imageResource = R.drawable.ic_play_circle_outline;
                        audioBgResource = R.drawable.card_background;
                        break;
                    case AppConstant.PLAY:
                        imgContentDesc = AppConstant.PAUSE_ICON;
                        iconColor = Color.parseColor("#ffffff");
                        textColor = Color.parseColor("#ffffff");
                        imageResource = R.drawable.ic_pause_circle_outline;
                        audioBgResource = R.drawable.blue_btn_background;
                        break;
                    case AppConstant.STOP:
                        imgContentDesc = AppConstant.PLAY_ICON;
                        iconColor = Color.parseColor("#ffffff");
                        textColor = Color.parseColor("#ffffff");
                        imageResource = R.drawable.ic_play_circle_outline;
                        audioBgResource = R.drawable.blue_btn_background;
                        break;
                }
            } else {
                imgContentDesc = AppConstant.PLAY_ICON;
                iconColor = Color.parseColor("#1C89DA");
                textColor = Color.parseColor("#000000");
                imageResource = R.drawable.ic_play_circle_outline;
                audioBgResource = R.drawable.card_background;
            }
            binding.playIcon.setContentDescription(imgContentDesc);
            binding.playIcon.setColorFilter(iconColor);
            binding.bookmarkIcon.setColorFilter(iconColor);
            binding.playIcon.setImageResource(imageResource);
            binding.audioItemLayout.setBackgroundResource(audioBgResource);
            binding.audioTitle.setTextColor(textColor);
            binding.durationTxt.setTextColor(textColor);
        }
    }

    public interface OnAudioClickListener {
        void onItemClick(Audio audio, int position);

        void onIconClick(AudioItemLayoutBinding binding, ImageView imageView, Audio audio, int position);
    }

    public void searchList(String keyword) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
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
        }, 500);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
