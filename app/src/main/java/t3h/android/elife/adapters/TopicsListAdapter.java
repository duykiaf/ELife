package t3h.android.elife.adapters;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import t3h.android.elife.databinding.TopicItemLayoutBinding;
import t3h.android.elife.models.Topic;

public class TopicsListAdapter extends RecyclerView.Adapter<TopicsListAdapter.TopicItemViewHolder> {
    private List<Topic> topicList;
    private List<Topic> dataSource;
    private Timer timer;
    private OnTopicClickListener onTopicClickListener;

    public TopicsListAdapter() {
        topicList = new ArrayList<>();
    }

    public void setTopicList(List<Topic> topicList) {
        this.topicList = topicList;
        dataSource = topicList;
        notifyDataSetChanged();
    }

    public void setOnTopicClickListener(OnTopicClickListener listener) {
        onTopicClickListener = listener;
    }

    @NonNull
    @Override
    public TopicItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TopicItemViewHolder(
                TopicItemLayoutBinding.inflate(
                        LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull TopicItemViewHolder holder, int position) {
        holder.bindView(topicList.get(position));
    }

    @Override
    public int getItemCount() {
        return topicList != null ? topicList.size() : 0;
    }

    public class TopicItemViewHolder extends RecyclerView.ViewHolder {
        private TopicItemLayoutBinding binding;

        public TopicItemViewHolder(@NonNull TopicItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.topicItem.setOnClickListener(v -> {
                if (onTopicClickListener != null) {
                    onTopicClickListener.onItemClick(topicList.get(getAdapterPosition()));
                }
            });
        }

        public void bindView(Topic topic) {
            binding.setTopic(topic);
        }
    }

    public void searchList(String keyword) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (keyword.isEmpty()) {
                    topicList = dataSource;
                } else {
                    List<Topic> temp = new ArrayList<>();
                    for (Topic topic : dataSource) {
                        if (topic.getName().toLowerCase().contains(keyword)) {
                            temp.add(topic);
                        }
                    }
                    topicList = temp;
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

    public interface OnTopicClickListener{
        void onItemClick(Topic topic);
    }
}
