package t3h.android.elife.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import t3h.android.elife.R;
import t3h.android.elife.databinding.FragmentAppBarBinding;

public class AppBarFragment extends Fragment {
    private FragmentAppBarBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_app_bar, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = requireArguments();
        binding.topAppBar.setTitle(args.getString("PAGE_TITLE", "No page found"));
        binding.topAppBar.setNavigationIcon(args.getInt("PAGE_ICON"));
        //binding.topAppBar.setMenu();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}