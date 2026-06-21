package com.al.comparex.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response from Steam Store search API:
 * https://store.steampowered.com/api/storesearch/?term={name}&l=english&cc=US
 */
public class SteamSearchResponse {

    @SerializedName("total")
    private int total;

    @SerializedName("items")
    private List<SteamItem> items;

    public int getTotal() { return total; }
    public List<SteamItem> getItems() { return items; }

    public static class SteamItem {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        public int getId()     { return id; }
        public String getName(){ return name; }
    }
}
