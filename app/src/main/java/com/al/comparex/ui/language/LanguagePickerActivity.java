package com.al.comparex.ui.language;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.al.comparex.databinding.ActivityLanguagePickerBinding;
import com.al.comparex.ui.spek.SpekInputActivity;
import com.al.comparex.utils.LangPrefs;

/**
 * Ditampilkan hanya sekali, saat user pertama kali membuka aplikasi —
 * sebelum SpekInputActivity. Setelah bahasa dipilih, lanjut ke pengisian spek.
 */
public class LanguagePickerActivity extends AppCompatActivity {

    private ActivityLanguagePickerBinding binding;
    private String selectedLang = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLanguagePickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.cardIndonesian.setOnClickListener(v -> selectLang(LangPrefs.LANG_ID));
        binding.cardEnglish.setOnClickListener(v -> selectLang(LangPrefs.LANG_EN));

        binding.btnContinue.setOnClickListener(v -> {
            if (selectedLang == null) return;
            LangPrefs.saveLang(this, selectedLang);
            startActivity(new Intent(this, SpekInputActivity.class));
            finish();
        });
    }

    private void selectLang(String lang) {
        selectedLang = lang;
        boolean isId = LangPrefs.LANG_ID.equals(lang);

        binding.radioIndonesian.setChecked(isId);
        binding.radioEnglish.setChecked(!isId);

        binding.cardIndonesian.setStrokeColor(
                getColorRes(isId ? com.al.comparex.R.color.accent : com.al.comparex.R.color.surface_variant));
        binding.cardEnglish.setStrokeColor(
                getColorRes(!isId ? com.al.comparex.R.color.accent : com.al.comparex.R.color.surface_variant));

        binding.btnContinue.setEnabled(true);
    }

    private int getColorRes(int resId) {
        return androidx.core.content.ContextCompat.getColor(this, resId);
    }
}
