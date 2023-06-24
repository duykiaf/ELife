package t3h.android.elife.ui;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
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
import androidx.recyclerview.widget.LinearLayoutManager;

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

public class AudiosListFragment extends Fragment {
    private FragmentAudiosListBinding binding;
    private boolean isPlaying = false;
    private int iconColor = 0;
    private String imgContentDesc;
    private int imageResource = 0;
    private int audioBgResource = 0;
    private boolean isBookmarksList = false;
    private boolean isSearching = false;
    private Topic topicInfo;
    private AudiosListAdapter adapter = new AudiosListAdapter();
    private final MainRepository mainRepository = new MainRepository();
    private final AudioHelper audioHelper = new AudioHelper();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_audios_list, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isBookmarksList = requireArguments().getBoolean("isBookmarksList");
        if (!isBookmarksList) {
            topicInfo = (Topic) requireArguments().get("topicInfo");
            initTopAppBarUI(false);
            initAudiosList();
        } else {
            initTopAppBarUI(true);
            loadBookmarksList();
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
        onMenuItemSelected();
        onItemSelected(adapter);
        binding.goToTopImageView.setOnClickListener(v -> binding.audiosRcv.smoothScrollToPosition(0));
    }

    private void initAudiosList() {
        MainRepository mainRepoForDao = new MainRepository(requireActivity().getApplication());
        mainRepoForDao.getAudioIds().observe(requireActivity(), adapter::setBookmarkAudioIds);
        mainRepository.getActiveAudiosListByTopicId(topicInfo.getId()).observe(requireActivity(), audioList -> {
            if (audioList != null && audioList.size() > 0) {
                binding.noDataTxt.setVisibility(View.GONE);
                setBtnState(true);
                adapter.setAudioList(audioList);
            } else {
                binding.noDataTxt.setVisibility(View.VISIBLE);
                setBtnState(false);
            }
        });
        initRcv(adapter);
    }

    private void onBackPressed() {
        if (!binding.appBarFragment.topAppBar.getTitle().equals(AppConstant.TOPICS)) {
            binding.appBarFragment.topAppBar.setNavigationOnClickListener(v -> {
                requireActivity().onBackPressed();
            });
        }
    }

    private void onMenuItemSelected() {
        binding.appBarFragment.topAppBar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.searchItem:
                    if (binding.searchEdt.getVisibility() == View.VISIBLE) {
                        setBtnState(true);
                        resetSearchFeature();
                    } else {
                        isSearching = true;
                        setSearchLayoutState(true);
                        binding.searchEdt.requestFocus();
                        setBtnState(false);
                        if (!isBookmarksList) {
                            loadSearchList(adapter);
                        }
                    }
                    return true;
                case R.id.bookmarksItem:
                    if (!isBookmarksList) {
                        initTopAppBarUI(true);
                        isBookmarksList = true;
                        resetSearchFeature();
                    }
                    return true;
            }
            return false;
        });
    }

    private void loadBookmarksList() {
        AudiosListAdapter adapter = new AudiosListAdapter();
        MainRepository mainRepoForDao = new MainRepository(requireActivity().getApplication());
        mainRepoForDao.getAudioIds().observe(requireActivity(), adapter::setBookmarkAudioIds);
        mainRepoForDao.getBookmarksList().observe(requireActivity(), audioList -> {
            if (audioList != null && audioList.size() > 0) {
                if (binding != null) {
                    binding.noDataTxt.setVisibility(View.GONE);
                    if (isSearching) {
                        binding.searchEdt.setText("");
                        setBtnState(false);
                    } else {
                        setBtnState(true);
                    }
                }
                adapter.setAudioList(audioList);
            } else {
                if (binding != null) {
                    binding.noDataTxt.setVisibility(View.VISIBLE);
                    setBtnState(false);
                }
            }
        });
        initRcv(adapter);
        loadSearchList(adapter);
        onItemSelected(adapter);
    }

    private void initRcv(AudiosListAdapter adapter) {
        binding.audiosRcv.setAdapter(adapter);
        binding.audiosRcv.setHasFixedSize(true);
        binding.audiosRcv.setLayoutManager(new LinearLayoutManager(requireActivity()));
    }

    private MainService mainService;
    private MainService.AudioBinder audioBinder;
    private boolean isServiceConnection;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e("DNV", "onServiceConnected");
            audioBinder = (MainService.AudioBinder) iBinder;
            audioBinder.getAudioList().observe(requireActivity(), audioList -> {
                if (audioBinder.getAudioController().isPlaying()) {
                    adapter.status = AppConstant.PLAY;
                } else {
                    adapter.status = AppConstant.STOP;
                }
                adapter.setCurrentIndex(audioBinder.getAudioController().getCurrentIndex());
            });
            mainService = audioBinder.getMainService();
            isServiceConnection = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("DNV", "onServiceDisconnected");
            mainService = null;
            isServiceConnection = false;
        }
    };

    private void clickStartService() {
        Log.e("DNV", "clickStartService");
        Intent intent = new Intent(requireActivity(), MainService.class);
        Bundle bundle = new Bundle();
        bundle.putInt("TopicId", topicInfo.getId());
        intent.putExtras(bundle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(intent);
        }
        requireContext().bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
    }

    private void clickStopService() {
        Intent intent = new Intent(requireActivity(), MainService.class);
        requireContext().stopService(intent);
        if (isServiceConnection) {
            requireContext().unbindService(serviceConnection);
            isServiceConnection = false;
        }
    }

    private void onItemSelected(AudiosListAdapter adapter) {
        adapter.setOnAudioClickListener(new AudiosListAdapter.OnAudioClickListener() {
            @Override
            public void onItemClick(Audio audio, int position) {
                clickStartService();
                if (position == audioBinder.getAudioController().getCurrentIndex()) {
                    if (audioBinder.getAudioController().isPlaying()) {
                        // pause audio
                        isPlaying = false;
                        adapter.status = AppConstant.STOP;
                    } else {
                        // play audio
                        isPlaying = true;
                        adapter.status = AppConstant.PLAY;
                    }
                } else {
                    // stop old audio then start new audio
                    isPlaying = true;
                    adapter.status = AppConstant.PLAY;
                    adapter.setCurrentIndex(position);
                    adapter.notifyItemChanged(audioBinder.getAudioController().getCurrentIndex());
                    audioBinder.getMainService().playAudioAt(position);
                }
            }

            @Override
            public void onIconClick(AudioItemLayoutBinding binding, ImageView imageView, Audio audio, int position) {
                switch (imageView.getId()) {
                    case R.id.playIcon:
                        clickStartService();
                        if (position == audioBinder.getAudioController().getCurrentIndex()) {
                            if (audioBinder.getAudioController().isPlaying()) {
                                // pause audio
                                isPlaying = false;
                                adapter.status = AppConstant.STOP;
                            } else {
                                // play audio
                                isPlaying = true;
                                adapter.status = AppConstant.PLAY;
                            }
                        } else {
                            // stop old audio then start new audio
                            isPlaying = true;
                            adapter.status = AppConstant.PLAY;
                            adapter.setCurrentIndex(position);
                            adapter.notifyItemChanged(audioBinder.getAudioController().getCurrentIndex());
                            audioBinder.getMainService().playAudioAt(position);
                        }

                        if (imageView.getContentDescription().equals(AppConstant.PLAY_ICON)) {
                            isPlaying = true;
                            imgContentDesc = AppConstant.PAUSE_ICON;
                            iconColor = getResources().getColor(R.color.white);
                            imageResource = R.drawable.ic_pause_circle_outline;
                            audioBgResource = R.drawable.blue_btn_background;
                            setAudioTextColor(binding, iconColor);
                        }
                        if (imageView.getContentDescription().equals(AppConstant.PAUSE_ICON)) {
                            isPlaying = false;
                            imgContentDesc = AppConstant.PLAY_ICON;
                            iconColor = getResources().getColor(R.color.primaryColor);
                            imageResource = R.drawable.ic_play_circle_outline;
                            audioBgResource = R.drawable.card_background;
                            setAudioTextColor(binding, getResources().getColor(R.color.black));
                        }
                        imageView.setContentDescription(imgContentDesc);
                        binding.playIcon.setColorFilter(iconColor);
                        binding.bookmarkIcon.setColorFilter(iconColor);
                        imageView.setImageResource(imageResource);
                        binding.audioItemLayout.setBackgroundResource(audioBgResource);
                        break;
                    case R.id.bookmarkIcon:
                        if (isPlaying) {
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
                            removeBookmark(audio);
                        }
                        imageView.setContentDescription(imgContentDesc);
                        imageView.setImageResource(imageResource);
                        break;
                }
            }
        });
    }

    private void setAudioTextColor(AudioItemLayoutBinding audioItemLayoutBinding, int colorResource) {
        audioItemLayoutBinding.audioTitle.setTextColor(colorResource);
        audioItemLayoutBinding.durationTxt.setTextColor(colorResource);
    }

    private void loadSearchList(AudiosListAdapter adapter) {
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
        binding.closeSearchLayout.setOnClickListener(v -> resetSearchFeature());
    }

    private void resetSearchFeature() {
        isSearching = false;
        setSearchLayoutState(false);
        binding.searchEdt.setText("");
        if (isBookmarksList) {
            loadBookmarksList();
        } else {
            initAudiosList();
        }
    }

    private void setBtnState(boolean isVisible) {
        binding.playAllBtn.setVisibility(getState(isVisible));
        binding.repeatSettingBtn.setVisibility(getState(isVisible));
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

    private void removeBookmark(Audio audio) {
        audioHelper.removeBookmark(requireActivity(), audio, deletedRow -> {
            if (deletedRow > 0) {
                Toast.makeText(requireActivity(), AppConstant.REMOVE_BOOKMARK_SUCCESSFULLY, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireActivity(), AppConstant.REMOVE_BOOKMARK_FAILED, Toast.LENGTH_SHORT).show();
            }
            if (isBookmarksList) {
                initTopAppBarUI(true);
                loadBookmarksList();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}