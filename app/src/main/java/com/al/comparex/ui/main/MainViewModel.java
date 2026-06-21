package com.al.comparex.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.al.comparex.data.model.Game;
import com.al.comparex.data.model.GameResponse;
import com.al.comparex.data.network.ApiClient;
import com.al.comparex.data.repository.GameRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<GameRepository.Resource<GameResponse>> searchResult   = new MutableLiveData<>();
    private final MutableLiveData<GameRepository.Resource<GameResponse>> popularResult  = new MutableLiveData<>();
    private final MutableLiveData<List<Game>> selectedGames = new MutableLiveData<>(new ArrayList<>());

    private String  lastQuery     = "";
    private boolean popularLoaded = false;

    // Active calls — cancelled in onCleared to prevent observer accumulation
    private Call<GameResponse> activeSearchCall;
    private Call<GameResponse> activePopularCall;

    public LiveData<GameRepository.Resource<GameResponse>> getSearchResult()  { return searchResult;  }
    public LiveData<GameRepository.Resource<GameResponse>> getPopularResult() { return popularResult; }
    public LiveData<List<Game>>                            getSelectedGames() { return selectedGames; }
    public String  getLastQuery()   { return lastQuery; }
    public boolean isSearchActive() { return !lastQuery.isEmpty(); }

    // ── Search ────────────────────────────────────────────────────────────────

    public void searchGames(String query) {
        if (query == null || query.trim().isEmpty()) return;
        lastQuery = query.trim();

        // Cancel previous in-flight search
        if (activeSearchCall != null) activeSearchCall.cancel();

        searchResult.setValue(GameRepository.Resource.loading(null));
        activeSearchCall = ApiClient.getRawgApi()
                .searchGames(ApiClient.RAWG_API_KEY, lastQuery, 20, 4, false);
        activeSearchCall.enqueue(new Callback<GameResponse>() {
            @Override public void onResponse(Call<GameResponse> c, Response<GameResponse> r) {
                if (r.isSuccessful() && r.body() != null)
                    searchResult.setValue(GameRepository.Resource.success(r.body()));
                else
                    searchResult.setValue(GameRepository.Resource.error(
                            "Gagal memuat data (kode: " + r.code() + ")", null));
            }
            @Override public void onFailure(Call<GameResponse> c, Throwable t) {
                if (!c.isCanceled())
                    searchResult.setValue(GameRepository.Resource.error(
                            "Koneksi gagal: " + t.getMessage(), null));
            }
        });
    }

    // ── Popular games (loaded once) ───────────────────────────────────────────

    public void loadPopularGames() {
        if (popularLoaded) return;
        popularLoaded = true;

        popularResult.setValue(GameRepository.Resource.loading(null));
        activePopularCall = ApiClient.getRawgApi()
                .getPopularGames(ApiClient.RAWG_API_KEY, "-metacritic", 20, 4, "60,100");
        activePopularCall.enqueue(new Callback<GameResponse>() {
            @Override public void onResponse(Call<GameResponse> c, Response<GameResponse> r) {
                if (r.isSuccessful() && r.body() != null)
                    popularResult.setValue(GameRepository.Resource.success(r.body()));
                else
                    popularResult.setValue(GameRepository.Resource.error(
                            "Gagal memuat game populer", null));
            }
            @Override public void onFailure(Call<GameResponse> c, Throwable t) {
                if (!c.isCanceled())
                    popularResult.setValue(GameRepository.Resource.error(
                            "Koneksi gagal", null));
            }
        });
    }

    // ── Selection ─────────────────────────────────────────────────────────────

    public boolean toggleSelection(Game game) {
        List<Game> current = selectedGames.getValue();
        if (current == null) current = new ArrayList<>();
        if (game.isSelected()) {
            game.setSelected(false);
            current.remove(game);
        } else {
            if (current.size() >= 2) return false;
            game.setSelected(true);
            current.add(game);
        }
        selectedGames.setValue(current);
        return true;
    }

    public int getSelectionCount() {
        List<Game> s = selectedGames.getValue();
        return s != null ? s.size() : 0;
    }

    public void clearSelections() {
        List<Game> current = selectedGames.getValue();
        if (current != null) {
            for (Game g : current) g.setSelected(false);
            current.clear();
        }
        selectedGames.setValue(new ArrayList<>());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (activeSearchCall  != null) activeSearchCall.cancel();
        if (activePopularCall != null) activePopularCall.cancel();
    }
}
