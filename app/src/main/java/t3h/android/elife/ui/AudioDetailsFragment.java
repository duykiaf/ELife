package t3h.android.elife.ui;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import t3h.android.elife.R;
import t3h.android.elife.adapters.FragmentAdapter;
import t3h.android.elife.databinding.FragmentAudioDetailsBinding;
import t3h.android.elife.helper.AudioHelper;
import t3h.android.elife.viewmodels.MainViewModel;

public class AudioDetailsFragment extends Fragment {
    private FragmentAudioDetailsBinding binding;
    private MainViewModel mainViewModel;
    private ExoPlayer player;
    private boolean isTheFirstOpenTime = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_audio_details, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new LyricsFragment());
        fragmentList.add(new TranslationsFragment());

        FragmentAdapter fragmentAdapter = new FragmentAdapter(requireActivity(), fragmentList);
        binding.pager.setAdapter(fragmentAdapter);

        String[] tabLayoutNames = {"Lyrics", "Translations"};
        new TabLayoutMediator(binding.tabLayout, binding.pager,
                (tab, position) -> tab.setText(tabLayoutNames[position])
        ).attach();

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        gettingPlayer();
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
                    Log.e("DNV", "onMediaItemTransition-details");
                    binding.audioTitle.setText(mediaItem.mediaMetadata.title);
                    mainViewModel.setAudioLyrics((String) mediaItem.mediaMetadata.description);
                    initAudioControlLayout(player);
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    Log.e("DNV", "onPlaybackStateChanged-details");
                    binding.audioTitle.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    mainViewModel.setAudioLyrics((String) player.getCurrentMediaItem().mediaMetadata.description);
                    initAudioControlLayout(player);
                } else if (playbackState == ExoPlayer.STATE_IDLE) {
                    Log.e("DNV", "ExoPlayer STATE_IDLE");
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
        updatePlayerPositionProgress(player);
    }

    private void setUpCurrentMediaItem(ExoPlayer player) {
        player.seekTo((Integer) requireArguments().get("audioIndex"), C.TIME_UNSET);
        player.prepare();
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
        binding.topAppBar.setNavigationOnClickListener(v -> {
            player.clearMediaItems();
            requireActivity().onBackPressed();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        binding = null;
    }
}