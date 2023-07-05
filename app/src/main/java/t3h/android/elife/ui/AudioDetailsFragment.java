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

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.Player;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import t3h.android.elife.R;
import t3h.android.elife.adapters.FragmentAdapter;
import t3h.android.elife.databinding.FragmentAudioDetailsBinding;
import t3h.android.elife.helper.AudioHelper;
import t3h.android.elife.models.Audio;
import t3h.android.elife.viewmodels.MainViewModel;

public class AudioDetailsFragment extends Fragment {
    private FragmentAudioDetailsBinding binding;
    private Audio audioInfo;
    private MainViewModel mainViewModel;
    private ExoPlayer player;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_audio_details, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e("DNV", "onViewCreated");
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
        if (requireArguments().get("audioInfo") != null) {
            audioInfo = (Audio) requireArguments().get("audioInfo");
            mainViewModel.setAudio(audioInfo);
            mainViewModel.getAudioInfo().observe(requireActivity(), audio -> {
                binding.audioTitle.setText(audio.getTitle());
            });
        }

        gettingPlayer();
    }

    private void gettingPlayer() {
        Log.e("DNV", "gettingPlayer");
        mainViewModel.getPlayer().observe(requireActivity(), livePlayer -> {
            if (livePlayer != null) {
                player = livePlayer;
                Log.e("DNV", String.valueOf(player.getMediaItemCount()));
                playerControls(player);
            }
        });
    }

    private void playerControls(ExoPlayer player) {
        // check if the player is playing
        if (player.isPlaying()) {
            Log.e("DNV", "isPlaying");
            initAudioControlLayout(player);
        } else {
            Log.e("DNV", "pause");
            player.prepare();
            player.play();
            initAudioControlLayout(player);
        }

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
                player.prepare();
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
                assert mediaItem != null;
                Log.e("DNV", (String) mediaItem.mediaMetadata.title);
                binding.audioTitle.setText(mediaItem.mediaMetadata.title);
                initAudioControlLayout(player);
                if (!player.isPlaying()) {
                    player.prepare();
                    player.play();
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    Log.e("DNV", "ExoPlayer STATE_READY");
                    binding.audioTitle.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    initAudioControlLayout(player);
                } else {
                    binding.playOrPauseIcon.setImageResource(R.drawable.ic_play_circle_outline);
                }
            }
        });
    }

    private void initAudioControlLayout(ExoPlayer player) {
        Log.e("DNV", "initAudioControlLayout");
        binding.audioCurrentTime.setText(AudioHelper.milliSecondsToTimer((int) player.getCurrentPosition()));
        binding.seekBar.setProgress((int) player.getCurrentPosition());
        binding.audioDuration.setText(AudioHelper.milliSecondsToTimer((int) player.getDuration()));
        binding.seekBar.setMax((int) player.getDuration());
        binding.playOrPauseIcon.setImageResource(R.drawable.ic_pause_circle_outline);
        updatePlayerPositionProgress(player);
    }

    private void updatePlayerPositionProgress(ExoPlayer player) {
        Log.e("DNV", "updatePlayerPositionProgress");
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
            requireActivity().onBackPressed();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        binding = null;
    }
}