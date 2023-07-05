package t3h.android.elife.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import t3h.android.elife.R;
import t3h.android.elife.databinding.FragmentLyricsBinding;
import t3h.android.elife.viewmodels.MainViewModel;

public class LyricsFragment extends Fragment {
    private FragmentLyricsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_lyrics, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainViewModel mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mainViewModel.getAudioInfo().observe(requireActivity(), audio -> {
            binding.audioLyrics.setText(audio.getLyrics());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}