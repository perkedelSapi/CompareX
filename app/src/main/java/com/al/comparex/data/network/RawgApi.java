package com.al.comparex.data.network;

import com.al.comparex.data.model.GameDetail;
import com.al.comparex.data.model.GameResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit interface for the RAWG Video Games Database API.
 * Base URL: https://api.rawg.io/api/
 *
 * Platform IDs di RAWG:
 *   4  = PC (Windows)
 *   5  = macOS
 *   6  = Linux
 *   18 = PlayStation 4
 *   7  = Nintendo Switch
 *
 * Semua endpoint menggunakan platforms=4 agar hanya game PC yang muncul.
 */
public interface RawgApi {

    /**
     * Ambil game populer — diurutkan by metacritic, hanya platform PC.
     *
     * @param apiKey     RAWG API key
     * @param ordering   Field sorting, e.g. "-metacritic", "-rating", "-added"
     * @param pageSize   Jumlah hasil (max 40)
     * @param platforms  Filter platform: 4 = PC
     * @param metacritic Filter minimum metacritic score, e.g. "60,100"
     */
    @GET("games")
    Call<GameResponse> getPopularGames(
            @Query("key")        String apiKey,
            @Query("ordering")   String ordering,
            @Query("page_size")  int    pageSize,
            @Query("platforms")  int    platforms,
            @Query("metacritic") String metacritic
    );

    /**
     * Cari game berdasarkan nama — hanya platform PC.
     *
     * @param apiKey     RAWG API key
     * @param query      Kata kunci pencarian
     * @param pageSize   Jumlah hasil (max 40)
     * @param platforms  Filter platform: 4 = PC
     * @param searchExact  Exact title match (false = fuzzy)
     */
    @GET("games")
    Call<GameResponse> searchGames(
            @Query("key")           String  apiKey,
            @Query("search")        String  query,
            @Query("page_size")     int     pageSize,
            @Query("platforms")     int     platforms,
            @Query("search_exact")  boolean searchExact
    );

    /**
     * Get full detail of a single game by its RAWG ID.
     */
    @GET("games/{id}")
    Call<GameDetail> getGameDetail(
            @Path("id")  int    gameId,
            @Query("key") String apiKey
    );
}
