package com.al.comparex.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.al.comparex.R;
import com.al.comparex.data.model.Game;
import com.al.comparex.data.model.HistoryEntry;
import com.al.comparex.databinding.ActivityMainBinding;
import com.al.comparex.ui.compare.CompareActivity;
import com.al.comparex.ui.detail.GameDetailActivity;
import com.al.comparex.ui.spek.SpekInputActivity;
import com.al.comparex.utils.HistoryPrefs;
import com.al.comparex.utils.SkeletonManager;
import com.al.comparex.utils.LangPrefs;
import com.al.comparex.utils.SpekPrefs;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SkeletonManager skeletonManager;
    private MainViewModel viewModel;
    private GameAdapter adapter;
    private GameAdapter popularAdapter;
    private HistoryAdapter historyAdapter;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LangPrefs.applyLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        skeletonManager = new SkeletonManager(binding.layoutSkeleton);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("CompareX");

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupSearchRecycler();
        setupPopularRecycler();
        setupSearchBar();
        setupCompareBar();
        setupHistory();
        setupOnboardingBanner();
        observeViewModel();

        if (!viewModel.getLastQuery().isEmpty()) {
            binding.etSearch.setText(viewModel.getLastQuery());
        } else {
            showState(State.HINT);
            viewModel.loadPopularGames();
        }
    }

    // ── RecyclerViews ─────────────────────────────────────────────────────────

    private void setupSearchRecycler() {
        adapter = new GameAdapter(new GameAdapter.OnGameClickListener() {
            @Override public void onCardClick(Game game)       { openDetail(game); }
            @Override public boolean onCheckboxToggle(Game game) {
                boolean ok = viewModel.toggleSelection(game);
                if (!ok) Toast.makeText(MainActivity.this,
                        getString(R.string.msg_max_selection), Toast.LENGTH_SHORT).show();
                return ok;
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setHasFixedSize(true);
    }

    private void setupPopularRecycler() {
        popularAdapter = new GameAdapter(new GameAdapter.OnGameClickListener() {
            @Override public void onCardClick(Game game)       { openDetail(game); }
            @Override public boolean onCheckboxToggle(Game game) {
                boolean ok = viewModel.toggleSelection(game);
                if (!ok) Toast.makeText(MainActivity.this,
                        getString(R.string.msg_max_selection), Toast.LENGTH_SHORT).show();
                return ok;
            }
        });
        binding.recyclerPopular.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerPopular.setAdapter(popularAdapter);
        binding.recyclerPopular.setHasFixedSize(false);
        binding.recyclerPopular.setNestedScrollingEnabled(false);
    }

    // ── Search bar ────────────────────────────────────────────────────────────

    private void setupSearchBar() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
            }
            @Override
            public void afterTextChanged(Editable s) {
                String q = s.toString().trim();
                if (q.length() < 2) {
                    showState(State.HINT);
                    viewModel.loadPopularGames(); // no-op jika sudah loaded (popularLoaded flag)
                    return;
                }
                searchRunnable = () -> viewModel.searchGames(q);
                searchHandler.postDelayed(searchRunnable, 600);
            }
        });
        binding.btnRetry.setOnClickListener(v -> {
            String q = binding.etSearch.getText().toString().trim();
            if (!q.isEmpty()) viewModel.searchGames(q);
        });
    }

    // ── Compare bar ───────────────────────────────────────────────────────────

    private void setupCompareBar() {
        binding.btnCompare.setEnabled(false);
        binding.btnCompare.setAlpha(0.5f);
        binding.btnCompare.setOnClickListener(v -> launchCompare());
        binding.btnClearSelection.setOnClickListener(v -> {
            viewModel.clearSelections();
            List<Game> cur = adapter.getCurrentList();
            if (cur != null && !cur.isEmpty()) adapter.submitList(cur);
            List<Game> pop = popularAdapter.getCurrentList();
            if (pop != null && !pop.isEmpty()) popularAdapter.submitList(pop);
            Toast.makeText(this, getString(R.string.msg_selection_cleared), Toast.LENGTH_SHORT).show();
        });
    }

    // ── Observers ─────────────────────────────────────────────────────────────

    private void observeViewModel() {
        // Search results
        viewModel.getSearchResult().observe(this, resource -> {
            if (resource == null) return;
            if (resource.isLoading()) { showState(State.LOADING); return; }
            if (resource.isError())   {
                showState(State.ERROR);
                binding.tvError.setText(resource.getMessage());
                return;
            }
            if (resource.getData() == null
                    || resource.getData().getResults() == null
                    || resource.getData().getResults().isEmpty()) {
                showState(State.EMPTY); return;
            }
            adapter.submitList(resource.getData().getResults());
            showState(State.CONTENT);
        });

        // Popular games
        viewModel.getPopularResult().observe(this, resource -> {
            if (resource == null) return;
            binding.progressPopular.setVisibility(resource.isLoading() ? View.VISIBLE : View.GONE);
            if (resource.isLoading()) {
                // Pastikan section popular terlihat saat loading
                if (!viewModel.isSearchActive()) showState(State.HINT);
                return;
            }
            if (resource.isSuccess() && resource.getData() != null
                    && resource.getData().getResults() != null) {
                popularAdapter.submitList(resource.getData().getResults());
                // Tampilkan section popular — showState HINT memastikan layout_hint visible
                if (!viewModel.isSearchActive()) showState(State.HINT);
            } else if (resource.isError()) {
                // Popular gagal load — tetap show HINT section agar tidak blank total
                if (!viewModel.isSearchActive()) showState(State.HINT);
            }
        });

        // Compare bar
        viewModel.getSelectedGames().observe(this, selected -> {
            int count = selected != null ? selected.size() : 0;
            boolean ready = count == 2;
            binding.layoutCompareBar.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            binding.tvSelectedCount.setText(getString(R.string.label_selected_games).replace("0", String.valueOf(count)));
            binding.btnCompare.setEnabled(ready);
            binding.btnCompare.setAlpha(ready ? 1f : 0.5f);
        });
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void openDetail(Game game) {
        Intent i = new Intent(this, GameDetailActivity.class);
        i.putExtra(GameDetailActivity.EXTRA_GAME_ID,   game.getId());
        i.putExtra(GameDetailActivity.EXTRA_GAME_NAME, game.getName());
        startActivity(i);
    }

    private void launchCompare() {
        List<Game> sel = viewModel.getSelectedGames().getValue();
        if (sel == null || sel.size() < 2) return;
        Intent i = new Intent(this, CompareActivity.class);
        i.putExtra(CompareActivity.EXTRA_GAME_ID_1,   sel.get(0).getId());
        i.putExtra(CompareActivity.EXTRA_GAME_NAME_1, sel.get(0).getName());
        i.putExtra(CompareActivity.EXTRA_GAME_ID_2,   sel.get(1).getId());
        i.putExtra(CompareActivity.EXTRA_GAME_NAME_2, sel.get(1).getName());
        startActivity(i);
    }

    // ── Onboarding banner ─────────────────────────────────────────────────────

    private void setupOnboardingBanner() {
        binding.btnSetupSpek.setOnClickListener(v ->
                startActivity(new Intent(this, SpekInputActivity.class)));
        refreshOnboardingBanner();
    }

    private void refreshOnboardingBanner() {
        boolean spekSet = SpekPrefs.isSpekSet(this);
        binding.layoutOnboardingBanner.setVisibility(spekSet ? View.GONE : View.VISIBLE);
    }

    // ── History ───────────────────────────────────────────────────────────────

    private void setupHistory() {
        historyAdapter = new HistoryAdapter(new HistoryAdapter.OnHistoryClickListener() {
            @Override public void onClick(HistoryEntry entry) {
                Intent i = new Intent(MainActivity.this, GameDetailActivity.class);
                i.putExtra(GameDetailActivity.EXTRA_GAME_ID,   entry.getGameId());
                i.putExtra(GameDetailActivity.EXTRA_GAME_NAME, entry.getGameName());
                startActivity(i);
            }
            @Override public void onLongClick(HistoryEntry entry) {
                // Show delete single item dialog
                new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.msg_delete_history_title))
                        .setMessage(entry.getGameName())
                        .setPositiveButton(getString(R.string.btn_delete), (d, w) -> {
                            List<HistoryEntry> list = HistoryPrefs.load(MainActivity.this);
                            list.removeIf(e -> e.getGameId() == entry.getGameId());
                            // Re-save via clear+add workaround using HistoryPrefs internals
                            HistoryPrefs.clear(MainActivity.this);
                            for (int i2 = list.size() - 1; i2 >= 0; i2--)
                                HistoryPrefs.add(MainActivity.this, list.get(i2));
                            refreshHistory();
                        })
                        .setNegativeButton(getString(R.string.btn_cancel), null)
                        .show();
            }
        });
        binding.recyclerHistory.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerHistory.setAdapter(historyAdapter);
        binding.recyclerHistory.setHasFixedSize(false);

        binding.tvHistoryClear.setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(getString(R.string.msg_delete_all_history_title))
                        .setMessage(getString(R.string.msg_delete_all_history_body))
                        .setPositiveButton(getString(R.string.btn_delete_all), (d, w) -> {
                            HistoryPrefs.clear(this);
                            refreshHistory();
                        })
                        .setNegativeButton(getString(R.string.btn_cancel), null)
                        .show());
    }

    private void refreshHistory() {
        List<HistoryEntry> list = HistoryPrefs.load(this);
        boolean hasHistory = !list.isEmpty();
        binding.layoutHistorySection.setVisibility(hasHistory ? View.VISIBLE : View.GONE);
        if (hasHistory) historyAdapter.submitList(new java.util.ArrayList<>(list));
    }

    // ── State ─────────────────────────────────────────────────────────────────

    private enum State { LOADING, CONTENT, EMPTY, ERROR, HINT }

    private void showState(State s) {
        boolean loading = s == State.LOADING;
        binding.layoutSkeleton.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) skeletonManager.start(); else skeletonManager.stop();

        binding.recyclerView.setVisibility(s == State.CONTENT ? View.VISIBLE : View.GONE);
        binding.layoutEmpty.setVisibility(s == State.EMPTY   ? View.VISIBLE  : View.GONE);
        binding.layoutError.setVisibility(s == State.ERROR   ? View.VISIBLE  : View.GONE);
        binding.layoutHint.setVisibility(s == State.HINT     ? View.VISIBLE  : View.GONE);
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem langItem = menu.findItem(R.id.action_toggle_lang);
        if (langItem != null) {
            langItem.setTitle(LangPrefs.isEnglish(this) ? "Bahasa Indonesia" : "English");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_lang) {
            String newLang = LangPrefs.isEnglish(this) ? LangPrefs.LANG_ID : LangPrefs.LANG_EN;
            LangPrefs.saveLang(this, newLang);
            recreate();
            return true;
        }
        if (item.getItemId() == R.id.action_edit_spek) {
            startActivity(new Intent(this, SpekInputActivity.class)); return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHistory();
        refreshOnboardingBanner();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
    }
}
