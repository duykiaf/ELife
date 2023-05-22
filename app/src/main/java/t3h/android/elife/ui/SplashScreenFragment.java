package t3h.android.elife.ui;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import t3h.android.elife.R;
import t3h.android.elife.databinding.FragmentSplashScreenBinding;

public class SplashScreenFragment extends Fragment {
    private FragmentSplashScreenBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_splash_screen, container, false);
        View view = binding.getRoot();

        Animation topAnim = AnimationUtils.loadAnimation(requireActivity(), R.anim.top_animation);
        Animation bottomAnim = AnimationUtils.loadAnimation(requireActivity(), R.anim.bottom_animation);
        binding.logo.setAnimation(topAnim);
        binding.appName.setAnimation(bottomAnim);
        binding.slogan.setAnimation(bottomAnim);
        binding.startBtn.setAnimation(bottomAnim);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}