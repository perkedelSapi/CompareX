package com.al.comparex.ui.spek;

import android.content.Context;
import android.content.Intent;
import java.util.Arrays;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.al.comparex.R;
import com.al.comparex.data.model.SpekUser;
import com.al.comparex.databinding.ActivitySpekInputBinding;
import com.al.comparex.ui.main.MainActivity;
import com.al.comparex.utils.FuzzySearchAdapter;
import com.al.comparex.utils.HardwareData;
import com.al.comparex.utils.LangPrefs;
import com.al.comparex.utils.SpekPrefs;


public class SpekInputActivity extends AppCompatActivity {

    private ActivitySpekInputBinding binding;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LangPrefs.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpekInputBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(getString(R.string.title_spek_input));

        setupAdapters();
        setupRamChips();
        restoreIfExists();

        binding.btnSaveSpek.setOnClickListener(v -> onSaveClicked());
    }

    // ── Fuzzy adapters ────────────────────────────────────────────────────────

    private void setupAdapters() {
        FuzzySearchAdapter cpuAdapter = new FuzzySearchAdapter(
                this, Arrays.asList(HardwareData.getCpuNames()));
        FuzzySearchAdapter gpuAdapter = new FuzzySearchAdapter(
                this, Arrays.asList(HardwareData.getGpuNames()));

        binding.actvCpu.setAdapter(cpuAdapter);
        binding.actvCpu.setThreshold(1);
        binding.actvGpu.setAdapter(gpuAdapter);
        binding.actvGpu.setThreshold(1);

        binding.actvCpu.setOnClickListener(v -> binding.actvCpu.showDropDown());
        binding.actvGpu.setOnClickListener(v -> binding.actvGpu.showDropDown());

        // CPU change listener
        binding.actvCpu.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c){}
            @Override public void afterTextChanged(Editable e) {}
        });

        // GPU change → auto-fill VRAM
        binding.actvGpu.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int b,int c) {
                autoFillVram(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable e) {}
        });
    }

    // ── Auto-fill VRAM when GPU is selected ──────────────────────────────────

    private void autoFillVram(String gpuName) {
        if (gpuName.isEmpty()) return;
        int vram = HardwareData.getVramForGpu(gpuName);
        if (vram >= 0) {
            binding.etVram.setText(vram == 0 ? "0" : String.valueOf(vram));
        }
    }

    private void setupRamChips() {
        binding.chip4gb.setOnClickListener(v  -> binding.etRam.setText("4"));
        binding.chip8gb.setOnClickListener(v  -> binding.etRam.setText("8"));
        binding.chip16gb.setOnClickListener(v -> binding.etRam.setText("16"));
        binding.chip32gb.setOnClickListener(v -> binding.etRam.setText("32"));
        binding.chip64gb.setOnClickListener(v -> binding.etRam.setText("64"));
    }

    // ── Restore saved spek ────────────────────────────────────────────────────

    private void restoreIfExists() {
        SpekUser saved = SpekPrefs.loadSpek(this);
        if (saved != null) {
            binding.actvCpu.setText(saved.getCpuName(), false);
            binding.actvGpu.setText(saved.getGpuName(), false);
            if (saved.getVramGb() >= 0)
                binding.etVram.setText(String.valueOf(saved.getVramGb()));
            binding.etRam.setText(String.valueOf(saved.getRamGb()));
        }
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    private void onSaveClicked() {
        String cpuName  = binding.actvCpu.getText().toString().trim();
        String gpuName  = binding.actvGpu.getText().toString().trim();
        String vramStr  = binding.etVram.getText() != null
                ? binding.etVram.getText().toString().trim() : "";
        String ramStr   = binding.etRam.getText().toString().trim();

        if (cpuName.isEmpty()) { binding.tilCpu.setError(getString(R.string.error_pick_cpu)); return; }
        binding.tilCpu.setError(null);
        if (gpuName.isEmpty()) { binding.tilGpu.setError(getString(R.string.error_pick_gpu)); return; }
        binding.tilGpu.setError(null);
        if (ramStr.isEmpty())  { binding.tilRam.setError(getString(R.string.error_enter_ram)); return; }
        binding.tilRam.setError(null);

        int ramGb;
        try {
            ramGb = Integer.parseInt(ramStr);
            if (ramGb <= 0 || ramGb > 512) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            binding.tilRam.setError(getString(R.string.error_invalid_ram)); return;
        }

        // VRAM: optional, default dari GPU name jika kosong
        int vramGb = vramStr.isEmpty()
                ? HardwareData.getVramForGpu(gpuName)
                : parseIntSafe(vramStr);
        if (vramGb < 0) vramGb = SpekUser.estimateVramFromScore(
                HardwareData.getGpuScore(gpuName));

        int cpuScore = HardwareData.getCpuScore(cpuName);
        int gpuScore = HardwareData.getGpuScore(gpuName);
        if (cpuScore == 0) cpuScore = 45;
        if (gpuScore == 0) gpuScore = 35;

        SpekPrefs.saveSpek(this,
                new SpekUser(cpuName, cpuScore, gpuName, gpuScore, vramGb, ramGb));
        Toast.makeText(this, getString(R.string.msg_spek_saved), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    // ── Util ──────────────────────────────────────────────────────────────────

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return -1; }
    }
}
