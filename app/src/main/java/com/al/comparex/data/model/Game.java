package com.al.comparex.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents a game item from RAWG search results.
 */
public class Game {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("released")
    private String released;

    @SerializedName("background_image")
    private String backgroundImage;

    @SerializedName("metacritic")
    private Integer metacritic;

    @SerializedName("rating")
    private double rating;

    @SerializedName("platforms")
    private List<PlatformWrapper> platforms;

    // Local-only field: whether this game is selected for comparison
    private boolean isSelected = false;

    // --- Getters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getReleased() { return released; }
    public String getBackgroundImage() { return backgroundImage; }
    public Integer getMetacritic() { return metacritic; }
    public double getRating() { return rating; }
    public List<PlatformWrapper> getPlatforms() { return platforms; }
    public boolean isSelected() { return isSelected; }

    // --- Setters ---
    public void setSelected(boolean selected) { isSelected = selected; }

    /** Returns a comma-separated string of platform names. */
    public String getPlatformNames() {
        if (platforms == null || platforms.isEmpty()) return "N/A";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < platforms.size(); i++) {
            if (platforms.get(i).getPlatform() != null) {
                sb.append(platforms.get(i).getPlatform().getName());
                if (i < platforms.size() - 1) sb.append(", ");
            }
        }
        return sb.toString();
    }

    // --- Nested classes ---

    public static class PlatformWrapper {
        @SerializedName("platform")
        private Platform platform;

        public Platform getPlatform() { return platform; }
    }

    public static class Platform {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;
        @SerializedName("slug")
        private String slug;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getSlug() { return slug; }
    }
}
