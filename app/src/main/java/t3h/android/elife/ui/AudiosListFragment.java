package t3h.android.elife.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import t3h.android.elife.R;
import t3h.android.elife.adapters.AudiosListAdapter;
import t3h.android.elife.databinding.AudioItemLayoutBinding;
import t3h.android.elife.databinding.FragmentAudiosListBinding;
import t3h.android.elife.helper.AppConstant;
import t3h.android.elife.helper.AudioHelper;
import t3h.android.elife.models.Audio;
import t3h.android.elife.models.Topic;
import t3h.android.elife.repositories.MainRepository;
import t3h.android.elife.services.MainService;
import t3h.android.elife.viewmodels.MainViewModel;

public class AudiosListFragment extends Fragment {
    private FragmentAudiosListBinding binding;
    private int iconColor = 0;
    private String imgContentDesc;
    private int imageResource = 0;
    private boolean isBookmarksList = false;
    private boolean isSearching = false;
    private boolean wasRemovedBookmark;
    private Topic topicInfo;
    private AudiosListAdapter adapter = new AudiosListAdapter();
    private AudiosListAdapter bookmarksListAdapter = new AudiosListAdapter();
    private final MainRepository mainRepository = new MainRepository();
    private final AudioHelper audioHelper = new AudioHelper();
    private ExoPlayer player;
    private Audio audio;
    private NavController navController;
    private MainViewModel mainViewModel;
    private Bundle audioInfoBundle = new Bundle();
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }
            audio = (Audio) bundle.get("AudioInfo");
            //isPlaying = bundle.getBoolean("AudioStatus");
            handleMinimizeAudioLayout(bundle.getInt("AudioAction"));
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(broadcastReceiver, new IntentFilter("DataFromService"));
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_audios_list, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment);
        player = new ExoPlayer.Builder(requireActivity()).build();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        isBookmarksList = requireArguments().getBoolean("isBookmarksList");
        if (!isBookmarksList) {
            topicInfo = (Topic) requireArguments().get("topicInfo");
            initTopAppBarUI(false);
            initAudiosList(player);
        } else {
            initTopAppBarUI(true);
            loadBookmarksList(player);
        }
    }

    private void initTopAppBarUI(boolean isBookmarksList) {
        if (isBookmarksList) {
            binding.appBarFragment.topAppBar.setTitle(AppConstant.BOOKMARKS_LIST);
        } else {
            binding.appBarFragment.topAppBar.setTitle(topicInfo.getName());
        }
        binding.appBarFragment.topAppBar.setNavigationIcon(R.drawable.ic_arrow_back);
    }

    @Override
    public void onResume() {
        super.onResume();
        onBackPressed();
        binding.goToTopImageView.setOnClickListener(v -> binding.audiosRcv.smoothScrollToPosition(0));
    }

    private void initAudiosList(ExoPlayer player) {
        MainRepository mainRepoForDao = new MainRepository(requireActivity().getApplication());
        mainRepoForDao.getAudioIds().observe(requireActivity(), adapter::setBookmarkAudioIds);
        mainRepository.getActiveAudiosListByTopicId(topicInfo.getId()).observe(requireActivity(), audioList -> {
            Audio firstAudio = null;
            if (audioList != null && audioList.size() > 0) {
                binding.noDataTxt.setVisibility(View.GONE);
                firstAudio = audioList.get(0);
                player.setMediaItems(getMediaItems(audioList));
            } else {
                binding.noDataTxt.setVisibility(View.VISIBLE);
                player.clearMediaItems();
            }
            adapter.setAudioList(audioList);
            adapter.setPlayer(player);
            initAudioControlLayout(adapter, firstAudio, player);
        });
        initRcv(adapter);
        onMenuItemSelected(player);
        onItemSelected(adapter, player);
    }

    private void onBackPressed() {
        if (!binding.appBarFragment.topAppBar.getTitle().equals(AppConstant.TOPICS)) {
            binding.appBarFragment.topAppBar.setNavigationOnClickListener(v -> {
                requireActivity().onBackPressed();
            });
        }
    }

    private void onMenuItemSelected(ExoPlayer player) {
        binding.appBarFragment.topAppBar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.searchItem:
                    if (binding.searchEdt.getVisibility() == View.VISIBLE) {
                        resetSearchFeature(player);
                    } else {
                        isSearching = true;
                        setSearchLayoutState(true);
                        binding.searchEdt.requestFocus();
                        binding.audioControlLayout.setVisibility(View.GONE);
                        if (!isBookmarksList) {
                            loadSearchList(adapter, player);
                        }
                    }
                    return true;
                case R.id.bookmarksItem:
                    if (!isBookmarksList) {
                        initTopAppBarUI(true);
                        isBookmarksList = true;
                        resetSearchFeature(player);
                    }
                    return true;
            }
            return false;
        });
    }

    private void loadBookmarksList(ExoPlayer player) {
        MainRepository mainRepoForDao = new MainRepository(requireActivity().getApplication());
        mainRepoForDao.getAudioIds().observe(requireActivity(), audioIds -> bookmarksListAdapter.setBookmarkAudioIds(audioIds));
        mainRepoForDao.getBookmarksList().observe(requireActivity(), audioList -> {
            if (audioList != null && audioList.size() > 0) {
                if (binding != null) {
                    binding.noDataTxt.setVisibility(View.GONE);
                    Audio firstAudio = audioList.get(0);
                    if (wasRemovedBookmark) {
                        player.setMediaItems(getMediaItems(audioList), bookmarksListAdapter.getCurrentIndex(), 0);
                    } else {
                        player.setMediaItems(getMediaItems(audioList));
                    }
                    bookmarksListAdapter.setAudioList(audioList);
                    bookmarksListAdapter.setPlayer(player);
                    initAudioControlLayout(bookmarksListAdapter, firstAudio, player);
                    if (isSearching) {
                        binding.searchEdt.setText("");
                    }
                }
            } else {
                if (binding != null) {
                    binding.noDataTxt.setVisibility(View.VISIBLE);
                    player.clearMediaItems();
                    bookmarksListAdapter.setAudioList(audioList);
                    bookmarksListAdapter.setPlayer(player);
                    initAudioControlLayout(bookmarksListAdapter, null, player);
                }
            }
        });
        initRcv(bookmarksListAdapter);
        onMenuItemSelected(player);
        loadSearchList(bookmarksListAdapter, player);
        onItemSelected(bookmarksListAdapter, player);
    }

    private void initRcv(AudiosListAdapter adapter) {
        binding.audiosRcv.setAdapter(adapter);
        binding.audiosRcv.setHasFixedSize(true);
        binding.audiosRcv.setLayoutManager(new LinearLayoutManager(requireActivity()));
    }

    private void initAudioControlLayout(AudiosListAdapter adapter, Audio audioInfo, ExoPlayer player) {
        if (audioInfo != null) {
            binding.audioControlLayout.setVisibility(View.VISIBLE);
            binding.audioTitle.setText(audioInfo.getTitle());
            initPlayOrPauseIcon(player);
            playerControls(adapter, player);
        } else {
            binding.audioControlLayout.setVisibility(View.GONE);
        }
    }

    private void playerControls(AudiosListAdapter adapter, ExoPlayer player) {
        binding.previousIcon.setOnClickListener(v -> {
            if (player.hasPreviousMediaItem()) {
                player.seekToPrevious();
                setPrepareAndPlay(player);
                adapterNotifyItemChanged(adapter, player);
            }
        });

        binding.nextIcon.setOnClickListener(v -> {
            if (player.hasNextMediaItem()) {
                player.seekToNext();
                setPrepareAndPlay(player);
                adapterNotifyItemChanged(adapter, player);
            }
        });

        binding.playOrPauseIcon.setOnClickListener(v -> {
            if (player.isPlaying()) {
                player.pause();
                adapter.status = AppConstant.STOP;
                binding.playOrPauseIcon.setImageResource(R.drawable.ic_play_circle_outline);
            } else {
                binding.playOrPauseIcon.setImageResource(R.drawable.ic_pause_circle_outline);
                setPrepareAndPlay(player);
                adapter.status = AppConstant.PLAY;
                if (adapter.getCurrentIndex() == 0) {
                    adapter.setCurrentIndex(player.getCurrentMediaItemIndex());
                }
            }
            adapter.notifyItemChanged(adapter.getCurrentIndex());
        });

        playerListener(adapter, player);
    }

    private List<MediaItem> getMediaItems(List<Audio> audioList) {
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

    private MediaMetadata getMetadata(Audio audio) {
        return new MediaMetadata.Builder()
                .setTitle(audio.getTitle())
                .build();
    }

    private void playerListener(AudiosListAdapter adapter, ExoPlayer player) {
        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                if (mediaItem != null) {
                    if (player.hasNextMediaItem() && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                        player.seekToNextMediaItem();
                        adapterNotifyItemChanged(adapter, player);
                    }
                    binding.audioTitle.setText(mediaItem.mediaMetadata.title);
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    binding.audioTitle.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    binding.playOrPauseIcon.setImageResource(R.drawable.ic_pause_circle_outline);
                }
                if (wasRemovedBookmark && !player.isPlaying() && binding != null) {
                    if (player.getCurrentMediaItem() != null) {
                        binding.audioTitle.setText(player.getCurrentMediaItem().mediaMetadata.title);
                    }
                    binding.playOrPauseIcon.setImageResource(R.drawable.ic_play_circle_outline);
                }
            }
        });
    }

    private void setPrepareAndPlay(ExoPlayer player) {
        player.prepare();
        player.play();
    }

    private void adapterNotifyItemChanged(AudiosListAdapter adapter, ExoPlayer player) {
        if (adapter.getCurrentIndex() != player.getCurrentMediaItemIndex()) {
            adapter.notifyItemChanged(adapter.getCurrentIndex());
        }
        adapter.setCurrentIndex(player.getCurrentMediaItemIndex());
        adapter.status = AppConstant.PLAY;
        adapter.notifyItemChanged(player.getCurrentMediaItemIndex());
    }

    private void initAudioDuration(Audio audioInfo) {
        binding.audioCurrentTime.setText(getResources().getString(R.string.audio_duration));
        AudioHelper.getAudioDuration(audioInfo.getAudioFile(), new AudioHelper.DurationCallback() {
            @Override
            public void onDurationReceived(String durationStr) {
                binding.audioDuration.setText(durationStr);
            }

            @Override
            public void onDurationError() {
                binding.audioDuration.setText(AppConstant.DURATION_DEFAULT);
            }
        });
    }

    private void initPlayOrPauseIcon(ExoPlayer player) {
        if (player.isPlaying()) {
            binding.playOrPauseIcon.setImageResource(R.drawable.ic_pause_circle_outline);
        } else {
            binding.playOrPauseIcon.setImageResource(R.drawable.ic_play_circle_outline);
        }
    }

    private void playOrPauseAudioOnService(Audio audio) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("audio", audio);
        Intent intent = new Intent(requireActivity(), MainService.class);
        intent.putExtras(bundle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent);
        } else {
            requireContext().startService(intent);
        }
    }

    private void clickStopService() {
        Intent intent = new Intent(requireActivity(), MainService.class);
        requireContext().stopService(intent);
    }

    private void onItemSelected(AudiosListAdapter adapter, ExoPlayer player) {
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mainViewModel.setPlayer(player);
        adapter.setOnAudioClickListener(new AudiosListAdapter.OnAudioClickListener() {
            @Override
            public void onItemClick(Audio audio, int position) {
                audioInfoBundle.putSerializable("audioInfo", audio);
                navController.navigate(R.id.action_audiosListFragment_to_audioDetailsFragment, audioInfoBundle);
//                playOrPauseAudioOnService(audio);
            }

            @Override
            public void onIconClick(AudioItemLayoutBinding binding, ImageView imageView, Audio audio,
                                    int position, List<Audio> audioList) {
                switch (imageView.getId()) {
                    case R.id.playIcon:
                        if (!isSearching) {
                            if (adapter.getCurrentIndex() != position) {
                                adapter.notifyItemChanged(adapter.getCurrentIndex());
                            }

                            adapter.setCurrentIndex(position);
                            // play audio
                            if (imageView.getContentDescription().equals(AppConstant.PLAY_ICON)) {
                                adapter.status = AppConstant.PLAY;
                                adapter.notifyItemChanged(adapter.getCurrentIndex());
                            }
                            // pause audio
                            if (imageView.getContentDescription().equals(AppConstant.PAUSE_ICON)) {
                                adapter.status = AppConstant.STOP;
                                adapter.notifyItemChanged(adapter.getCurrentIndex());
                            }
                            initAudioControlLayout(adapter, audio, player);
                        } else {
                            audioInfoBundle.putSerializable("audioInfo", audio);
                            navController.navigate(R.id.action_audiosListFragment_to_audioDetailsFragment, audioInfoBundle);
                        }
//                        playOrPauseAudioOnService(audio);
                        break;
                    case R.id.bookmarkIcon:
                        if (player.isPlaying()) {
                            iconColor = getResources().getColor(R.color.white);
                        } else {
                            iconColor = getResources().getColor(R.color.primaryColor);
                        }
                        binding.bookmarkIcon.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
                        if (imageView.getContentDescription().equals(AppConstant.BOOKMARK_BORDER_ICON)) {
                            imgContentDesc = AppConstant.BOOKMARK_ICON;
                            imageResource = R.drawable.ic_bookmark;
                            addBookmark(audio);
                        }
                        if (imageView.getContentDescription().equals(AppConstant.BOOKMARK_ICON)) {
                            imgContentDesc = AppConstant.BOOKMARK_BORDER_ICON;
                            imageResource = R.drawable.ic_bookmark_border;
                            removeBookmark(adapter, audio, position, player);
                        }
                        imageView.setContentDescription(imgContentDesc);
                        imageView.setImageResource(imageResource);
                        break;
                }
            }
        });
    }

    private void loadSearchList(AudiosListAdapter adapter, ExoPlayer player) {
        binding.searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                InputFilter[] filters = new InputFilter[1];
                filters[0] = new InputFilter.LengthFilter(AppConstant.KEYWORD_MAX_LENGTH);
                binding.searchEdt.setFilters(filters);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.cancelTimer();
                if (charSequence.toString().length() == AppConstant.KEYWORD_MAX_LENGTH) {
                    binding.searchEdt.setError(AppConstant.SEARCH_ERROR);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                adapter.searchList(editable.toString().trim().toLowerCase());
            }
        });
        binding.closeSearchLayout.setOnClickListener(v -> resetSearchFeature(player));
    }

    private void resetSearchFeature(ExoPlayer player) {
        isSearching = false;
        setSearchLayoutState(false);
        binding.searchEdt.setText("");
        if (isBookmarksList) {
            loadBookmarksList(player);
        } else {
            initAudiosList(player);
        }
    }

    private void setSearchLayoutState(boolean isVisible) {
        binding.searchEdt.setVisibility(getState(isVisible));
        binding.closeSearchLayout.setVisibility(getState(isVisible));
    }

    private int getState(boolean isVisible) {
        return isVisible ? View.VISIBLE : View.GONE;
    }

    private void addBookmark(Audio audio) {
        audioHelper.addBookmark(requireActivity(), audio, audioId -> {
            if (audioId > 0) {
                Toast.makeText(requireActivity(), AppConstant.ADD_BOOKMARK_SUCCESSFULLY, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireActivity(), AppConstant.ADD_BOOKMARK_FAILED, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeBookmark(AudiosListAdapter adapter, Audio audio, int position, ExoPlayer player) {
        audioHelper.removeBookmark(requireActivity(), audio, deletedRow -> {
            if (deletedRow > 0) {
                Toast.makeText(requireActivity(), AppConstant.REMOVE_BOOKMARK_SUCCESSFULLY, Toast.LENGTH_SHORT).show();
                if (isBookmarksList) {
                    wasRemovedBookmark = true;
                    initTopAppBarUI(true);
                    if (adapter.status != AppConstant.NONE) {
                        if (player.getMediaItemCount() > 1 && position >= player.getCurrentMediaItemIndex()) {
                            bookmarksAdapterNotifyItemChanged(adapter, player, player.getCurrentMediaItemIndex());
                        } else if (player.getMediaItemCount() > 1 && position < player.getCurrentMediaItemIndex()) {
                            bookmarksAdapterNotifyItemChanged(adapter, player, player.getCurrentMediaItemIndex() - 1);
                        }
                    }
                    loadBookmarksList(player);
                }
            } else {
                Toast.makeText(requireActivity(), AppConstant.REMOVE_BOOKMARK_FAILED, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bookmarksAdapterNotifyItemChanged(AudiosListAdapter adapter, ExoPlayer player, int position) {
        adapter.setCurrentIndex(position);
        setAdapterStatusByPlayerState(player);
        adapter.notifyItemChanged(position);
    }

    private void setAdapterStatusByPlayerState(ExoPlayer player) {
        if (player.isPlaying()) {
            adapter.status = AppConstant.PLAY;
        } else {
            adapter.status = AppConstant.STOP;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (player.isPlaying()) {
            player.stop();
        }
        player.release();
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(broadcastReceiver);
    }

    private void handleMinimizeAudioLayout(int action) {
        switch (action) {
            case AppConstant.ACTION_START:
                // show minimize audio layout and audio playing info
                break;
            case AppConstant.ACTION_PAUSE:
                break;
            case AppConstant.ACTION_RESUME:
                break;
            case AppConstant.ACTION_CLEAR:
                break;
        }
    }

    private void sendActionToService(int action) {
        Intent intent = new Intent(requireActivity(), MainService.class);
        intent.putExtra("AudioActionToService", action);
        intent.putExtra("AudioInfoToService", audio);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent);
        } else {
            requireContext().startService(intent);
        }
    }
}