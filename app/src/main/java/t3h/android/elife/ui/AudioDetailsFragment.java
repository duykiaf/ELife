package t3h.android.elife.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import t3h.android.elife.R;
import t3h.android.elife.adapters.FragmentAdapter;
import t3h.android.elife.databinding.FragmentAudioDetailsBinding;

public class AudioDetailsFragment extends Fragment {
    private FragmentAudioDetailsBinding binding;

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

//        binding.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                super.onPageSelected(position);
//                switch (position) {
//                    case 0:
//                        break;
//                    case 1:
//                        break;
//                }
//            }
//        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}