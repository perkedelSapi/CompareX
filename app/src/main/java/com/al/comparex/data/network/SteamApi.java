package com.al.comparex.data.network;

import com.al.comparex.data.model.SteamRequirement;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Retrofit interface for the unofficial Steam requirements API.
 * Base URL: https://fadel.nasiwebhost.com/
 */
public interface SteamApi {

    /**
     * Get system requirements for a game by its Steam App ID.
     *
     * @param appId  Steam App ID (e.g. "570" for Dota 2)
     */
    @GET("games/steam.php")
    Call<SteamRequirement> getRequirements(
            @Query("game") String appId
    );
}
