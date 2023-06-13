package t3h.android.elife.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import t3h.android.elife.R;
import t3h.android.elife.adapters.TopicsListAdapter;
import t3h.android.elife.databinding.FragmentTopicsListBinding;
import t3h.android.elife.repositories.MainRepository;

public class TopicsListFragment extends Fragment {
    private FragmentTopicsListBinding binding;
    private final TopicsListAdapter adapter = new TopicsListAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_topics_list, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initTopicsList();
    }

    @Override
    public void onResume() {
        super.onResume();
        onMenuItemSelected();
        onTopicItemSelected();
        binding.goToTopImageView.setOnClickListener(v -> binding.topicsRcv.smoothScrollToPosition(0));
    }

    private void initTopicsList() {
        MainRepository mainRepository = new MainRepository();
        mainRepository.getActiveTopicsList().observe(requireActivity(), topics -> {
            if (topics != null) {
                adapter.setTopicList(topics);
            } else {
                binding.noDataTxt.setVisibility(View.VISIBLE);
            }
        });
        binding.topicsRcv.setAdapter(adapter);
        binding.topicsRcv.setLayoutManager(new LinearLayoutManager(requireActivity()));
    }

    private void onMenuItemSelected() {
        binding.appBarFragment.topAppBar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.searchItem:
                    if (binding.searchEdt.getVisibility() == View.VISIBLE) {
                        resetSearchFeature();
                    } else {
                        binding.searchEdt.setVisibility(View.VISIBLE);
                        binding.closeSearchLayout.setVisibility(View.VISIBLE);
                        binding.searchEdt.requestFocus();
                        loadSearchList(adapter);
                    }
                    return true;
                case R.id.bookmarksItem:
                    Toast.makeText(requireActivity(), "Bookmarks list", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        });
    }

    private void loadSearchList(TopicsListAdapter adapter) {
        binding.searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                InputFilter[] filters = new InputFilter[1];
                filters[0] = new InputFilter.LengthFilter(50);
                binding.searchEdt.setFilters(filters);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.cancelTimer();
                if (charSequence.toString().length() == 50) {
                    binding.searchEdt.setError("Keyword must not exceed 50 characters");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                adapter.searchList(editable.toString().trim().toLowerCase());
            }
        });
        binding.closeSearchLayout.setOnClickListener(v -> resetSearchFeature());
    }

    private void resetSearchFeature() {
        binding.searchEdt.setVisibility(View.GONE);
        binding.closeSearchLayout.setVisibility(View.GONE);
        binding.searchEdt.setText("");
        initTopicsList();
    }

    private void onTopicItemSelected() {
        adapter.setOnItemListClickListener(object -> {
            Toast.makeText(requireActivity(), object.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}