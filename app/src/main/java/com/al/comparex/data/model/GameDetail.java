package com.al.comparex.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Full game detail response from RAWG API endpoint GET /games/{id}.
 */
public class GameDetail {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("description_raw")
    private String descriptionRaw;

    @SerializedName("released")
    private String released;

    @SerializedName("background_image")
    private String backgroundImage;

    @SerializedName("metacritic")
    private Integer metacritic;

    @SerializedName("rating")
    private double rating;

    @SerializedName("developers")
    private List<NamedEntity> developers;

    @SerializedName("publishers")
    private List<NamedEntity> publishers;

    @SerializedName("genres")
    private List<NamedEntity> genres;

    @SerializedName("esrb_rating")
    private EsrbRating esrbRating;

    @SerializedName("platforms")
    private List<PlatformDetail> platforms;

    // steam_appid is sometimes returned in detail response
    @SerializedName("steam_appid")
    private Integer steamAppId;

    // --- Convenience getters ---

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescriptionRaw() { return descriptionRaw != null ? descriptionRaw : ""; }
    public String getReleased() { return released != null ? released : "N/A"; }
    public String getBackgroundImage() { return backgroundImage; }
    public Integer getMetacritic() { return metacritic; }
    public double getRating() { return rating; }
    public List<NamedEntity> getDevelopers() { return developers; }
    public List<NamedEntity> getPublishers() { return publishers; }
    public List<NamedEntity> getGenres() { return genres; }
    public EsrbRating getEsrbRating() { return esrbRating; }
    public List<PlatformDetail> getPlatforms() { return platforms; }
    public Integer getSteamAppId() { return steamAppId; }

    public String getDeveloperNames() { return joinNames(developers); }
    public String getPublisherNames() { return joinNames(publishers); }
    public String getGenreNames() { return joinNames(genres); }

    private String joinNames(List<NamedEntity> list) {
        if (list == null || list.isEmpty()) return "N/A";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).getName());
            if (i < list.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    /**
     * Returns the PC minimum requirements string from RAWG platform data (fallback).
     */
    public String getRawgMinRequirements() {
        if (platforms == null) return null;
        for (PlatformDetail pd : platforms) {
            if (pd.getPlatform() != null && "pc".equalsIgnoreCase(pd.getPlatform().getSlug())
                    && pd.getRequirements() != null) {
                return pd.getRequirements().getMinimum();
            }
        }
        return null;
    }

    /**
     * Returns the PC recommended requirements string from RAWG platform data (fallback).
     */
    public String getRawgRecRequirements() {
        if (platforms == null) return null;
        for (PlatformDetail pd : platforms) {
            if (pd.getPlatform() != null && "pc".equalsIgnoreCase(pd.getPlatform().getSlug())
                    && pd.getRequirements() != null) {
                return pd.getRequirements().getRecommended();
            }
        }
        return null;
    }

    // --- Nested classes ---

    public static class NamedEntity {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;
        @SerializedName("slug")
        private String slug;

        public int getId() { return id; }
        public String getName() { return name != null ? name : "N/A"; }
        public String getSlug() { return slug; }
    }

    public static class EsrbRating {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;

        public int getId() { return id; }
        public String getName() { return name != null ? name : "N/A"; }
    }

    public static class PlatformDetail {
        @SerializedName("platform")
        private Game.Platform platform;
        @SerializedName("requirements")
        private Requirements requirements;

        public Game.Platform getPlatform() { return platform; }
        public Requirements getRequirements() { return requirements; }
    }

    public static class Requirements {
        @SerializedName("minimum")
        private String minimum;
        @SerializedName("recommended")
        private String recommended;

        public String getMinimum() { return minimum; }
        public String getRecommended() { return recommended; }
    }
}
