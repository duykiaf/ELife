package t3h.android.elife.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import t3h.android.elife.R;
import t3h.android.elife.databinding.AudioItemLayoutBinding;
import t3h.android.elife.helper.AppConstant;
import t3h.android.elife.helper.AudioHelper;
import t3h.android.elife.models.Audio;
import t3h.android.elife.repositories.MainRepository;

public class AudiosListAdapter extends RecyclerView.Adapter<AudiosListAdapter.AudioItemViewHolder> {
    private List<Audio> audioList;
    private List<Integer> bookmarkAudioIds;
    private OnAudioClickListener onAudioClickListener;
    private int currentIndex = -1;

    public AudiosListAdapter() {
        audioList = new ArrayList<>();
        bookmarkAudioIds = new ArrayList<>();
    }

    public void setAudioList(List<Audio> audioList) {
        this.audioList = audioList;
        notifyDataSetChanged();
    }

    public void setBookmarkAudioIds(List<Integer> audioIds) {
        bookmarkAudioIds = audioIds;
    }

    public void setOnAudioClickListener(OnAudioClickListener listener) {
        onAudioClickListener = listener;
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
        private AudioItemLayoutBinding binding;

        public AudioItemViewHolder(@NonNull AudioItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.audioItemLayout.setOnClickListener(v -> {
                if (onAudioClickListener != null) {
                    onAudioClickListener.onItemClick(audioList.get(getAdapterPosition()));
                }
            });

            binding.playIcon.setOnClickListener(v -> {
                if (onAudioClickListener != null) {
                    onAudioClickListener.onIconClick(binding, binding.playIcon, audioList.get(getAdapterPosition()));
                }
            });

            binding.bookmarkIcon.setOnClickListener(v -> {
                if (onAudioClickListener != null) {
                    onAudioClickListener.onIconClick(binding, binding.bookmarkIcon, audioList.get(getAdapterPosition()));
                }
            });
        }

        public void bindView(Audio audio) {
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
                for (Integer id: bookmarkAudioIds) {
                    if (audio.getId() == id) {
                        binding.bookmarkIcon.setContentDescription(AppConstant.BOOKMARK_ICON);
                        binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark);
                        break;
                    }
                }
            }
        }
    }

    public interface OnAudioClickListener {
        void onItemClick(Audio audio);

        void onIconClick(AudioItemLayoutBinding binding, ImageView imageView, Audio audio);
    }
}
