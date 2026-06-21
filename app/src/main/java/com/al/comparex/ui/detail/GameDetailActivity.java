package com.al.comparex.ui.detail;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.al.comparex.R;
import androidx.core.content.ContextCompat;
import com.al.comparex.data.model.GameDetail;
import com.al.comparex.data.model.SpekUser;
import com.al.comparex.data.model.SteamRequirement;
import com.al.comparex.databinding.ActivityGameDetailBinding;
import com.al.comparex.utils.SkeletonManager;
import com.al.comparex.utils.LangPrefs;
import com.al.comparex.utils.SpekPrefs;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

public class GameDetailActivity extends AppCompatActivity {

    public static final String EXTRA_GAME_ID   = "extra_game_id";
    public static final String EXTRA_GAME_NAME = "extra_game_name";

    private ActivityGameDetailBinding binding;
    private SkeletonManager skeletonManager;
    private GameDetailViewModel viewModel;
    private SpekUser userSpek;
    private int gameId;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LangPrefs.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        skeletonManager = new SkeletonManager(binding.layoutSkeleton.getRoot());

        gameId = getIntent().getIntExtra(EXTRA_GAME_ID, -1);
        String name = getIntent().getStringExtra(EXTRA_GAME_NAME);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(name != null ? name : "Detail Game");
        }

        userSpek  = SpekPrefs.loadSpek(this);
        viewModel = new ViewModelProvider(this).get(GameDetailViewModel.class);

        showUserSpekInAiCard();
        observeViewModel();
        viewModel.loadDetail(gameId);

        // Retry button: reset loadedGameId supaya bisa fetch ulang
        binding.btnRetry.setOnClickListener(v -> viewModel.loadDetail(gameId));

        binding.btnAiAnalysis.setOnClickListener(v -> {
            if (userSpek == null) {
                binding.tvAiResult.setText(
                        "Spesifikasi belum diset.\nKembali dan isi spek terlebih dahulu.");
                binding.cardAiResult.setVisibility(View.VISIBLE);
                return;
            }
            viewModel.requestAiAnalysis(userSpek);
        });
    }

    // ── Spek card ─────────────────────────────────────────────────────────────

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

    // ── Observers ─────────────────────────────────────────────────────────────

    private void observeViewModel() {
        viewModel.getGameDetail().observe(this, resource -> {
            if (resource.isLoading()) { showLoading(); return; }
            if (resource.isError())   { showError(resource.getMessage()); return; }
            if (resource.getData() != null) {
                showContent();
                bindGameDetail(resource.getData());
            }
        });

        viewModel.getSteamReq().observe(this, resource -> {
            if (resource == null) return;

            // Tampilkan loading indicator requirements
            if (resource.isLoading()) {
                binding.layoutReqLoading.setVisibility(View.VISIBLE);
                binding.layoutReqContent.setVisibility(View.GONE);
                binding.tvReqUnavailable.setVisibility(View.GONE);
                return;
            }

            // Loading selesai — sembunyikan indicator
            binding.layoutReqLoading.setVisibility(View.GONE);

            if (resource.isSuccess() && resource.getData() != null
                    && resource.getData().getData() != null
                    && resource.getData().getData().getSystemRequirements() != null) {
                // Data Steam berhasil
                bindRequirements(resource.getData().getData().getSystemRequirements());
                return;
            }

            // Cek apakah ini sinyal "rawg_fallback" dari ViewModel
            // atau memang error biasa → coba bind dari RAWG juga
            GameDetail gd = viewModel.getGameDetail().getValue() != null
                    ? viewModel.getGameDetail().getValue().getData() : null;

            if (gd != null && (gd.getRawgMinRequirements() != null
                    || gd.getRawgRecRequirements() != null)) {
                // Ada data RAWG — gunakan sebagai fallback
                bindRawgFallbackRequirements(gd);
            } else {
                // Benar-benar tidak ada requirements
                binding.layoutReqContent.setVisibility(View.GONE);
                binding.tvReqUnavailable.setVisibility(View.VISIBLE);
                binding.tvReqUnavailable.setText(getString(R.string.msg_req_not_available_yet));
            }
        });

        viewModel.getAiLoading().observe(this, loading -> {
            if (loading == null) return;
            binding.btnAiAnalysis.setEnabled(!loading);
            binding.progressAi.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnAiAnalysis.setText((CharSequence)(loading ? getString(R.string.msg_analyzing) : getString(R.string.btn_ai_analysis)));
            if (loading) binding.cardAiResult.setVisibility(View.GONE);
        });

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

        viewModel.getAiError().observe(this, err -> {
            if (err == null || err.isEmpty()) return;
            binding.tvAiResult.setText(err);
            binding.cardAiResult.setVisibility(View.VISIBLE);
        });

        viewModel.getAiRetryStatus().observe(this, status -> {
            if (status == null || status.isEmpty()) {
                if (Boolean.TRUE.equals(viewModel.getAiLoading().getValue()))
                    binding.btnAiAnalysis.setText("Menganalisis…");
                else
                    binding.btnAiAnalysis.setText(getString(R.string.btn_ai_analysis));
                return;
            }
            binding.btnAiAnalysis.setText(status);
            binding.progressAi.setVisibility(View.VISIBLE);
        });
    }

    // ── Bind helpers ──────────────────────────────────────────────────────────

    private void bindGameDetail(GameDetail game) {
        Glide.with(this)
                .load(game.getBackgroundImage())
                .placeholder(R.drawable.ic_game_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(binding.imgGame);

        binding.tvGameName.setText(game.getName());
        binding.tvReleaseDate.setText(game.getReleased());
        binding.tvDeveloper.setText(game.getDeveloperNames());
        binding.tvPublisher.setText(game.getPublisherNames());
        binding.tvGenre.setText(game.getGenreNames());
        binding.tvRating.setText(String.format("%.1f / 5.0  ★", game.getRating()));

        if (game.getMetacritic() != null && game.getMetacritic() > 0) {
            binding.tvMetacritic.setText(String.valueOf(game.getMetacritic()));
            binding.tvMetacritic.setVisibility(View.VISIBLE);
        } else {
            binding.tvMetacritic.setVisibility(View.GONE);
        }

        String desc = game.getDescriptionRaw();
        binding.tvDescription.setText(
                (desc != null && !desc.isEmpty()) ? desc : getString(R.string.msg_no_description));
    }

    private void bindRequirements(SteamRequirement.SystemRequirements req) {
        binding.layoutReqContent.setVisibility(View.VISIBLE);
        binding.tvReqUnavailable.setVisibility(View.GONE);

        SteamRequirement.RequirementSpec min = req.getMinimum();
        SteamRequirement.RequirementSpec rec = req.getRecommended();

        if (min != null) {
            binding.tvMinOs.setText(nullSafe(min.getOs()));
            binding.tvMinProcessor.setText(nullSafe(min.getProcessor()));
            binding.tvMinMemory.setText(nullSafe(min.getMemory()));
            binding.tvMinGraphics.setText(nullSafe(min.getGraphics()));
            binding.tvMinDirectx.setText(nullSafe(min.getDirectx()));
            binding.tvMinStorage.setText(nullSafe(min.getStorage()));
        } else {
            setMinUnavailable();
        }
        if (rec != null) {
            binding.tvRecOs.setText(nullSafe(rec.getOs()));
            binding.tvRecProcessor.setText(nullSafe(rec.getProcessor()));
            binding.tvRecMemory.setText(nullSafe(rec.getMemory()));
            binding.tvRecGraphics.setText(nullSafe(rec.getGraphics()));
            binding.tvRecDirectx.setText(nullSafe(rec.getDirectx()));
            binding.tvRecStorage.setText(nullSafe(rec.getStorage()));
        } else {
            setRecUnavailable();
        }
    }

    private void bindRawgFallbackRequirements(GameDetail gd) {
        binding.layoutReqContent.setVisibility(View.VISIBLE);
        binding.tvReqUnavailable.setVisibility(View.GONE);

        SteamRequirement.RequirementSpec minParsed = viewModel.resolveMinSpec();
        SteamRequirement.RequirementSpec recParsed = viewModel.resolveRecSpec();

        if (minParsed != null) {
            binding.tvMinOs.setText(nullSafe(minParsed.getOs()));
            binding.tvMinProcessor.setText(nullSafe(minParsed.getProcessor()));
            binding.tvMinMemory.setText(nullSafe(minParsed.getMemory()));
            binding.tvMinGraphics.setText(nullSafe(minParsed.getGraphics()));
            binding.tvMinDirectx.setText(nullSafe(minParsed.getDirectx()));
            binding.tvMinStorage.setText(nullSafe(minParsed.getStorage()));
        } else {
            setMinUnavailable();
        }
        if (recParsed != null) {
            binding.tvRecOs.setText(nullSafe(recParsed.getOs()));
            binding.tvRecProcessor.setText(nullSafe(recParsed.getProcessor()));
            binding.tvRecMemory.setText(nullSafe(recParsed.getMemory()));
            binding.tvRecGraphics.setText(nullSafe(recParsed.getGraphics()));
            binding.tvRecDirectx.setText(nullSafe(recParsed.getDirectx()));
            binding.tvRecStorage.setText(nullSafe(recParsed.getStorage()));
        } else {
            setRecUnavailable();
        }
    }

    /** Tampilkan "N/A" untuk semua field minimum jika spec null. */
    private void setMinUnavailable() {
        String na = "N/A";
        binding.tvMinOs.setText(na);
        binding.tvMinProcessor.setText(na);
        binding.tvMinMemory.setText(na);
        binding.tvMinGraphics.setText(na);
        binding.tvMinDirectx.setText(na);
        binding.tvMinStorage.setText(na);
    }

    /** Tampilkan "N/A" untuk semua field recommended jika spec null. */
    private void setRecUnavailable() {
        String na = "N/A";
        binding.tvRecOs.setText(na);
        binding.tvRecProcessor.setText(na);
        binding.tvRecMemory.setText(na);
        binding.tvRecGraphics.setText(na);
        binding.tvRecDirectx.setText(na);
        binding.tvRecStorage.setText(na);
    }

    private String nullSafe(String val) {
        return (val != null && !val.isEmpty()) ? val : "N/A";
    }

    // ── State ─────────────────────────────────────────────────────────────────

    private void showLoading() {
        binding.layoutSkeleton.getRoot().setVisibility(View.VISIBLE);
        skeletonManager.start();
        binding.layoutContent.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
    }

    private void showContent() {
        skeletonManager.stop();
        binding.layoutSkeleton.getRoot().setVisibility(View.GONE);
        binding.layoutContent.setVisibility(View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);
    }

    private void showError(String msg) {
        skeletonManager.stop();
        binding.layoutSkeleton.getRoot().setVisibility(View.GONE);
        binding.layoutContent.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.tvError.setText(msg != null ? msg : getString(R.string.msg_error_generic));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
