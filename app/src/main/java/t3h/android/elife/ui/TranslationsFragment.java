package t3h.android.elife.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.List;
import java.util.Set;

import t3h.android.elife.R;
import t3h.android.elife.databinding.FragmentTranslationsBinding;
import t3h.android.elife.helper.AppConstant;
import t3h.android.elife.helper.LanguagesDropdownHelper;
import t3h.android.elife.models.DropdownItem;
import t3h.android.elife.viewmodels.MainViewModel;

public class TranslationsFragment extends Fragment {
    private FragmentTranslationsBinding binding;
    private MainViewModel mainViewModel;
    private RemoteModelManager modelManager;
    private int getSelectedItemPosition;
    private String targetLanguage = "";
    private List<DropdownItem> languagesDropdown = LanguagesDropdownHelper.languagesDropdown();
    private TranslatorOptions.Builder optionsBuilder;
    private TranslatorOptions options;
    private Translator translator;
    private int getTranslationModelsCount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_translations, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        modelManager = RemoteModelManager.getInstance();
        optionsBuilder = new TranslatorOptions.Builder().setSourceLanguage(TranslateLanguage.ENGLISH);
        getTranslationModelsStoredOnTheDevice();
    }

    private void getTranslationModelsStoredOnTheDevice() {
        modelManager.getDownloadedModels(TranslateRemoteModel.class)
                .addOnSuccessListener(translateRemoteModels -> {
                    getTranslationModelsCount = translateRemoteModels.size();
                    if (getTranslationModelsCount == 2) {
                        translate(translateRemoteModels);
                        initLanguagesDropdown();
                        showSelectedTranslateLanguage(translateRemoteModels);
                    } else {
                        binding.lyricsTranslation.setText(AppConstant.CHOOSE_TRANSLATE_LANGUAGE);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(requireActivity(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show());
    }

    private void translate(Set<TranslateRemoteModel> translateRemoteModels) {
        for (TranslateRemoteModel model : translateRemoteModels) {
            if (!model.getLanguage().equalsIgnoreCase(TranslateLanguage.ENGLISH)) {
                targetLanguage = model.getLanguage();
            }
        }

        options = optionsBuilder.setTargetLanguage(targetLanguage).build();
        translator = Translation.getClient(options);
        getLifecycle().addObserver(translator);

        initLyricsTranslation(translator);
    }

    private void showSelectedTranslateLanguage(Set<TranslateRemoteModel> translateRemoteModels) {
        for (TranslateRemoteModel model : translateRemoteModels) {
            if (!model.getLanguage().equalsIgnoreCase(TranslateLanguage.ENGLISH)) {
                targetLanguage = model.getLanguage();
                break;
            }
        }
        DropdownItem selectedItem = null;
        for (DropdownItem item : languagesDropdown) {
            if (item.getHiddenValue().equalsIgnoreCase(targetLanguage)) {
                selectedItem = item;
                break;
            }
        }
        if (selectedItem != null) {
            getSelectedItemPosition = languagesDropdown.indexOf(selectedItem);
            binding.languagesSpinner.setSelection(getSelectedItemPosition);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        selectTranslateLanguage();
    }

    private void selectTranslateLanguage() {
        initLanguagesDropdown();
        binding.languagesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DropdownItem item = (DropdownItem) adapterView.getSelectedItem();
                targetLanguage = item.getHiddenValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        applySelectedTranslateLanguage();
    }

    private void applySelectedTranslateLanguage() {
        binding.applyBtn.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            modelManager.getDownloadedModels(TranslateRemoteModel.class)
                    .addOnSuccessListener(this::initTranslateLanguage)
                    .addOnFailureListener(e -> Toast.makeText(requireActivity(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show());
        });
    }

    private void initTranslateLanguage(Set<TranslateRemoteModel> translateRemoteModels) {
        if (translateRemoteModels.size() == 2) {
            if (targetLanguage.isEmpty()) {
                errorNotification();
            } else {
                for (TranslateRemoteModel item : translateRemoteModels) {
                    if (!item.getLanguage().equalsIgnoreCase(TranslateLanguage.ENGLISH) &&
                            !item.getLanguage().equalsIgnoreCase(targetLanguage)) {
                        // delete old target language model
                        deleteOldTargetLanguage(item.getLanguage());
                        // download new target language model and re-translation
                        downloadNewTargetLanguage(targetLanguage);
                    } else if (item.getLanguage().equalsIgnoreCase(targetLanguage)) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                }
            }
        } else {
            if (targetLanguage.isEmpty()) {
                errorNotification();
            } else {
                downloadTranslateModelsIfNeeded(targetLanguage);
            }
        }
    }

    private void errorNotification() {
        binding.progressBar.setVisibility(View.GONE);
        Toast.makeText(requireActivity(), AppConstant.CHOOSE_TRANSLATE_LANGUAGE, Toast.LENGTH_SHORT).show();
    }

    private void initLanguagesDropdown() {
        ArrayAdapter<DropdownItem> arrayAdapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_spinner_item, languagesDropdown);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.languagesSpinner.setAdapter(arrayAdapter);
    }

    private void downloadTranslateModelsIfNeeded(String targetLanguageSelected) {
        options = optionsBuilder.setTargetLanguage(targetLanguageSelected).build();
        translator = Translation.getClient(options);
        getLifecycle().addObserver(translator);

        DownloadConditions conditions = new DownloadConditions.Builder().requireWifi().build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(v -> initLyricsTranslation(translator))
                .addOnFailureListener(e -> Toast.makeText(requireActivity(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show());
    }

    private void initLyricsTranslation(Translator translator) {
        mainViewModel.getAudioLyrics().observe(requireActivity(), lyrics -> {
            Task<String> test = translator.translate(lyrics)
                    .addOnSuccessListener(result -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.lyricsTranslation.setText(result);
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireActivity(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show());
        });
    }

    private void deleteOldTargetLanguage(String code) {
        TranslateRemoteModel oldTargetLanguageModel = new TranslateRemoteModel.Builder(code).build();
        modelManager.deleteDownloadedModel(oldTargetLanguageModel)
                .addOnSuccessListener(unused -> binding.progressBar.setVisibility(View.GONE))
                .addOnFailureListener(e -> Toast.makeText(requireActivity(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show());
    }

    private void downloadNewTargetLanguage(String code) {
        TranslateRemoteModel newTargetLanguageModel = new TranslateRemoteModel.Builder(code).build();
        DownloadConditions conditions = new DownloadConditions.Builder().requireWifi().build();
        modelManager.download(newTargetLanguageModel, conditions)
                .addOnSuccessListener(listener -> downloadTranslateModelsIfNeeded(code)) // re-translation
                .addOnFailureListener(e -> Toast.makeText(requireActivity(), AppConstant.SYSTEM_ERROR, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}