package com.al.comparex.ui.compare;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.al.comparex.data.model.GameDetail;
import com.al.comparex.data.model.SpekUser;
import com.al.comparex.data.model.SteamRequirement;
import com.al.comparex.R;
import androidx.core.content.ContextCompat;
import com.al.comparex.databinding.ActivityCompareBinding;
import com.al.comparex.utils.SkeletonManager;
import com.al.comparex.utils.LangPrefs;
import com.al.comparex.utils.SpekPrefs;

public class CompareActivity extends AppCompatActivity {

    public static final String EXTRA_GAME_ID_1   = "extra_game_id_1";
    public static final String EXTRA_GAME_NAME_1 = "extra_game_name_1";
    public static final String EXTRA_GAME_ID_2   = "extra_game_id_2";
    public static final String EXTRA_GAME_NAME_2 = "extra_game_name_2";

    private ActivityCompareBinding binding;
    private SkeletonManager skeletonManager;
    private CompareViewModel viewModel;
    private SpekUser userSpek;
    private int id1, id2;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LangPrefs.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompareBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        skeletonManager = new SkeletonManager(binding.layoutSkeleton.getRoot());

        id1 = getIntent().getIntExtra(EXTRA_GAME_ID_1, -1);
        String n1  = getIntent().getStringExtra(EXTRA_GAME_NAME_1);
        id2 = getIntent().getIntExtra(EXTRA_GAME_ID_2, -1);
        String n2  = getIntent().getStringExtra(EXTRA_GAME_NAME_2);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_compare));
        }

        // Set initial game name headers
        binding.tvHeaderGame1.setText(n1 != null ? n1 : "Game 1");
        binding.tvHeaderGame2.setText(n2 != null ? n2 : "Game 2");

        // Load user spek FIRST (needed for AI card display)
        userSpek = SpekPrefs.loadSpek(this);
        showUserSpekInAiCard();

        viewModel = new ViewModelProvider(this).get(CompareViewModel.class);

        observeViewModel();
        viewModel.loadGames(id1, id2);

        // AI Analysis button
        binding.btnAiAnalysis.setOnClickListener(v -> {
            if (userSpek == null) {
                binding.tvAiResult.setText(
                        "Spesifikasi belum diset.\nKembali ke menu utama dan isi spek terlebih dahulu.");
                binding.cardAiResult.setVisibility(View.VISIBLE);
                return;
            }
            viewModel.requestAiAnalysis(userSpek);
        });

        binding.btnRetry.setOnClickListener(v -> viewModel.loadGames(id1, id2));
    }

    // -----------------------------------------------------------------------
    //  Show user spek summary in AI card
    // -----------------------------------------------------------------------

    private void showUserSpekInAiCard() {
        if (userSpek != null) {
            binding.tvUserSpekSummary.setText(
                    "CPU : " + userSpek.getCpuName() + "\n" +
                    "GPU : " + userSpek.getGpuName() + "\n" +
                    "RAM : " + userSpek.getRamGb()   + " GB");
        } else {
            binding.tvUserSpekSummary.setText("Spesifikasi belum diset");
        }
    }

    // -----------------------------------------------------------------------
    //  ViewModel observers
    // -----------------------------------------------------------------------

    private void observeViewModel() {
        // Game 1 detail
        viewModel.getDetail1().observe(this, res -> {
            if (res.isLoading()) { showLoading(); return; }
            if (res.isError())   { showError(res.getMessage()); return; }
            if (res.getData() != null) { showContent(); bindInfo(res.getData(), true); }
        });

        // Game 2 detail
        viewModel.getDetail2().observe(this, res -> {
            if (res.isLoading() || res.isError()) return;
            if (res.getData() != null) { showContent(); bindInfo(res.getData(), false); }
        });

        // Steam reqs game 1 → bind requirements
        viewModel.getSteam1().observe(this, res -> {
            if (res == null || res.isLoading()) return;
            bindRequirements(true);
        });

        // Steam reqs game 2 → bind requirements
        viewModel.getSteam2().observe(this, res -> {
            if (res == null || res.isLoading()) return;
            bindRequirements(false);
        });

        // AI loading state
        viewModel.getAiLoading().observe(this, loading -> {
            if (loading == null) return;
            binding.btnAiAnalysis.setEnabled(!loading);
            binding.progressAi.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnAiAnalysis.setText((CharSequence)(loading ? getString(R.string.msg_analyzing) : getString(R.string.btn_ai_analysis)));
            if (loading) binding.cardAiResult.setVisibility(View.GONE);
        });

        // AI analysis result
        viewModel.getAiAnalysis().observe(this, text -> {
            if (text == null || text.isEmpty()) return;
            binding.tvAiResult.setText(text);
            binding.cardAiResult.setVisibility(View.VISIBLE);
        });

        viewModel.getAiHasAnchorData().observe(this, hasAnchor -> {
            if (hasAnchor == null) return;
            binding.tvAnchorBadge.setVisibility(View.VISIBLE);
            if (hasAnchor) {
                binding.tvAnchorBadge.setText(getString(R.string.badge_anchor_data));
                binding.tvAnchorBadge.setTextColor(ContextCompat.getColor(this, R.color.metacritic_green));
                binding.tvAnchorBadge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_anchor));
            } else {
                binding.tvAnchorBadge.setText(getString(R.string.badge_estimated_data));
                binding.tvAnchorBadge.setTextColor(ContextCompat.getColor(this, R.color.metacritic_yellow));
                binding.tvAnchorBadge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_estimate));
            }
        });

        // AI error
        viewModel.getAiError().observe(this, err -> {
            if (err == null || err.isEmpty()) return;
            binding.tvAiResult.setText(err);
            binding.cardAiResult.setVisibility(View.VISIBLE);
        });

        // AI retry countdown — tampilkan di tombol
        viewModel.getAiRetryStatus().observe(this, status -> {
            if (status == null || status.isEmpty()) {
                // Restore teks tombol ke default saat tidak ada countdown
                if (Boolean.TRUE.equals(viewModel.getAiLoading().getValue())) {
                    binding.btnAiAnalysis.setText((CharSequence) getString(R.string.msg_analyzing));
                } else {
                    binding.btnAiAnalysis.setText(getString(R.string.btn_ai_analysis));
                }
                return;
            }
            // Tampilkan countdown di tombol
            binding.btnAiAnalysis.setText(status);
            binding.progressAi.setVisibility(View.VISIBLE);
        });
    }

    // -----------------------------------------------------------------------
    //  Data binding helpers
    // -----------------------------------------------------------------------

    private void bindInfo(GameDetail g, boolean isGame1) {
        if (isGame1) {
            binding.tvHeaderGame1.setText(g.getName());
            binding.tvDev1.setText(g.getDeveloperNames());
            binding.tvDate1.setText(g.getReleased());
            binding.tvMeta1.setText(g.getMetacritic() != null && g.getMetacritic() > 0
                    ? String.valueOf(g.getMetacritic()) : "N/A");
            binding.tvGenre1.setText(g.getGenreNames());
        } else {
            binding.tvHeaderGame2.setText(g.getName());
            binding.tvDev2.setText(g.getDeveloperNames());
            binding.tvDate2.setText(g.getReleased());
            binding.tvMeta2.setText(g.getMetacritic() != null && g.getMetacritic() > 0
                    ? String.valueOf(g.getMetacritic()) : "N/A");
            binding.tvGenre2.setText(g.getGenreNames());
        }
    }

    /**
     * Populates all requirement rows using ViewModel's resolveSpec
     * which tries Steam API first, then falls back to RAWG raw text.
     */
    private void bindRequirements(boolean isGame1) {
        SteamRequirement.RequirementSpec min = viewModel.getMinSpec(isGame1);
        SteamRequirement.RequirementSpec rec = viewModel.getRecSpec(isGame1);
        final String na = "N/A";

        if (isGame1) {
            // Minimum
            binding.tvMinOs1.setText(min    != null ? min.getOs()        : na);
            binding.tvMinProc1.setText(min  != null ? min.getProcessor() : na);
            binding.tvMinMem1.setText(min   != null ? min.getMemory()    : na);
            binding.tvMinGfx1.setText(min   != null ? min.getGraphics()  : na);
            binding.tvMinDx1.setText(min    != null ? min.getDirectx()   : na);
            binding.tvMinStore1.setText(min != null ? min.getStorage()   : na);
            // Recommended
            binding.tvRecOs1.setText(rec    != null ? rec.getOs()        : na);
            binding.tvRecProc1.setText(rec  != null ? rec.getProcessor() : na);
            binding.tvRecMem1.setText(rec   != null ? rec.getMemory()    : na);
            binding.tvRecGfx1.setText(rec   != null ? rec.getGraphics()  : na);
            binding.tvRecDx1.setText(rec    != null ? rec.getDirectx()   : na);
            binding.tvRecStore1.setText(rec != null ? rec.getStorage()   : na);
        } else {
            // Minimum
            binding.tvMinOs2.setText(min    != null ? min.getOs()        : na);
            binding.tvMinProc2.setText(min  != null ? min.getProcessor() : na);
            binding.tvMinMem2.setText(min   != null ? min.getMemory()    : na);
            binding.tvMinGfx2.setText(min   != null ? min.getGraphics()  : na);
            binding.tvMinDx2.setText(min    != null ? min.getDirectx()   : na);
            binding.tvMinStore2.setText(min != null ? min.getStorage()   : na);
            // Recommended
            binding.tvRecOs2.setText(rec    != null ? rec.getOs()        : na);
            binding.tvRecProc2.setText(rec  != null ? rec.getProcessor() : na);
            binding.tvRecMem2.setText(rec   != null ? rec.getMemory()    : na);
            binding.tvRecGfx2.setText(rec   != null ? rec.getGraphics()  : na);
            binding.tvRecDx2.setText(rec    != null ? rec.getDirectx()   : na);
            binding.tvRecStore2.setText(rec != null ? rec.getStorage()   : na);
        }
    }

    // -----------------------------------------------------------------------
    //  State helpers
    // -----------------------------------------------------------------------

    private void showLoading() {
        binding.layoutSkeleton.getRoot().setVisibility(View.VISIBLE);
        skeletonManager.start();
        binding.scrollContent.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
    }

    private void showContent() {
        skeletonManager.stop();
        binding.layoutSkeleton.getRoot().setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);
    }

    private void showError(String msg) {
        skeletonManager.stop();
        binding.layoutSkeleton.getRoot().setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.tvError.setText(msg != null ? msg : getString(R.string.msg_error_unknown));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
