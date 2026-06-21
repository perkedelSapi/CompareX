package com.al.comparex.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.al.comparex.databinding.ActivitySplashBinding;
import com.al.comparex.ui.language.LanguagePickerActivity;
import com.al.comparex.ui.main.MainActivity;
import com.al.comparex.ui.spek.SpekInputActivity;
import com.al.comparex.utils.LangPrefs;
import com.al.comparex.utils.SpekPrefs;

/**
 * Entry point of the app.
 * Shows a branded splash for 1.5 s, then routes to:
 *   - LanguagePickerActivity if user has never chosen a language (first launch)
 *   - SpekInputActivity      if specs have never been saved
 *   - MainActivity            if specs are already stored
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Class<?> destination;
            if (!LangPrefs.hasChosenLanguage(this)) {
                destination = LanguagePickerActivity.class;
            } else if (SpekPrefs.isSpekSet(this)) {
                destination = MainActivity.class;
            } else {
                destination = SpekInputActivity.class;
            }

            startActivity(new Intent(this, destination));
            finish();
        }, SPLASH_DELAY_MS);
    }
}
