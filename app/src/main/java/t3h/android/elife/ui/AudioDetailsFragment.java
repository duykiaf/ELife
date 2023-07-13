package t3h.android.elife.ui;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import t3h.android.elife.R;
import t3h.android.elife.adapters.FragmentAdapter;
import t3h.android.elife.databinding.FragmentAudioDetailsBinding;
import t3h.android.elife.helper.AppConstant;
import t3h.android.elife.helper.AudioHelper;
import t3h.android.elife.models.Audio;
import t3h.android.elife.repositories.MainRepository;
import t3h.android.elife.viewmodels.MainViewModel;

public class AudioDetailsFragment extends Fragment {
    private FragmentAudioDetailsBinding binding;
    private MainViewModel mainViewModel;
    private ExoPlayer player;
    private boolean isTheFirstOpenTime = true;
    private int bookmarkStyleResource = 0;
    private String bookmarkContentDesc;
    private final AudioHelper audioHelper = new AudioHelper();
    private MainRepository mainRepoForDao;
    private AlertDialog speedSettingDialog;
    private String audioSpeedStr;
    private float audioSpeedValue;
    private PlaybackParameters playbackParameters;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_audio_details, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewPager();
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mainRepoForDao = new MainRepository(requireActivity().getApplication());
        gettingPlayer();
        binding.audioSpeed.setOnClickListener(v -> showSpeedSettingDialog(view));
    }

    private void initViewPager() {
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new LyricsFragment());
        fragmentList.add(new TranslationsFragment());

        FragmentAdapter fragmentAdapter = new FragmentAdapter(requireActivity(), fragmentList);
        binding.pager.setAdapter(fragmentAdapter);

        String[] tabLayoutNames = {"Lyrics", "Translations"};
        new TabLayoutMediator(binding.tabLayout, binding.pager,
                (tab, position) -> tab.setText(tabLayoutNames[position])
        ).attach();
    }

    private void gettingPlayer() {
        mainViewModel.getPlayer().observe(requireActivity(), livePlayer -> {
            if (livePlayer != null) {
                player = livePlayer;
                playerControls(player);
            }
        });
    }

    private void playerControls(ExoPlayer player) {
        if (isAdded()) {
            mainViewModel.getRepeatModeOne().observe(requireActivity(), repeatModeOne -> {
                if (repeatModeOne) {
                    player.setRepeatMode(Player.REPEAT_MODE_ONE);
                }
            });
        }

        initAudioControlLayout(player);

        binding.previousIcon.setOnClickListener(v -> {
            if (player.hasPreviousMediaItem()) {
                player.seekToPreviousMediaItem();
                updatePlayerPositionProgress(player);
            }
        });

        binding.nextIcon.setOnClickListener(v -> {
            if (player.hasNextMediaItem()) {
                player.seekToNextMediaItem();
                updatePlayerPositionProgress(player);
            }
        });

        binding.playOrPauseIcon.setOnClickListener(v -> {
            if (player.isPlaying()) {
                player.pause();
                binding.playOrPauseIcon.setImageResource(R.drawable.ic_play_circle_outline);
            } else {
                player.play();
                binding.playOrPauseIcon.setImageResource(R.drawable.ic_pause_circle_outline);
            }
        });

        // set seek bar changed listener
        onSeekBarChangedListener(player);

        // player listener
        playerListener(player);
    }

    private void playerListener(ExoPlayer player) {
        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                if (mediaItem != null) {
                    binding.audioTitle.setText(mediaItem.mediaMetadata.title);
                    mainViewModel.setAudioLyrics((String) mediaItem.mediaMetadata.description);
                    initAudioControlLayout(player);
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    binding.audioTitle.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    mainViewModel.setAudioLyrics((String) player.getCurrentMediaItem().mediaMetadata.description);
                    initAudioControlLayout(player);
                } else if (playbackState == ExoPlayer.STATE_IDLE) {
                    Log.e("DNV", "STATE_IDLE");
                    binding.playOrPauseIcon.setImageResource(R.drawable.ic_play_circle_outline);
                } else {
                    binding.playOrPauseIcon.setImageResource(R.drawable.ic_play_circle_outline);
                }
            }
        });
    }

    private void initAudioControlLayout(ExoPlayer player) {
        if (!player.isPlaying()) {
            if (isTheFirstOpenTime) {
                isTheFirstOpenTime = false;
                setUpCurrentMediaItem(player);
            }
            player.play();
        } else {
            if (isTheFirstOpenTime) {
                isTheFirstOpenTime = false;
                if (requireArguments().get("playAnotherAudio") != null && requireArguments().getBoolean("playAnotherAudio")) {
                    setUpCurrentMediaItem(player);
                    player.play();
                }
                binding.audioTitle.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                mainViewModel.setAudioLyrics((String) player.getCurrentMediaItem().mediaMetadata.description);
            }
        }
        binding.audioCurrentTime.setText(AudioHelper.milliSecondsToTimer((int) player.getCurrentPosition()));
        binding.seekBar.setProgress((int) player.getCurrentPosition());
        binding.audioDuration.setText(AudioHelper.milliSecondsToTimer((int) player.getDuration()));
        binding.seekBar.setMax((int) player.getDuration());
        binding.playOrPauseIcon.setImageResource(R.drawable.ic_pause_circle_outline);
        if (isAdded()) {
            initBookmarkIcon(player);
        }
        updatePlayerPositionProgress(player);
    }

    private void setUpCurrentMediaItem(ExoPlayer player) {
        player.seekTo((Integer) requireArguments().get("audioIndex"), C.TIME_UNSET);
        player.prepare();
    }

    private void initBookmarkIcon(ExoPlayer player) {
        checkBookmarkStatus(getCurrentAudioId(player));
        setBookmark(player);
    }

    private void checkBookmarkStatus(int audioSelectedId) {
        if (isAdded()) {
            // set default icon info
            binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark_border);
            binding.bookmarkIcon.setContentDescription(AppConstant.BOOKMARK_BORDER_ICON);

            // check icon status
            mainRepoForDao.getAudioIds().observe(requireActivity(), bookmarkIds -> {
                for (Integer id : bookmarkIds) {
                    if (audioSelectedId == id) {
                        binding.bookmarkIcon.setImageResource(R.drawable.ic_bookmark);
                        binding.bookmarkIcon.setContentDescription(AppConstant.BOOKMARK_ICON);
                        break;
                    }
                }
            });
        }
    }

    private int getCurrentAudioId(ExoPlayer player) {
        return (int) (player.getCurrentMediaItem().mediaMetadata.extras).get("audioId");
    }

    private void updatePlayerPositionProgress(ExoPlayer player) {
        new Handler().postDelayed(() -> {
            if (player.isPlaying()) {
                binding.audioCurrentTime.setText(AudioHelper.milliSecondsToTimer((int) player.getCurrentPosition()));
                binding.seekBar.setProgress((int) player.getCurrentPosition());
            }
            updatePlayerPositionProgress(player);
        }, 1000);
    }

    private void onSeekBarChangedListener(ExoPlayer player) {
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressValue = seekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                binding.seekBar.setProgress(progressValue);
                binding.audioCurrentTime.setText(AudioHelper.milliSecondsToTimer(progressValue));
                player.seekTo(progressValue);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            onBackPressed();
        }
    }

    private void onBackPressed() {
        binding.topAppBar.setNavigationOnClickListener(v -> {
            mainViewModel.setRepeatModeOne(false);
            mainViewModel.setRepeatModeAll(true);
            player.clearMediaItems();
            requireActivity().onBackPressed();
        });
    }

    private void showSpeedSettingDialog(View v) {
        if (speedSettingDialog == null) {
            View view = LayoutInflater.from(requireActivity()).inflate(
                    R.layout.playback_speed_dialog, v.findViewById(R.id.playbackSpeedDialog)
            );
            initSpeedSettingDialog(view);
            RadioGroup radioGroup = view.findViewById(R.id.playbackSpeedRG);
            setRadioCheckedDefault(view);
            onCheckedRadioBtnChangeListener(view, radioGroup);
            view.findViewById(R.id.closeBtn).setOnClickListener(s -> speedSettingDialog.dismiss());
        }
        speedSettingDialog.show();
    }

    private void initSpeedSettingDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);
        speedSettingDialog = builder.create();
        if (speedSettingDialog.getWindow() != null) {
            speedSettingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
    }

    private void setRadioCheckedDefault(View view) {
        RadioButton radioCheckedDefault = view.findViewById(R.id.radioCheckedDefault);
        radioCheckedDefault.setChecked(true);
    }

    private void onCheckedRadioBtnChangeListener(View view, RadioGroup radioGroup) {
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton radioButton = view.findViewById(checkedId);
            audioSpeedStr = radioButton.getText().toString();
            switch (audioSpeedStr) {
                case "0.5x":
                    audioSpeedValue = 0.5f;
                    break;
                case "0.75x":
                    audioSpeedValue = 0.75f;
                    break;
                case "1x":
                    audioSpeedValue = 1f;
                    break;
                case "1.25x":
                    audioSpeedValue = 1.25f;
                    break;
                case "1.5x":
                    audioSpeedValue = 1.5f;
                    break;
            }
            playbackParameters = new PlaybackParameters(audioSpeedValue);
            binding.audioSpeed.setText(radioButton.getText().toString());
            player.setPlaybackParameters(playbackParameters);
        });
    }

    private void setBookmark(ExoPlayer player) {
        MainRepository mainRepository = new MainRepository();
        binding.bookmarkIcon.setOnClickListener(v -> {
            if (binding.bookmarkIcon.getContentDescription().equals(AppConstant.BOOKMARK_BORDER_ICON)) {
                bookmarkStyleResource = R.drawable.ic_bookmark;
                bookmarkContentDesc = AppConstant.BOOKMARK_ICON;
                // add bookmark here
                mainRepository.getAudioById(getCurrentAudioId(player)).observe(requireActivity(), result -> {
                    if (result != null && result.size() > 0) {
                        addBookmark(result.get(0));
                    }
                });
            } else {
                bookmarkStyleResource = R.drawable.ic_bookmark_border;
                bookmarkContentDesc = AppConstant.BOOKMARK_BORDER_ICON;
                // remove bookmark here
                mainRepository.getAudioById(getCurrentAudioId(player)).observe(requireActivity(), result -> {
                    if (result != null && result.size() > 0) {
                        removeBookmark(result.get(0), player);
                    }
                });
            }
            binding.bookmarkIcon.setImageResource(bookmarkStyleResource);
            binding.bookmarkIcon.setContentDescription(bookmarkContentDesc);
            binding.bookmarkIcon.setColorFilter(getResources().getColor(R.color.primaryColor), PorterDuff.Mode.MULTIPLY);
        });
    }

    private void addBookmark(Audio audioSelected) {
        audioHelper.addBookmark(requireActivity(), audioSelected, audioId -> {
            if (audioId > 0) {
                Toast.makeText(requireActivity(), AppConstant.ADD_BOOKMARK_SUCCESSFULLY, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireActivity(), AppConstant.ADD_BOOKMARK_FAILED, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeBookmark(Audio audioSelected, ExoPlayer player) {
        audioHelper.removeBookmark(requireActivity(), audioSelected, deletedRow -> {
            if (deletedRow > 0) {
                Toast.makeText(requireActivity(), AppConstant.REMOVE_BOOKMARK_SUCCESSFULLY, Toast.LENGTH_SHORT).show();
                initBookmarkIcon(player);
                if (requireArguments().getBoolean("isBookmarksList")) {
                    // check if audioIds null, back pressed bookmarks list screen
                    checkBookmarksList();
                }
            } else {
                Toast.makeText(requireActivity(), AppConstant.REMOVE_BOOKMARK_FAILED, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkBookmarksList() {
        mainRepoForDao.getBookmarksList().observe(requireActivity(), list -> {
            if (list.isEmpty() && isAdded()) {
                requireActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        binding = null;
    }
}