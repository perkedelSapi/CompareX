package com.al.comparex.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Response model dari unofficial Steam requirements API.
 * https://fadel.nasiwebhost.com/games/steam.php?game={app_id}
 */
public class SteamRequirement {

    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private SteamData data;

    public String getStatus() { return status; }
    public SteamData getData() { return data; }

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status) && data != null;
    }

    // ----------------------------------------------------------------
    public static class SteamData {
        @SerializedName("title")
        private String title;

        @SerializedName("system_requirements")
        private SystemRequirements systemRequirements;

        public String getTitle() { return title; }
        public SystemRequirements getSystemRequirements() { return systemRequirements; }
    }

    // ----------------------------------------------------------------
    public static class SystemRequirements {
        @SerializedName("minimum")
        private RequirementSpec minimum;

        @SerializedName("recommended")
        private RequirementSpec recommended;

        public RequirementSpec getMinimum() { return minimum; }
        public RequirementSpec getRecommended() { return recommended; }
    }

    // ----------------------------------------------------------------
    // Non-final so GameDetailViewModel can subclass it for RAWG fallback
    public static class RequirementSpec {
        @SerializedName("os")
        private String os;

        @SerializedName("processor")
        private String processor;

        @SerializedName("memory")
        private String memory;

        @SerializedName("graphics")
        private String graphics;

        @SerializedName("directx")
        private String directx;

        @SerializedName("storage")
        private String storage;

        @SerializedName("additional_notes")
        private String additionalNotes;

        // Default constructor (required for subclassing & Gson)
        public RequirementSpec() {}

        // All-args constructor for programmatic creation
        public RequirementSpec(String os, String processor, String memory,
                               String graphics, String directx, String storage) {
            this.os = os;
            this.processor = processor;
            this.memory = memory;
            this.graphics = graphics;
            this.directx = directx;
            this.storage = storage;
        }

        public String getOs()             { return os        != null ? os        : "N/A"; }
        public String getProcessor()      { return processor != null ? processor : "N/A"; }
        public String getMemory()         { return memory    != null ? memory    : "N/A"; }
        public String getGraphics()       { return graphics  != null ? graphics  : "N/A"; }
        public String getDirectx()        { return directx   != null ? directx   : "N/A"; }
        public String getStorage()        { return storage   != null ? storage   : "N/A"; }
        public String getAdditionalNotes(){ return additionalNotes != null ? additionalNotes : "N/A"; }
    }
}
