package com.al.comparex.data.network;

import com.al.comparex.data.model.SteamSearchResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Steam Store search API – used to resolve a game name → Steam App ID.
 * Base URL: https://store.steampowered.com/api/
 */
public interface SteamStoreApi {

    /**
     * Search Steam store for a game by name.
     *
     * @param term  Game name to search
     * @param lang  Language (e.g. "english")
     * @param cc    Country code (e.g. "US")
     */
    @GET("storesearch/")
    Call<SteamSearchResponse> searchGame(
            @Query("term") String term,
            @Query("l")    String lang,
            @Query("cc")   String cc
    );
}
