package com.al.comparex.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.al.comparex.data.model.GameDetail;
import com.al.comparex.data.model.GameResponse;
import com.al.comparex.data.model.SteamRequirement;
import com.al.comparex.data.model.SteamSearchResponse;
import com.al.comparex.data.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Single source of truth for all network data.
 *
 * Steam requirements resolution strategy (in order):
 *  1. Use steam_appid field from RAWG game detail (if present).
 *  2. If not present, search Steam Store by game name → get App ID.
 *  3. Use App ID to call unofficial Steam requirements API.
 *  4. If all Steam calls fail, caller uses RAWG raw HTML as fallback.
 */
public class GameRepository {

    private static GameRepository instance;

    public static GameRepository getInstance() {
        if (instance == null) instance = new GameRepository();
        return instance;
    }

    private GameRepository() {}

    // ------------------------------------------------------------------
    //  RAWG Search
    // ------------------------------------------------------------------
    /** Fetch popular/trending games (ordering=-rating, metacritic > 80, page_size 20). */
    public LiveData<Resource<GameResponse>> getPopularGames() {
        MutableLiveData<Resource<GameResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));
        ApiClient.getRawgApi().getPopularGames(ApiClient.RAWG_API_KEY, "-metacritic", 20, 4, "60,100")
                .enqueue(new retrofit2.Callback<GameResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<GameResponse> call,
                                           retrofit2.Response<GameResponse> response) {
                        if (response.isSuccessful() && response.body() != null)
                            result.postValue(Resource.success(response.body()));
                        else
                            result.postValue(Resource.error("Gagal memuat game populer", null));
                    }
                    @Override
                    public void onFailure(retrofit2.Call<GameResponse> call, Throwable t) {
                        result.postValue(Resource.error("Koneksi gagal", null));
                    }
                });
        return result;
    }

    public LiveData<Resource<GameResponse>> searchGames(String query) {
        MutableLiveData<Resource<GameResponse>> ld = new MutableLiveData<>();
        ld.setValue(Resource.loading(null));

        ApiClient.getRawgApi()
                .searchGames(ApiClient.RAWG_API_KEY, query, 20, 4, false)
                .enqueue(new Callback<GameResponse>() {
                    @Override
                    public void onResponse(Call<GameResponse> c, Response<GameResponse> r) {
                        if (r.isSuccessful() && r.body() != null)
                            ld.setValue(Resource.success(r.body()));
                        else
                            ld.setValue(Resource.error("Gagal memuat data (kode: " + r.code() + ")", null));
                    }
                    @Override
                    public void onFailure(Call<GameResponse> c, Throwable t) {
                        ld.setValue(Resource.error("Koneksi gagal: " + t.getMessage(), null));
                    }
                });
        return ld;
    }

    // ------------------------------------------------------------------
    //  RAWG Game Detail
    // ------------------------------------------------------------------
    public LiveData<Resource<GameDetail>> getGameDetail(int gameId) {
        MutableLiveData<Resource<GameDetail>> ld = new MutableLiveData<>();
        ld.setValue(Resource.loading(null));

        ApiClient.getRawgApi()
                .getGameDetail(gameId, ApiClient.RAWG_API_KEY)
                .enqueue(new Callback<GameDetail>() {
                    @Override
                    public void onResponse(Call<GameDetail> c, Response<GameDetail> r) {
                        if (r.isSuccessful() && r.body() != null)
                            ld.setValue(Resource.success(r.body()));
                        else
                            ld.setValue(Resource.error("Gagal memuat detail game (kode: " + r.code() + ")", null));
                    }
                    @Override
                    public void onFailure(Call<GameDetail> c, Throwable t) {
                        ld.setValue(Resource.error("Koneksi gagal: " + t.getMessage(), null));
                    }
                });
        return ld;
    }

    // ------------------------------------------------------------------
    //  Steam Requirements — full resolution chain
    // ------------------------------------------------------------------

    /**
     * Resolves Steam requirements for a game.
     *
     * @param steamAppId  Pass the steam_appid from RAWG if available, else null/empty.
     * @param gameName    Always pass the game name for fallback store-search.
     */
    public LiveData<Resource<SteamRequirement>> getSteamRequirements(
            String steamAppId, String gameName) {

        MutableLiveData<Resource<SteamRequirement>> ld = new MutableLiveData<>();
        ld.setValue(Resource.loading(null));

        if (steamAppId != null && !steamAppId.isEmpty() && !steamAppId.equals("0")) {
            // Step 1: We already have the App ID — fetch directly
            fetchSteamByAppId(steamAppId, gameName, ld);
        } else {
            // Step 2: Search Steam Store by game name first
            searchSteamStore(gameName, ld);
        }
        return ld;
    }

    /** Step 2: Search Steam Store to find the App ID. */
    private void searchSteamStore(String gameName,
                                   MutableLiveData<Resource<SteamRequirement>> ld) {
        ApiClient.getSteamStoreApi()
                .searchGame(gameName, "english", "US")
                .enqueue(new Callback<SteamSearchResponse>() {
                    @Override
                    public void onResponse(Call<SteamSearchResponse> c,
                                           Response<SteamSearchResponse> r) {
                        if (r.isSuccessful() && r.body() != null
                                && r.body().getItems() != null
                                && !r.body().getItems().isEmpty()) {
                            // Pick the best match (first result is usually correct)
                            String appId = String.valueOf(r.body().getItems().get(0).getId());
                            fetchSteamByAppId(appId, gameName, ld);
                        } else {
                            ld.setValue(Resource.error("Game tidak ditemukan di Steam", null));
                        }
                    }
                    @Override
                    public void onFailure(Call<SteamSearchResponse> c, Throwable t) {
                        ld.setValue(Resource.error("Steam Store search gagal: " + t.getMessage(), null));
                    }
                });
    }

    /** Step 3: Fetch requirements from unofficial API using App ID. */
    private void fetchSteamByAppId(String appId, String gameName,
                                    MutableLiveData<Resource<SteamRequirement>> ld) {
        ApiClient.getSteamApi()
                .getRequirements(appId)
                .enqueue(new Callback<SteamRequirement>() {
                    @Override
                    public void onResponse(Call<SteamRequirement> c,
                                           Response<SteamRequirement> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().isSuccess()) {
                            ld.setValue(Resource.success(r.body()));
                        } else {
                            // App ID resolved but requirements not available (maybe not on Steam)
                            ld.setValue(Resource.error("Requirements tidak tersedia di Steam", null));
                        }
                    }
                    @Override
                    public void onFailure(Call<SteamRequirement> c, Throwable t) {
                        ld.setValue(Resource.error("Steam API gagal: " + t.getMessage(), null));
                    }
                });
    }

    // ------------------------------------------------------------------
    //  Generic Resource wrapper
    // ------------------------------------------------------------------
    public static class Resource<T> {
        public enum Status { SUCCESS, ERROR, LOADING }

        private final Status status;
        private final T      data;
        private final String message;

        private Resource(Status s, T d, String m) { status = s; data = d; message = m; }

        public static <T> Resource<T> success(T data)              { return new Resource<>(Status.SUCCESS, data, null); }
        public static <T> Resource<T> error(String msg, T data)    { return new Resource<>(Status.ERROR,   data, msg);  }
        public static <T> Resource<T> loading(T data)              { return new Resource<>(Status.LOADING, data, null); }

        public Status getStatus()  { return status;  }
        public T      getData()    { return data;    }
        public String getMessage() { return message; }

        public boolean isLoading() { return status == Status.LOADING; }
        public boolean isSuccess() { return status == Status.SUCCESS; }
        public boolean isError()   { return status == Status.ERROR;   }
    }
}
