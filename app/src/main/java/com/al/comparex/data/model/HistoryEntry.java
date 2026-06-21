package com.al.comparex.data.model;

/**
 * One item in the recent-games history list.
 */
public class HistoryEntry {

    private final int     gameId;
    private final String  gameName;
    private final String  backgroundImage;   // nullable
    private final Integer metacritic;        // nullable
    private final String  released;          // nullable
    private final int     compatPercent;     // 0-100
    private final String  compatSetting;     // "Low", "Medium", "High", "Ultra", "N/A"
    private final long    checkedAt;         // epoch ms

    public HistoryEntry(int gameId, String gameName, String backgroundImage,
                        Integer metacritic, String released,
                        int compatPercent, String compatSetting, long checkedAt) {
        this.gameId          = gameId;
        this.gameName        = gameName;
        this.backgroundImage = backgroundImage;
        this.metacritic      = metacritic;
        this.released        = released;
        this.compatPercent   = compatPercent;
        this.compatSetting   = compatSetting;
        this.checkedAt       = checkedAt;
    }

    public int     getGameId()          { return gameId; }
    public String  getGameName()        { return gameName; }
    public String  getBackgroundImage() { return backgroundImage; }
    public Integer getMetacritic()      { return metacritic; }
    public String  getReleased()        { return released; }
    public int     getCompatPercent()   { return compatPercent; }
    public String  getCompatSetting()   { return compatSetting; }
    public long    getCheckedAt()       { return checkedAt; }
}
