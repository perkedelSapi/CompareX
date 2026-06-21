package com.al.comparex.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Tingkat 2: Tabel benchmark komunitas hardcoded.
 *
 * Data bersumber dari Digital Foundry, TechPowerUp, GamersNexus, komunitas
 * Steam/Reddit, dan pengujian YouTuber populer (Jarrod's Tech, Testing Games, dll).
 *
 * Format setiap entri:
 *   GPU tier (normalized) × Game (normalized) → FPS avg, 1% low, setting, resolusi, notes
 *
 * Cara kerja:
 *   1. GPU user di-normalize ke tier terdekat (RTX 3050 Laptop → "rtx3050_laptop")
 *   2. Game di-normalize (Elden Ring → "elden_ring")
 *   3. Cari match → inject ke prompt sebagai "DATA BENCHMARK KOMUNITAS"
 *   4. AI wajib gunakan data ini sebagai anchor, bukan mengarang
 */
public class BenchmarkHintBuilder {

    // =========================================================================
    //  DATA MODEL
    // =========================================================================

    public static class BenchmarkEntry {
        public final String gpuTier;
        public final String gameKey;
        public final String gameName;
        public final int    avgFps;
        public final int    lowFps1pct;
        public final String setting;
        public final String resolution;
        public final String notes;

        public BenchmarkEntry(String gpuTier, String gameKey, String gameName,
                              int avgFps, int lowFps1pct,
                              String setting, String resolution, String notes) {
            this.gpuTier    = gpuTier;
            this.gameKey    = gameKey;
            this.gameName   = gameName;
            this.avgFps     = avgFps;
            this.lowFps1pct = lowFps1pct;
            this.setting    = setting;
            this.resolution = resolution;
            this.notes      = notes;
        }
    }

    // =========================================================================
    //  GPU NORMALIZATION
    //  Kelompokkan GPU ke tier yang ada datanya. Urutan: dari yang paling spesifik
    //  ke yang paling umum agar tidak salah match.
    // =========================================================================

    public static String normalizeGpu(String gpuName) {
        if (gpuName == null) return "unknown";
        String u = gpuName.toUpperCase().trim();

        // ── NVIDIA RTX 50 series ──────────────────────────────────────────────
        if (u.contains("RTX 5090"))                      return "rtx5090";
        if (u.contains("RTX 5080"))                      return "rtx5080";
        if (u.contains("RTX 5070 TI") || u.contains("RTX 5070TI")) return "rtx5070ti";
        if (u.contains("RTX 5070"))                      return "rtx5070";
        if (u.contains("RTX 5060 TI") || u.contains("RTX 5060TI")) return "rtx5060ti";
        if (u.contains("RTX 5060"))                      return "rtx5060";

        // ── NVIDIA RTX 40 series Desktop ─────────────────────────────────────
        if (u.contains("RTX 4090"))                      return "rtx4090";
        if (u.contains("RTX 4080 SUPER"))                return "rtx4080super";
        if (u.contains("RTX 4080"))                      return "rtx4080";
        if (u.contains("RTX 4070 TI SUPER") || u.contains("RTX 4070TI SUPER")) return "rtx4070tisuper";
        if (u.contains("RTX 4070 TI") || u.contains("RTX 4070TI")) return "rtx4070ti";
        if (u.contains("RTX 4070 SUPER"))                return "rtx4070super";
        if (u.contains("RTX 4070"))                      return "rtx4070";
        if (u.contains("RTX 4060 TI") || u.contains("RTX 4060TI")) return "rtx4060ti";
        if (u.contains("RTX 4060"))                      return "rtx4060";
        if (u.contains("RTX 4050"))                      return "rtx4050_laptop";

        // ── NVIDIA RTX 40 series Laptop ───────────────────────────────────────
        if (u.contains("RTX 4090") && (u.contains("LAPTOP") || u.contains("MOBILE"))) return "rtx4090_laptop";
        if (u.contains("RTX 4080") && (u.contains("LAPTOP") || u.contains("MOBILE"))) return "rtx4080_laptop";
        if (u.contains("RTX 4070") && (u.contains("LAPTOP") || u.contains("MOBILE"))) return "rtx4070_laptop";
        if (u.contains("RTX 4060") && (u.contains("LAPTOP") || u.contains("MOBILE"))) return "rtx4060_laptop";

        // ── NVIDIA RTX 30 series Desktop ─────────────────────────────────────
        if (u.contains("RTX 3090 TI") || u.contains("RTX 3090TI")) return "rtx3090ti";
        if (u.contains("RTX 3090"))                      return "rtx3090";
        if (u.contains("RTX 3080 TI") || u.contains("RTX 3080TI")) return "rtx3080ti";
        if (u.contains("RTX 3080"))                      return "rtx3080";
        if (u.contains("RTX 3070 TI") || u.contains("RTX 3070TI")) return "rtx3070ti";
        if (u.contains("RTX 3070"))                      return "rtx3070";
        if (u.contains("RTX 3060 TI") || u.contains("RTX 3060TI")) return "rtx3060ti";
        if (u.contains("RTX 3060"))                      return "rtx3060";
        if (u.contains("RTX 3050 TI") || u.contains("RTX 3050TI")) return "rtx3050";
        if (u.contains("RTX 3050") && (u.contains("LAPTOP") || u.contains("MOBILE") || u.matches(".*\\d{4}[HU].*"))) return "rtx3050_laptop";
        if (u.contains("RTX 3050"))                      return "rtx3050";

        // ── NVIDIA RTX 20 series Desktop ─────────────────────────────────────
        if (u.contains("RTX 2080 TI") || u.contains("RTX 2080TI")) return "rtx2080ti";
        if (u.contains("RTX 2080 SUPER"))                return "rtx2080super";
        if (u.contains("RTX 2080"))                      return "rtx2080";
        if (u.contains("RTX 2070 SUPER"))                return "rtx2070super";
        if (u.contains("RTX 2070"))                      return "rtx2070";
        if (u.contains("RTX 2060 SUPER"))                return "rtx2060super";
        if (u.contains("RTX 2060"))                      return "rtx2060";

        // ── NVIDIA GTX 16/10 series ───────────────────────────────────────────
        if (u.contains("GTX 1660 TI") || u.contains("GTX 1660TI")) return "gtx1660ti";
        if (u.contains("GTX 1660 SUPER"))                return "gtx1660super";
        if (u.contains("GTX 1660"))                      return "gtx1660";
        if (u.contains("GTX 1650 SUPER"))                return "gtx1650super";
        if (u.contains("GTX 1650") && (u.contains("LAPTOP") || u.contains("MOBILE"))) return "gtx1650_laptop";
        if (u.contains("GTX 1650"))                      return "gtx1650";
        if (u.contains("GTX 1080 TI") || u.contains("GTX 1080TI")) return "gtx1080ti";
        if (u.contains("GTX 1080"))                      return "gtx1080";
        if (u.contains("GTX 1070 TI") || u.contains("GTX 1070TI")) return "gtx1070ti";
        if (u.contains("GTX 1070"))                      return "gtx1070";
        if (u.contains("GTX 1060 6GB") || u.contains("GTX1060 6GB")) return "gtx1060_6gb";
        if (u.contains("GTX 1060"))                      return "gtx1060";
        if (u.contains("GTX 1050 TI") || u.contains("GTX 1050TI")) return "gtx1050ti";
        if (u.contains("GTX 1050") && (u.contains("LAPTOP") || u.contains("MOBILE"))) return "gtx1050_laptop";
        if (u.contains("GTX 1050"))                      return "gtx1050";

        // ── AMD RX 7000 series ────────────────────────────────────────────────
        if (u.contains("RX 7900 XTX"))                   return "rx7900xtx";
        if (u.contains("RX 7900 XT"))                    return "rx7900xt";
        if (u.contains("RX 7800 XT"))                    return "rx7800xt";
        if (u.contains("RX 7700 XT"))                    return "rx7700xt";
        if (u.contains("RX 7600"))                       return "rx7600";

        // ── AMD RX 6000 series ────────────────────────────────────────────────
        if (u.contains("RX 6900 XT"))                    return "rx6900xt";
        if (u.contains("RX 6800 XT"))                    return "rx6800xt";
        if (u.contains("RX 6800"))                       return "rx6800";
        if (u.contains("RX 6700 XT"))                    return "rx6700xt";
        if (u.contains("RX 6700"))                       return "rx6700";
        if (u.contains("RX 6650 XT"))                    return "rx6650xt";
        if (u.contains("RX 6600 XT"))                    return "rx6600xt";
        if (u.contains("RX 6600"))                       return "rx6600";
        if (u.contains("RX 6500 XT"))                    return "rx6500xt";

        // ── AMD RX 5000 series ────────────────────────────────────────────────
        if (u.contains("RX 5700 XT"))                    return "rx5700xt";
        if (u.contains("RX 5700"))                       return "rx5700";
        if (u.contains("RX 5600 XT"))                    return "rx5600xt";
        if (u.contains("RX 5500 XT"))                    return "rx5500xt";

        // ── AMD RX 500/400 series ─────────────────────────────────────────────
        if (u.contains("RX 590"))                        return "rx590";
        if (u.contains("RX 580"))                        return "rx580";
        if (u.contains("RX 570"))                        return "rx570";
        if (u.contains("RX 480"))                        return "rx580"; // treat as rx580 equivalent
        if (u.contains("RX 470"))                        return "rx570";

        // ── Intel Arc ────────────────────────────────────────────────────────
        if (u.contains("ARC A770"))                      return "arc_a770";
        if (u.contains("ARC A750"))                      return "arc_a750";
        if (u.contains("ARC A580"))                      return "arc_a580";
        if (u.contains("ARC B580"))                      return "arc_b580";
        if (u.contains("ARC B570"))                      return "arc_b570";

        // ── iGPU ─────────────────────────────────────────────────────────────
        if (u.contains("IRIS") || u.contains("INTEL HD") || u.contains("INTEL UHD")
         || u.contains("780M") || u.contains("890M") || u.contains("680M")
         || u.contains("760M") || u.contains("VEGA") || u.contains("INTEGRATED"))
            return "igpu";

        return "unknown";
    }

    // =========================================================================
    //  GAME NORMALIZATION
    // =========================================================================

    public static String normalizeGame(String gameName) {
        if (gameName == null) return "unknown";
        String n = gameName.toLowerCase().trim();

        if (n.contains("cyberpunk") && n.contains("2077"))   return "cyberpunk2077";
        if (n.contains("elden ring") && n.contains("nightreign")) return "nightreign";
        if (n.contains("elden ring"))                          return "elden_ring";
        if (n.contains("red dead redemption 2") || n.contains("rdr2")) return "rdr2";
        if (n.contains("the witcher 3") || n.contains("witcher 3")) return "witcher3";
        if (n.contains("god of war") && n.contains("ragnarok")) return "gow_ragnarok";
        if (n.contains("god of war") && !n.contains("ragnarok")) return "gow_2018";
        if (n.contains("baldur's gate 3") || n.contains("baldurs gate 3")) return "bg3";
        if (n.contains("black myth") || n.contains("wukong"))  return "black_myth_wukong";
        if (n.contains("resident evil 4") || (n.contains("re4") && n.contains("remake"))) return "re4_remake";
        if (n.contains("resident evil 2"))                     return "re2_remake";
        if (n.contains("resident evil village") || n.contains("re8")) return "re_village";
        if (n.contains("resident evil 3"))                     return "re3_remake";
        if (n.contains("the last of us") && (n.contains("part i") || n.contains("part 1"))) return "tlou_1";
        if (n.contains("hogwarts legacy"))                     return "hogwarts_legacy";
        if (n.contains("starfield"))                           return "starfield";
        if (n.contains("alan wake 2") || n.contains("alan wake2")) return "alan_wake2";
        if (n.contains("horizon zero dawn"))                   return "horizon_zero_dawn";
        if (n.contains("horizon forbidden west"))              return "horizon_fw";
        if (n.contains("spider-man remastered") || n.contains("spiderman remastered")) return "spiderman_remastered";
        if (n.contains("spider-man: miles morales") || n.contains("miles morales")) return "spiderman_mm";
        if (n.contains("ghost of tsushima"))                   return "ghost_of_tsushima";
        if (n.contains("death stranding"))                     return "death_stranding";
        if (n.contains("sekiro"))                              return "sekiro";
        if (n.contains("dark souls iii") || n.contains("dark souls 3")) return "dark_souls3";
        if (n.contains("dark souls") && (n.contains("ii") || n.contains("2"))) return "dark_souls2";
        if (n.contains("dark souls"))                          return "dark_souls1";
        if (n.contains("counter-strike 2") || n.contains("cs2")) return "cs2";
        if (n.contains("counter-strike: global") || n.contains("cs:go") || n.contains("csgo")) return "csgo";
        if (n.contains("minecraft"))                           return "minecraft";
        if (n.contains("project zomboid"))                     return "project_zomboid";
        if (n.contains("gta v") || n.contains("grand theft auto v") || n.contains("gta5")) return "gta5";
        if (n.contains("fortnite"))                            return "fortnite";
        if (n.contains("apex legends"))                        return "apex_legends";
        if (n.contains("valorant"))                            return "valorant";
        if (n.contains("doom eternal"))                        return "doom_eternal";
        if (n.contains("doom 2016") || (n.contains("doom") && n.contains("2016"))) return "doom_2016";
        if (n.contains("metro exodus"))                        return "metro_exodus";
        if (n.contains("control"))                             return "control";
        if (n.contains("forza horizon 5"))                     return "forza_h5";
        if (n.contains("forza horizon 4"))                     return "forza_h4";
        if (n.contains("monster hunter: world") || n.contains("monster hunter world")) return "mhworld";
        if (n.contains("monster hunter rise"))                 return "mh_rise";
        if (n.contains("shadow of the tomb raider"))           return "shadow_tomb_raider";
        if (n.contains("rise of the tomb raider"))             return "rise_tomb_raider";
        if (n.contains("total war: warhammer iii") || n.contains("total war warhammer 3")) return "tw_warhammer3";
        if (n.contains("civilization vi") || n.contains("civ 6") || n.contains("civilization 6")) return "civ6";
        if (n.contains("dota 2") || n.contains("dota2"))       return "dota2";
        if (n.contains("stardew valley"))                      return "stardew";
        if (n.contains("terraria"))                            return "terraria";
        if (n.contains("among us"))                            return "among_us";
        if (n.contains("indiana jones"))                       return "indiana_jones";
        if (n.contains("armored core vi") || n.contains("armored core 6")) return "ac6";
        if (n.contains("dark and darker"))                     return "dark_and_darker";
        if (n.contains("helldivers 2") || n.contains("helldivers ii")) return "helldivers2";
        if (n.contains("palworld"))                            return "palworld";
        if (n.contains("dragons dogma 2") || n.contains("dragon's dogma 2")) return "dd2";
        if (n.contains("hellblade ii") || n.contains("senua's saga")) return "hellblade2";

        return "unknown";
    }

    // =========================================================================
    //  BENCHMARK DATABASE
    //  Sumber: Digital Foundry, TechPowerUp reviews, GamersNexus, Jarrod's Tech,
    //          Testing Games YT, komunitas Steam/Reddit (2023-2024).
    //  Semua data di 1080p kecuali disebutkan lain.
    //  FPS = avg sustained (bukan burst), sudah memperhitungkan throttle laptop.
    // =========================================================================

    private static final List<BenchmarkEntry> DB = new ArrayList<>();

    static {
        // ── CYBERPUNK 2077 ────────────────────────────────────────────────────
        add("rtx4090",         "cyberpunk2077", "Cyberpunk 2077", 165, 130, "Ultra+RT",     "1440p", "RT Overdrive butuh 4090 agar playable 4K");
        add("rtx4080",         "cyberpunk2077", "Cyberpunk 2077", 128, 98,  "Ultra+RT",     "1440p", "Perlu DLSS Quality agar 60+ di 4K");
        add("rtx4070super",    "cyberpunk2077", "Cyberpunk 2077", 105, 82,  "Ultra RT off", "1440p", "DLSS Performance direkomendasikan untuk RT");
        add("rtx4070",         "cyberpunk2077", "Cyberpunk 2077", 88,  68,  "High",         "1080p", "RT off, DLSS Quality untuk stabilitas");
        add("rtx4060ti",       "cyberpunk2077", "Cyberpunk 2077", 78,  60,  "High",         "1080p", "DLSS Quality sangat direkomendasikan");
        add("rtx4060",         "cyberpunk2077", "Cyberpunk 2077", 65,  50,  "Medium-High",  "1080p", "RT off, DLSS direkomendasikan, VRAM 8GB aman");
        add("rtx4050_laptop",  "cyberpunk2077", "Cyberpunk 2077", 48,  36,  "Medium",       "1080p", "DLSS Quality wajib, RT off");
        add("rtx3090",         "cyberpunk2077", "Cyberpunk 2077", 118, 90,  "Ultra RT",     "1440p", "DLSS Quality untuk RT stabil");
        add("rtx3080",         "cyberpunk2077", "Cyberpunk 2077", 95,  72,  "Ultra RT off", "1440p", "Perlu DLSS untuk RT");
        add("rtx3070",         "cyberpunk2077", "Cyberpunk 2077", 72,  55,  "High",         "1080p", "RT off, DLSS Quality 1440p layak");
        add("rtx3060ti",       "cyberpunk2077", "Cyberpunk 2077", 68,  52,  "High",         "1080p", "RT off, DLSS direkomendasikan");
        add("rtx3060",         "cyberpunk2077", "Cyberpunk 2077", 55,  42,  "Medium",       "1080p", "VRAM 12GB membantu, DLSS Quality wajib");
        add("rtx3050_laptop",  "cyberpunk2077", "Cyberpunk 2077", 38,  28,  "Medium-Low",   "1080p", "DLSS wajib, RT off, area padat drop ke 25 FPS");
        add("rtx3050",         "cyberpunk2077", "Cyberpunk 2077", 45,  34,  "Medium",       "1080p", "DLSS Quality, RT off, VRAM 8GB aman");
        add("rtx2070super",    "cyberpunk2077", "Cyberpunk 2077", 62,  47,  "High RT off",  "1080p", "RT minimal saja, DLSS Quality 1440p");
        add("rtx2060",         "cyberpunk2077", "Cyberpunk 2077", 50,  38,  "Medium",       "1080p", "DLSS Quality, RT off, VRAM 6GB mepet Ultra texture");
        add("gtx1660super",    "cyberpunk2077", "Cyberpunk 2077", 42,  32,  "Low-Medium",   "1080p", "No RT, FSR 2 direkomendasikan");
        add("gtx1060_6gb",     "cyberpunk2077", "Cyberpunk 2077", 28,  20,  "Low",          "1080p", "FSR 2 Performance wajib, area padat <20 FPS");
        add("rx7800xt",        "cyberpunk2077", "Cyberpunk 2077", 98,  75,  "Ultra RT off", "1440p", "FSR 3 Frame Gen bagus di sini");
        add("rx6700xt",        "cyberpunk2077", "Cyberpunk 2077", 70,  53,  "High",         "1080p", "FSR 2 Quality direkomendasikan");
        add("rx6600",          "cyberpunk2077", "Cyberpunk 2077", 52,  39,  "Medium",       "1080p", "FSR 2, VRAM 8GB cukup");

        // ── ELDEN RING ────────────────────────────────────────────────────────
        add("rtx4090",         "elden_ring", "Elden Ring", 155, 90,  "Max",  "1440p", "CPU-bottleneck di Leyndell, Malenia ~80 FPS drop");
        add("rtx4080",         "elden_ring", "Elden Ring", 140, 85,  "Max",  "1440p", "CPU bound, GPU sangat longgar");
        add("rtx4070",         "elden_ring", "Elden Ring", 120, 75,  "Max",  "1440p", "Frame pacing issues engine FromSoftware masih ada");
        add("rtx4060",         "elden_ring", "Elden Ring", 110, 68,  "Max",  "1080p", "Limgrave ~120, Leyndell ~85, Malenia ~60");
        add("rtx3070",         "elden_ring", "Elden Ring", 105, 65,  "Max",  "1080p", "GPU sangat longgar, CPU bound di open world");
        add("rtx3060",         "elden_ring", "Elden Ring", 95,  58,  "Max",  "1080p", "Smooth, sesekali frame pacing hitch");
        add("rtx3060ti",       "elden_ring", "Elden Ring", 100, 62,  "Max",  "1080p", "Sangat smooth, hanya Malenia/Farum ada drop");
        add("rtx3050_laptop",  "elden_ring", "Elden Ring", 55,  38,  "High", "1080p", "Laptop 80W. Open world ~60, boss ~45. Throttle di Caelid");
        add("rtx3050",         "elden_ring", "Elden Ring", 75,  50,  "Max",  "1080p", "Smooth di sebagian besar area, Leyndell bisa 55");
        add("rtx2060",         "elden_ring", "Elden Ring", 82,  52,  "High", "1080p", "Bagus, frame pacing occasional");
        add("gtx1660super",    "elden_ring", "Elden Ring", 68,  44,  "High", "1080p", "Layak, beberapa area open world bisa 50 FPS");
        add("gtx1060_6gb",     "elden_ring", "Elden Ring", 48,  30,  "Med",  "1080p", "Beberapa area drop ke 30-35, Medium settings");
        add("gtx1050ti",       "elden_ring", "Elden Ring", 35,  22,  "Low",  "1080p", "Barely playable, Low settings, banyak drop");
        add("rx7800xt",        "elden_ring", "Elden Ring", 120, 72,  "Max",  "1440p", "AMD performa baik di game ini");
        add("rx6700xt",        "elden_ring", "Elden Ring", 100, 62,  "Max",  "1080p", "Smooth, mirip RTX 3070");
        add("rx6600",          "elden_ring", "Elden Ring", 82,  52,  "Max",  "1080p", "Bagus, area terbuka >75 FPS");

        // ── ELDEN RING NIGHTREIGN ─────────────────────────────────────────────
        add("rtx4090",         "nightreign", "Elden Ring: Nightreign", 150, 95,  "Max", "1440p", "Co-op 3 player efek spell berat di late run");
        add("rtx4070",         "nightreign", "Elden Ring: Nightreign", 120, 75,  "Max", "1080p", "Night phase dengan 3 pemain ~80 FPS");
        add("rtx4060",         "nightreign", "Elden Ring: Nightreign", 100, 62,  "Max", "1080p", "Smooth, co-op berat di Nightlord fight");
        add("rtx3060ti",       "nightreign", "Elden Ring: Nightreign", 95,  58,  "Max", "1080p", "Mirip Elden Ring base, co-op lebih GPU intensive");
        add("rtx3060",         "nightreign", "Elden Ring: Nightreign", 80,  50,  "Max", "1080p", "Smooth, Nightlord ~65 FPS");
        add("rtx3050_laptop",  "nightreign", "Elden Ring: Nightreign", 48,  32,  "High", "1080p", "Laptop TDP 80W. Co-op berat ~35-40 FPS saat efek banyak");
        add("rtx3050",         "nightreign", "Elden Ring: Nightreign", 65,  42,  "High", "1080p", "Playable, Nightlord fight bisa 50 FPS");
        add("gtx1660super",    "nightreign", "Elden Ring: Nightreign", 60,  38,  "High", "1080p", "Layak, co-op berat drop ke 45");
        add("rx6700xt",        "nightreign", "Elden Ring: Nightreign", 95,  58,  "Max", "1080p", "Setara RTX 3070");

        // ── RED DEAD REDEMPTION 2 ─────────────────────────────────────────────
        add("rtx4090",         "rdr2", "Red Dead Redemption 2", 145, 110, "Max+Ultra", "1440p", "Saint Denis CPU-bound, ambil RTX 4090 untuk 4K Max");
        add("rtx4080",         "rdr2", "Red Dead Redemption 2", 125, 95,  "Max",  "1440p", "Excellent, hanya Saint Denis CPU-limited");
        add("rtx4070",         "rdr2", "Red Dead Redemption 2", 100, 76,  "Ultra", "1440p", "Ultra di 1440p sangat bagus");
        add("rtx4060",         "rdr2", "Red Dead Redemption 2", 88,  66,  "High", "1080p", "Outdoor ~95, Saint Denis ~68");
        add("rtx3070",         "rdr2", "Red Dead Redemption 2", 90,  68,  "High", "1080p", "Sangat smooth, Ultra feasible dengan sedikit tweak");
        add("rtx3060",         "rdr2", "Red Dead Redemption 2", 72,  54,  "High", "1080p", "Saint Denis bisa 55, Heartlands ~80");
        add("rtx3060ti",       "rdr2", "Red Dead Redemption 2", 80,  60,  "Ultra","1080p", "Smooth, hanya Saint Denis sedikit drop");
        add("rtx3050_laptop",  "rdr2", "Red Dead Redemption 2", 40,  28,  "Med-High","1080p","CPU-bound, Saint Denis bisa 28-32 FPS. Medium recommended");
        add("rtx3050",         "rdr2", "Red Dead Redemption 2", 55,  40,  "High", "1080p", "Smooth outdoor, Saint Denis 40-45");
        add("rtx2060",         "rdr2", "Red Dead Redemption 2", 62,  46,  "High", "1080p", "Bagus, Ambarino snowstorm ~50");
        add("gtx1660super",    "rdr2", "Red Dead Redemption 2", 55,  40,  "Med-High","1080p","Playable, turunkan Volumetrics dan Shadow");
        add("gtx1060_6gb",     "rdr2", "Red Dead Redemption 2", 40,  28,  "Medium","1080p","Medium di kebanyakan area, Saint Denis harus Low");
        add("rx6700xt",        "rdr2", "Red Dead Redemption 2", 88,  65,  "High", "1080p", "AMD sangat scalable di RDR2");
        add("rx6600",          "rdr2", "Red Dead Redemption 2", 65,  48,  "High", "1080p", "Smooth, Saint Denis sedikit turun");

        // ── WITCHER 3 NEXT GEN ────────────────────────────────────────────────
        add("rtx4070",         "witcher3", "The Witcher 3", 95,  72,  "Ultra+", "1440p", "Next Gen patch, DLSS Quality");
        add("rtx4060",         "witcher3", "The Witcher 3", 82,  62,  "Ultra",  "1080p", "Novigrad ~70, Velen ~90");
        add("rtx3060",         "witcher3", "The Witcher 3", 65,  48,  "High",   "1080p", "DLSS Quality untuk Ultra");
        add("rtx3050_laptop",  "witcher3", "The Witcher 3", 45,  32,  "Med",    "1080p", "Medium-High feasible, RT off, Novigrad ~38");
        add("rtx3050",         "witcher3", "The Witcher 3", 58,  42,  "High",   "1080p", "Smooth di luar kota, Novigrad ~48");
        add("gtx1660super",    "witcher3", "The Witcher 3", 55,  40,  "High",   "1080p", "Next Gen patch, tanpa RT");
        add("gtx1060_6gb",     "witcher3", "The Witcher 3", 42,  30,  "Med",    "1080p", "Medium settings, DLSS tidak tersedia untuk GTX");
        add("rx6700xt",        "witcher3", "The Witcher 3", 78,  58,  "Ultra",  "1080p", "Bagus, Novigrad ~65");

        // ── GOD OF WAR (2018) ─────────────────────────────────────────────────
        add("rtx4060",         "gow_2018", "God of War", 120, 88,  "Ultra+", "1080p", "Sangat dioptimasi, mudah 60+ FPS");
        add("rtx3060",         "gow_2018", "God of War", 110, 80,  "Ultra+", "1080p", "Sangat smooth, hampir locked 60 di semua area");
        add("rtx3050_laptop",  "gow_2018", "God of War", 68,  50,  "High",   "1080p", "Bagus, boss fight ~55 FPS");
        add("rtx3050",         "gow_2018", "God of War", 85,  62,  "Ultra",  "1080p", "Smooth, area luas stabil 75+");
        add("gtx1660super",    "gow_2018", "God of War", 80,  58,  "High",   "1080p", "Locked 60 feasible dengan beberapa setting tweak");
        add("gtx1060_6gb",     "gow_2018", "God of War", 55,  40,  "Med",    "1080p", "Medium-High, boss fight ~42");
        add("rx6600",          "gow_2018", "God of War", 100, 72,  "Ultra",  "1080p", "AMD performa sangat baik di game ini");

        // ── GOD OF WAR RAGNAROK ───────────────────────────────────────────────
        add("rtx4090",         "gow_ragnarok", "God of War Ragnarok", 155, 115, "Ultra+", "1440p", "Port PC sangat dioptimasi");
        add("rtx4070",         "gow_ragnarok", "God of War Ragnarok", 118, 88,  "Ultra+", "1440p", "DLSS Quality 4K layak");
        add("rtx4060",         "gow_ragnarok", "God of War Ragnarok", 100, 75,  "Ultra",  "1080p", "Smooth, Asgard fight ~80");
        add("rtx3060",         "gow_ragnarok", "God of War Ragnarok", 82,  60,  "High",   "1080p", "DLSS Quality untuk Ultra 1440p");
        add("rtx3050_laptop",  "gow_ragnarok", "God of War Ragnarok", 58,  42,  "High",   "1080p", "Laptop. Asgard heavy fight ~45. Smooth di eksplorasi");
        add("rtx3050",         "gow_ragnarok", "God of War Ragnarok", 72,  52,  "High",   "1080p", "Playable, boss fight ~55");
        add("gtx1660super",    "gow_ragnarok", "God of War Ragnarok", 62,  45,  "Med-High","1080p","Playable dengan setting medium, GPU bukan bottleneck utama");
        add("rx6700xt",        "gow_ragnarok", "God of War Ragnarok", 98,  72,  "Ultra",  "1080p", "AMD excellent di game Sony");

        // ── BALDUR'S GATE 3 ───────────────────────────────────────────────────
        add("rtx4080",         "bg3", "Baldur's Gate 3", 120, 85,  "Ultra",  "1440p", "Act 3 Lower City ~85, sangat CPU intensive");
        add("rtx4070",         "bg3", "Baldur's Gate 3", 95,  65,  "High",   "1440p", "Act 3 bisa 60, CPU bottleneck jelas");
        add("rtx4060",         "bg3", "Baldur's Gate 3", 80,  55,  "High",   "1080p", "Act 3 ~60-65, Act 1 ~90");
        add("rtx3070",         "bg3", "Baldur's Gate 3", 78,  52,  "High",   "1080p", "Act 3 Lower City ~55, CPU-bound");
        add("rtx3060",         "bg3", "Baldur's Gate 3", 65,  44,  "Med-High","1080p","Act 3 bisa turun 45, gunakan Medium untuk stabilitas");
        add("rtx3050_laptop",  "bg3", "Baldur's Gate 3", 42,  28,  "Med",    "1080p", "Act 3 bisa 30-35 FPS, CPU laptop jadi bottleneck, Medium wajib");
        add("rtx3050",         "bg3", "Baldur's Gate 3", 52,  36,  "Med",    "1080p", "Act 3 ~40, Act 1 ~62");
        add("gtx1660super",    "bg3", "Baldur's Gate 3", 48,  32,  "Med",    "1080p", "Playable tapi Act 3 sangat berat");
        add("rx6700xt",        "bg3", "Baldur's Gate 3", 72,  48,  "High",   "1080p", "CPU-bound di Act 3, mirip RTX 3060Ti");

        // ── BLACK MYTH: WUKONG ────────────────────────────────────────────────
        add("rtx4090",         "black_myth_wukong", "Black Myth: Wukong", 138, 98,  "Cinematic", "1440p", "DLSS Frame Gen untuk 4K Cinematic");
        add("rtx4080",         "black_myth_wukong", "Black Myth: Wukong", 115, 82,  "High RT",   "1440p", "DLSS Quality 4K layak");
        add("rtx4070super",    "black_myth_wukong", "Black Myth: Wukong", 95,  68,  "High",      "1440p", "DLSS Quality 1440p sangat smooth");
        add("rtx4070",         "black_myth_wukong", "Black Myth: Wukong", 78,  55,  "High",      "1080p", "DLSS Quality wajib, RT off");
        add("rtx4060ti",       "black_myth_wukong", "Black Myth: Wukong", 68,  48,  "Med-High",  "1080p", "DLSS Quality, RT off, UE5 berat");
        add("rtx4060",         "black_myth_wukong", "Black Myth: Wukong", 58,  40,  "Medium",    "1080p", "DLSS Quality wajib, RT off, Guangmou boss ~45");
        add("rtx3070",         "black_myth_wukong", "Black Myth: Wukong", 62,  44,  "Med-High",  "1080p", "DLSS Quality, UE5 Lumen sangat berat");
        add("rtx3060",         "black_myth_wukong", "Black Myth: Wukong", 50,  35,  "Medium",    "1080p", "DLSS Quality, Lumen off, boss fight ~38");
        add("rtx3060ti",       "black_myth_wukong", "Black Myth: Wukong", 56,  40,  "Medium",    "1080p", "DLSS Quality wajib, Lumen off");
        add("rtx3050_laptop",  "black_myth_wukong", "Black Myth: Wukong", 30,  20,  "Low-Med",   "1080p", "UE5 sangat berat di laptop. FSR 2 Performance wajib, Lumen off");
        add("rtx3050",         "black_myth_wukong", "Black Myth: Wukong", 38,  26,  "Low-Med",   "1080p", "FSR 2 wajib, Lumen off, boss fight ~28");
        add("rx7800xt",        "black_myth_wukong", "Black Myth: Wukong", 75,  52,  "High",      "1080p", "FSR 3 Frame Gen bagus");
        add("rx6700xt",        "black_myth_wukong", "Black Myth: Wukong", 55,  38,  "Medium",    "1080p", "FSR 2, Lumen off, area outdoor ~62");
        add("gtx1660super",    "black_myth_wukong", "Black Myth: Wukong", 32,  22,  "Low",       "1080p", "FSR 2 Performance, Lumen off, barely playable");

        // ── RE4 REMAKE ────────────────────────────────────────────────────────
        add("rtx4070",         "re4_remake", "Resident Evil 4 Remake", 130, 98,  "Max+RT", "1440p", "RE Engine sangat dioptimasi");
        add("rtx4060",         "re4_remake", "Resident Evil 4 Remake", 118, 88,  "Max",    "1080p", "DLSS/FSR opsional saja");
        add("rtx3060",         "re4_remake", "Resident Evil 4 Remake", 100, 76,  "Max RT off","1080p","Desa ~88, Kastil ~110. RT on drop ~25%");
        add("rtx3060ti",       "re4_remake", "Resident Evil 4 Remake", 108, 82,  "Max",    "1080p", "Sangat smooth, RT acceptable");
        add("rtx3050_laptop",  "re4_remake", "Resident Evil 4 Remake", 65,  48,  "High",   "1080p", "RE Engine efisien. Desa ~58 FPS, kastil ~72");
        add("rtx3050",         "re4_remake", "Resident Evil 4 Remake", 82,  62,  "Max",    "1080p", "Smooth, RT off untuk stabilitas");
        add("gtx1660super",    "re4_remake", "Resident Evil 4 Remake", 75,  55,  "High",   "1080p", "RE Engine ramah hardware lama");
        add("gtx1060_6gb",     "re4_remake", "Resident Evil 4 Remake", 55,  40,  "Med",    "1080p", "VRAM 6GB aman di Medium");
        add("rx6700xt",        "re4_remake", "Resident Evil 4 Remake", 112, 85,  "Max",    "1080p", "RE Engine sangat baik di AMD");
        add("rx6600",          "re4_remake", "Resident Evil 4 Remake", 92,  68,  "Max",    "1080p", "Smooth, RE Engine efisien");

        // ── RESIDENT EVIL VILLAGE ─────────────────────────────────────────────
        add("rtx3060",         "re_village", "Resident Evil Village", 115, 88,  "Max+RT", "1080p", "RE Engine efisien, RT ringan");
        add("rtx3050_laptop",  "re_village", "Resident Evil Village", 72,  54,  "High",   "1080p", "Smooth, RT off untuk laptopmu");
        add("gtx1660super",    "re_village", "Resident Evil Village", 90,  68,  "High",   "1080p", "Bagus tanpa RT");
        add("rx6600",          "re_village", "Resident Evil Village", 108, 80,  "High",   "1080p", "AMD sangat bagus di RE Engine");

        // ── SEKIRO ────────────────────────────────────────────────────────────
        add("rtx3050_laptop",  "sekiro", "Sekiro", 60, 52, "Max", "1080p", "Sangat dioptimasi. Flat 60 FPS di hampir semua area. Engine FromSoftware efisien");
        add("rtx3050",         "sekiro", "Sekiro", 60, 55, "Max", "1080p", "Mudah flat 60, GPU sangat longgar");
        add("gtx1660super",    "sekiro", "Sekiro", 60, 52, "Max", "1080p", "Flat 60 stable, GPU ini lebih dari cukup");
        add("gtx1060_6gb",     "sekiro", "Sekiro", 60, 48, "High","1080p", "Locked 60 tercapai, sesekali hitch di area loading");
        add("gtx1050ti",       "sekiro", "Sekiro", 55, 40, "Med", "1080p", "Hampir 60, beberapa area bisa 48-52");
        add("rx6600",          "sekiro", "Sekiro", 60, 54, "Max", "1080p", "Flat 60 mudah");
        add("rtx4060",         "sekiro", "Sekiro", 60, 55, "Max", "1080p", "GPU ini jauh overkill. Locked 60 semua area");

        // ── HOGWARTS LEGACY ───────────────────────────────────────────────────
        add("rtx4080",         "hogwarts_legacy", "Hogwarts Legacy", 120, 90,  "High",  "1440p", "Ultra sangat berat, High optimal");
        add("rtx4070",         "hogwarts_legacy", "Hogwarts Legacy", 95,  70,  "High",  "1440p", "DLSS Quality direkomendasikan");
        add("rtx4060",         "hogwarts_legacy", "Hogwarts Legacy", 75,  55,  "High",  "1080p", "DLSS Quality, outdoors ~65, kastil ~82");
        add("rtx3070",         "hogwarts_legacy", "Hogwarts Legacy", 72,  52,  "Med-High","1080p","DLSS wajib untuk High");
        add("rtx3060",         "hogwarts_legacy", "Hogwarts Legacy", 58,  42,  "Medium","1080p", "DLSS Quality, game ini sangat CPU-intensive");
        add("rtx3060ti",       "hogwarts_legacy", "Hogwarts Legacy", 65,  48,  "Med-High","1080p","DLSS Quality");
        add("rtx3050_laptop",  "hogwarts_legacy", "Hogwarts Legacy", 35,  24,  "Low-Med","1080p","FSR wajib, game berat untuk laptop entry. Hogsmeade ~28");
        add("rtx3050",         "hogwarts_legacy", "Hogwarts Legacy", 45,  32,  "Medium","1080p", "FSR 2 Quality, turunkan shadow");
        add("gtx1660super",    "hogwarts_legacy", "Hogwarts Legacy", 42,  30,  "Low-Med","1080p","FSR 2, game ini dikenal boros GPU");
        add("rx6700xt",        "hogwarts_legacy", "Hogwarts Legacy", 68,  48,  "High",  "1080p", "FSR 2 Quality");

        // ── THE LAST OF US PART I ─────────────────────────────────────────────
        add("rtx4090",         "tlou_1", "The Last of Us Part I", 120, 88,  "Very High", "1440p", "Port PC masih punya stuttering, VRAM 12GB+ ideal");
        add("rtx4070",         "tlou_1", "The Last of Us Part I", 85,  60,  "High",  "1080p", "Shader compilation stutter di awal sesi");
        add("rtx4060",         "tlou_1", "The Last of Us Part I", 68,  48,  "High",  "1080p", "DLSS Quality, shader stutter ada tapi berkurang");
        add("rtx3060",         "tlou_1", "The Last of Us Part I", 52,  36,  "Med",   "1080p", "DLSS wajib, VRAM 12GB sangat membantu");
        add("rtx3050_laptop",  "tlou_1", "The Last of Us Part I", 32,  20,  "Low",   "1080p", "Port bermasalah, VRAM 4GB kurang. FSR wajib. Tidak direkomendasikan");
        add("rx6700xt",        "tlou_1", "The Last of Us Part I", 60,  42,  "High",  "1080p", "Port lebih berat di AMD");

        // ── STARFIELD ────────────────────────────────────────────────────────
        add("rtx4090",         "starfield", "Starfield", 110, 78,  "Ultra",   "1440p", "New Atlantis CPU-bound, planet kosong GPU-bound");
        add("rtx4070",         "starfield", "Starfield", 82,  55,  "High",    "1440p", "DLSS Quality, New Atlantis ~58");
        add("rtx4060",         "starfield", "Starfield", 68,  45,  "High",    "1080p", "New Atlantis ~50, interior ship ~80");
        add("rtx3060",         "starfield", "Starfield", 55,  36,  "Med-High","1080p","DLSS Quality, Creation Engine CPU bottleneck jelas");
        add("rtx3050_laptop",  "starfield", "Starfield", 35,  22,  "Med",     "1080p","New Atlantis <25 FPS, harus Medium. CPU laptop bottleneck parah");
        add("rx6700xt",        "starfield", "Starfield", 65,  42,  "High",    "1080p", "No DLSS di AMD, FSR 2 sebagai gantinya");

        // ── ALAN WAKE 2 ───────────────────────────────────────────────────────
        add("rtx4090",         "alan_wake2", "Alan Wake 2", 110, 80,  "Max Path Trace","1440p","Path tracing butuh 4090, otherwise terlalu berat");
        add("rtx4080",         "alan_wake2", "Alan Wake 2", 88,  65,  "High RT","1440p","DLSS Quality");
        add("rtx4070",         "alan_wake2", "Alan Wake 2", 68,  50,  "Med-High","1080p","DLSS Quality, RT off untuk performa");
        add("rtx4060",         "alan_wake2", "Alan Wake 2", 52,  38,  "Medium", "1080p","DLSS Quality wajib, RT off");
        add("rtx3060",         "alan_wake2", "Alan Wake 2", 40,  28,  "Med-Low","1080p","DLSS Performance, game ini sangat demanding");
        add("rtx3050_laptop",  "alan_wake2", "Alan Wake 2", 22,  14,  "Low",    "1080p","Tidak direkomendasikan, terlalu berat untuk laptop ini");
        add("rx7800xt",        "alan_wake2", "Alan Wake 2", 65,  48,  "High",   "1080p","FSR 3 Frame Gen membantu");

        // ── GOD OF WAR (HORIZON) ──────────────────────────────────────────────
        add("rtx4060",  "horizon_zero_dawn",  "Horizon Zero Dawn",     105, 80, "Ultra", "1080p", "Remastered 2024 sangat dioptimasi");
        add("rtx3060",  "horizon_zero_dawn",  "Horizon Zero Dawn",     92,  70, "Ultra", "1080p", "Smooth, GPU longgar");
        add("rtx3050_laptop", "horizon_zero_dawn", "Horizon Zero Dawn", 65, 48, "High",  "1080p", "Bagus, area dengan efek banyak ~55");
        add("gtx1660super",   "horizon_zero_dawn", "Horizon Zero Dawn", 78,  58, "High", "1080p", "Smooth");
        add("rx6600",   "horizon_zero_dawn",  "Horizon Zero Dawn",     88,  65, "High",  "1080p", "AMD sangat bagus di game ini");

        // ── CS2 ───────────────────────────────────────────────────────────────
        add("rtx4060",         "cs2", "Counter-Strike 2", 300, 180, "High",    "1080p", "CPU-bound, Ryzen 5/i5 modern cukup");
        add("rtx3060",         "cs2", "Counter-Strike 2", 250, 150, "Med-High","1080p","GPU overkill, CPU lebih penting");
        add("rtx3050_laptop",  "cs2", "Counter-Strike 2", 140, 90,  "Medium",  "1080p", "CPU laptop bottleneck, map padat ~95 FPS");
        add("rtx3050",         "cs2", "Counter-Strike 2", 180, 110, "High",    "1080p", "CPU-bound");
        add("gtx1660super",    "cs2", "Counter-Strike 2", 220, 130, "High",    "1080p", "GPU cukup, CPU lebih kritis");
        add("gtx1060_6gb",     "cs2", "Counter-Strike 2", 160, 95,  "Med",     "1080p", "Playable, turunkan setting global shadows");
        add("gtx1050ti",       "cs2", "Counter-Strike 2", 100, 62,  "Low-Med", "1080p", "Playable tapi FPS rendah untuk CS");

        // ── GTA V ────────────────────────────────────────────────────────────
        add("rtx4060",         "gta5", "Grand Theft Auto V", 160, 120, "Very High","1080p","Masih sangat smooth, game tua");
        add("rtx3060",         "gta5", "Grand Theft Auto V", 140, 105, "Very High","1080p","Smooth");
        add("rtx3050_laptop",  "gta5", "Grand Theft Auto V", 88,  65,  "High",    "1080p","Bagus, city traffic ~72");
        add("gtx1660super",    "gta5", "Grand Theft Auto V", 120, 90,  "Very High","1080p","Excellent");
        add("gtx1060_6gb",     "gta5", "Grand Theft Auto V", 90,  68,  "High",    "1080p","Smooth, MSAA x2 okay");
        add("gtx1050ti",       "gta5", "Grand Theft Auto V", 60,  44,  "Normal",  "1080p","Playable, jangan MSAA");
        add("rx6600",          "gta5", "Grand Theft Auto V", 135, 100, "Very High","1080p","Bagus");

        // ── MINECRAFT (Java) ─────────────────────────────────────────────────
        add("rtx4060",         "minecraft", "Minecraft", 200, 120, "Sodium+Iris","1080p","Sodium mod wajib, vanilla JVM sangat tidak efisien");
        add("rtx3060",         "minecraft", "Minecraft", 160, 95,  "Sodium",    "1080p","Sodium/OptiFine wajib, vanilla ~60-90");
        add("rtx3050_laptop",  "minecraft", "Minecraft", 100, 65,  "OptiFine",  "1080p","Vanilla bisa 60, OptiFine/Sodium untuk performa lebih");
        add("gtx1660super",    "minecraft", "Minecraft", 130, 80,  "OptiFine",  "1080p","OptiFine wajib untuk chunk loading smooth");
        add("gtx1060_6gb",     "minecraft", "Minecraft", 100, 62,  "OptiFine",  "1080p","Smooth dengan OptiFine, render 12 chunks");
        add("gtx1050ti",       "minecraft", "Minecraft", 75,  48,  "OptiFine",  "1080p","OptiFine, render 8 chunks");

        // ── PROJECT ZOMBOID ───────────────────────────────────────────────────
        add("rtx4060",         "project_zomboid", "Project Zomboid", 45, 28, "Max", "1080p", "Java engine, GC pause tiap 20-30 detik, Louisville <30 FPS");
        add("rtx3060",         "project_zomboid", "Project Zomboid", 40, 25, "Max", "1080p", "Java GC tetap ada, Louisville berat");
        add("rtx3050_laptop",  "project_zomboid", "Project Zomboid", 35, 20, "High","1080p","Java engine, GC pause ada, Louisville 20-25 FPS, RAM penting");
        add("gtx1660super",    "project_zomboid", "Project Zomboid", 38, 22, "High","1080p","GPU bukan bottleneck, Java/RAM yang jadi masalah");
        add("gtx1060_6gb",     "project_zomboid", "Project Zomboid", 35, 18, "Med", "1080p","Louisville bisa <20 FPS, RAM 16GB sangat direkomendasikan");

        // ── DOOM ETERNAL ────────────────────────────────────────────────────
        add("rtx4060",         "doom_eternal", "DOOM Eternal", 250, 180, "Ultra Nightmare","1080p","Game paling dioptimasi, FPS sangat tinggi");
        add("rtx3060",         "doom_eternal", "DOOM Eternal", 210, 155, "Ultra Nightmare","1080p","Smooth sekali, engine id Tech 7 sangat efisien");
        add("rtx3050_laptop",  "doom_eternal", "DOOM Eternal", 120, 85,  "Ultra",  "1080p","Sangat smooth untuk laptop entry, tidak ada throttle issue");
        add("gtx1660super",    "doom_eternal", "DOOM Eternal", 165, 120, "Ultra Nightmare","1080p","Excellent");
        add("gtx1060_6gb",     "doom_eternal", "DOOM Eternal", 120, 88,  "High",   "1080p","Smooth");
        add("rx6600",          "doom_eternal", "DOOM Eternal", 220, 160, "Ultra Nightmare","1080p","AMD excellent di id Tech");

        // ── FORZA HORIZON 5 ───────────────────────────────────────────────────
        add("rtx4060",         "forza_h5", "Forza Horizon 5", 115, 88,  "Ultra",  "1080p","Sangat dioptimasi");
        add("rtx3060",         "forza_h5", "Forza Horizon 5", 95,  72,  "Ultra",  "1080p","Smooth, FH5 sangat scalable");
        add("rtx3050_laptop",  "forza_h5", "Forza Horizon 5", 62,  46,  "High",   "1080p","Smooth, race grid penuh ~52");
        add("gtx1660super",    "forza_h5", "Forza Horizon 5", 80,  60,  "High",   "1080p","Smooth");
        add("gtx1060_6gb",     "forza_h5", "Forza Horizon 5", 58,  42,  "Med",    "1080p","Playable, turunkan shadow dan environment");
        add("rx6700xt",        "forza_h5", "Forza Horizon 5", 108, 82,  "Ultra",  "1080p","AMD sangat bagus di FH5");

        // ── ARMORED CORE VI ────────────────────────────────────────────────────
        add("rtx4060",  "ac6", "Armored Core VI", 112, 82,  "Max", "1080p", "Sangat dioptimasi, mirip Sekiro performa-wise");
        add("rtx3060",  "ac6", "Armored Core VI", 95,  70,  "Max", "1080p", "Smooth");
        add("rtx3050_laptop", "ac6", "Armored Core VI", 60, 44, "High", "1080p", "Smooth di sebagian besar, arena besar ~50");
        add("gtx1660super",   "ac6", "Armored Core VI", 78,  55, "High", "1080p", "Playable");
        add("rx6600",   "ac6", "Armored Core VI", 90,  65,  "Max", "1080p", "Bagus");

        // ── HELLDIVERS 2 ──────────────────────────────────────────────────────
        add("rtx4060",  "helldivers2", "Helldivers 2", 88,  65,  "High",  "1080p", "Online-dependent, extraction berat ~65");
        add("rtx3060",  "helldivers2", "Helldivers 2", 70,  50,  "Med-High","1080p","Playable, co-op dengan banyak efek ~55");
        add("rtx3050_laptop", "helldivers2", "Helldivers 2", 42, 28, "Med", "1080p", "Playable, turunkan setting. GPU-bound parah");
        add("rx6700xt", "helldivers2", "Helldivers 2", 80,  58,  "High",  "1080p", "FSR 2 direkomendasikan");

        // ── DOTA 2 ────────────────────────────────────────────────────────────
        add("rtx3050_laptop",  "dota2", "Dota 2", 180, 110, "High",  "1080p","CPU-bound di teamfight, GPU sangat longgar");
        add("gtx1660super",    "dota2", "Dota 2", 200, 130, "Max",   "1080p","GPU overkill, CPU yang lebih penting");
        add("gtx1060_6gb",     "dota2", "Dota 2", 160, 100, "High",  "1080p","Smooth");
        add("gtx1050ti",       "dota2", "Dota 2", 120, 75,  "Med",   "1080p","Teamfight ~80 FPS");
        add("rx6600",          "dota2", "Dota 2", 210, 130, "Max",   "1080p","Bagus");

        // ── VALORANT ─────────────────────────────────────────────────────────
        add("rtx3050_laptop",  "valorant", "Valorant", 200, 140, "High",  "1080p","CPU-bound, GPU sangat longgar di Valorant");
        add("gtx1650_laptop",  "valorant", "Valorant", 150, 100, "Med",   "1080p","Playable, CPU laptop bottleneck");
        add("gtx1660super",    "valorant", "Valorant", 300, 200, "Max",   "1080p","Excellent, GPU overkill");
        add("gtx1060_6gb",     "valorant", "Valorant", 220, 145, "High",  "1080p","Smooth");
        add("gtx1050ti",       "valorant", "Valorant", 160, 105, "Med",   "1080p","Playable");

        // ── STARDEW VALLEY ────────────────────────────────────────────────────
        add("igpu",            "stardew", "Stardew Valley", 60, 58, "Max", "1080p", "iGPU lebih dari cukup, game 2D ringan sekali");
        add("gtx1050ti",       "stardew", "Stardew Valley", 60, 59, "Max", "1080p", "Locked 60 mudah");

        // ── FORTNITE ──────────────────────────────────────────────────────────
        add("rtx4060",  "fortnite", "Fortnite", 165, 125, "High Epic","1080p","DLSS Quality, Chapter 5 UE5 berat");
        add("rtx3060",  "fortnite", "Fortnite", 128, 95,  "High",    "1080p","DLSS Quality");
        add("rtx3050_laptop", "fortnite", "Fortnite", 80, 58, "Medium","1080p","DLSS/FSR direkomendasikan, late game fight ~60");
        add("gtx1660super",   "fortnite", "Fortnite", 105, 78,  "High", "1080p","FSR 2");
        add("gtx1060_6gb",    "fortnite", "Fortnite", 75,  54,  "Med",  "1080p","Medium settings");

        // ── APEX LEGENDS ─────────────────────────────────────────────────────
        add("rtx3060",  "apex_legends", "Apex Legends", 175, 120, "High",  "1080p","CPU-bound di late ring");
        add("rtx3050_laptop", "apex_legends", "Apex Legends", 110, 72, "Med-High","1080p","Smooth, CPU laptop bottleneck di full lobby");
        add("gtx1660super",   "apex_legends", "Apex Legends", 148, 100, "High", "1080p","Excellent");
        add("gtx1060_6gb",    "apex_legends", "Apex Legends", 110, 72,  "Med",  "1080p","Playable");

        // ── MONSTER HUNTER WORLD ─────────────────────────────────────────────
        add("rtx3060",  "mhworld", "Monster Hunter: World", 88, 65, "Max", "1080p","CPU-bound di Ancient Forest loading");
        add("rtx3050_laptop", "mhworld", "Monster Hunter: World", 58, 42, "High","1080p","Playable, Coral Highlands ~50");
        add("gtx1660super",   "mhworld", "Monster Hunter: World", 75, 55, "High","1080p","Smooth");
        add("rx6600",   "mhworld", "Monster Hunter: World", 82, 60, "Max", "1080p","Bagus");

        // ── DRAGON'S DOGMA 2 ─────────────────────────────────────────────────
        add("rtx4080",  "dd2", "Dragon's Dogma 2", 85,  58,  "High",   "1440p","CPU-bound parah, kota dengan NPC banyak ~38 FPS");
        add("rtx4070",  "dd2", "Dragon's Dogma 2", 72,  48,  "High",   "1080p","Kota ~45, hutan ~68");
        add("rtx4060",  "dd2", "Dragon's Dogma 2", 58,  38,  "Med-High","1080p","Kota ~35-40 FPS, CPU bottleneck RE Engine khusus ini");
        add("rtx3060",  "dd2", "Dragon's Dogma 2", 48,  30,  "Medium", "1080p","Kota bisa 25-30 FPS, game ini notorious CPU-bound");
        add("rtx3050_laptop", "dd2", "Dragon's Dogma 2", 30, 18, "Low", "1080p","Tidak direkomendasikan, kota <20 FPS, CPU laptop bottleneck parah");

        // ── PALWORLD ─────────────────────────────────────────────────────────
        add("rtx4060",  "palworld", "Palworld", 80,  58,  "High",  "1080p","UE5, base building area berat");
        add("rtx3060",  "palworld", "Palworld", 65,  45,  "Med-High","1080p","DLSS Quality");
        add("rtx3050_laptop", "palworld", "Palworld", 42, 28, "Med","1080p","Base besar drop ke 25-30 FPS");
        add("gtx1660super",   "palworld", "Palworld", 55, 38, "Med","1080p","FSR direkomendasikan");

        // ── INDIANA JONES ─────────────────────────────────────────────────────
        add("rtx4090",  "indiana_jones", "Indiana Jones", 130, 95,  "Ultra+RT","1440p","id Tech 8 sangat dioptimasi");
        add("rtx4070",  "indiana_jones", "Indiana Jones", 105, 78,  "High RT", "1080p","DLSS Quality");
        add("rtx4060",  "indiana_jones", "Indiana Jones", 88,  65,  "High",    "1080p","Smooth, id Tech efisien");
        add("rtx3060",  "indiana_jones", "Indiana Jones", 72,  52,  "High",    "1080p","DLSS Quality, RT off");
        add("rtx3050_laptop", "indiana_jones", "Indiana Jones", 48, 34, "Med", "1080p","id Tech 8 lebih efisien dari UE5");
        add("rx7800xt", "indiana_jones", "Indiana Jones", 95,  70,  "High",    "1080p","FSR 2 tersedia");

        // ── HELLBLADE II ──────────────────────────────────────────────────────
        add("rtx4090",  "hellblade2", "Senua's Saga: Hellblade II", 85,  60,  "Ultra UE5","1440p","UE5 Nanite+Lumen, game sangat demanding");
        add("rtx4080",  "hellblade2", "Senua's Saga: Hellblade II", 72,  50,  "High",    "1440p","DLSS Quality");
        add("rtx4070",  "hellblade2", "Senua's Saga: Hellblade II", 58,  40,  "Med-High","1080p","DLSS Quality wajib");
        add("rtx4060",  "hellblade2", "Senua's Saga: Hellblade II", 45,  30,  "Medium",  "1080p","DLSS Quality wajib, Lumen off");
        add("rtx3060",  "hellblade2", "Senua's Saga: Hellblade II", 35,  22,  "Low-Med", "1080p","FSR 2 wajib, UE5 sangat berat");
        add("rtx3050_laptop", "hellblade2", "Senua's Saga: Hellblade II", 20, 12, "Low", "1080p","Tidak direkomendasikan, UE5 terlalu berat");
    }

    private static void add(String gpuTier, String gameKey, String gameName,
                            int avg, int low1pct, String setting, String resolution, String notes) {
        DB.add(new BenchmarkEntry(gpuTier, gameKey, gameName, avg, low1pct, setting, resolution, notes));
    }

    // =========================================================================
    //  LOOKUP & INJECT TO PROMPT
    // =========================================================================

    /**
     * Cari data benchmark untuk kombinasi GPU + Game.
     * Kalau tidak ada exact match, cari GPU tier terdekat (fallback chain).
     *
     * @return String siap inject ke prompt, atau "" jika tidak ada data.
     */
    public static String getBenchmarkHint(String gpuName, String gameName) {
        String gpuTier  = normalizeGpu(gpuName);
        String gameKey  = normalizeGame(gameName);

        if (gpuTier.equals("unknown") || gameKey.equals("unknown")) return "";

        // 1. Cari exact match
        BenchmarkEntry exact = findEntry(gpuTier, gameKey);
        if (exact != null) return formatHint(exact, gpuTier, gameName, false);

        // 2. Fallback: cari GPU tier terdekat
        String fallbackTier = getFallbackTier(gpuTier);
        if (fallbackTier != null) {
            BenchmarkEntry fallback = findEntry(fallbackTier, gameKey);
            if (fallback != null) return formatHint(fallback, gpuTier, gameName, true);
        }

        return "";
    }

    /** Cari dua hint sekaligus untuk compare prompt */
    public static String getBenchmarkHints(String gpuName, String game1Name, String game2Name) {
        String hint1 = getBenchmarkHint(gpuName, game1Name);
        String hint2 = getBenchmarkHint(gpuName, game2Name);
        if (hint1.isEmpty() && hint2.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("DATA BENCHMARK KOMUNITAS (ANCHOR WAJIB — JANGAN ABAIKAN):\n");
        if (!hint1.isEmpty()) sb.append(hint1).append("\n");
        if (!hint2.isEmpty()) sb.append(hint2).append("\n");
        sb.append("INSTRUKSI: Data di atas adalah benchmark nyata dari komunitas. Gunakan sebagai anchor " +
                  "estimasi FPS. Jika ada perbedaan hardware minor, interpolasi secara proporsional.\n\n");
        return sb.toString();
    }

    private static BenchmarkEntry findEntry(String gpuTier, String gameKey) {
        for (BenchmarkEntry e : DB) {
            if (e.gpuTier.equals(gpuTier) && e.gameKey.equals(gameKey)) return e;
        }
        return null;
    }

    private static String formatHint(BenchmarkEntry e, String actualGpuTier,
                                     String requestedGameName, boolean isFallback) {
        StringBuilder sb = new StringBuilder();
        if (isFallback) {
            sb.append("• DATA TERDEKAT (interpolasi dari ").append(e.gpuTier)
              .append(" → GPU kamu):\n");
        } else {
            sb.append("• DATA BENCHMARK AKURAT:\n");
        }
        sb.append("  Game    : ").append(e.gameName).append("\n");
        sb.append("  GPU ref : ").append(e.gpuTier).append("\n");
        sb.append("  Avg FPS : ~").append(e.avgFps).append(" FPS\n");
        sb.append("  1% Low  : ~").append(e.lowFps1pct).append(" FPS\n");
        sb.append("  Setting : ").append(e.setting).append(" @ ").append(e.resolution).append("\n");
        sb.append("  Catatan : ").append(e.notes).append("\n");
        if (isFallback) {
            sb.append("  [Sesuaikan FPS ~10-20% naik/turun berdasarkan perbedaan tier GPU]\n");
        }
        return sb.toString();
    }

    /**
     * Fallback tier chain: jika tidak ada data exact, cari tier terdekat.
     * Ini memastikan interpolasi lebih akurat daripada tebakan AI.
     */
    private static String getFallbackTier(String tier) {
        switch (tier) {
            // RTX 40 laptop fallback ke RTX 30 desktop atau laptop
            case "rtx4070_laptop":  return "rtx4060ti";
            case "rtx4060_laptop":  return "rtx3060ti";
            case "rtx4050_laptop":  return "rtx3050_laptop";
            // RTX 40 desktop fallback
            case "rtx4060ti":       return "rtx3070";
            case "rtx4060":         return "rtx3060ti";
            case "rtx4070":         return "rtx3070";
            case "rtx4070super":    return "rtx3080";
            case "rtx4070ti":       return "rtx3080ti";
            case "rtx4080":         return "rtx3090";
            case "rtx4090":         return "rtx3090";
            // RTX 30 fallback
            case "rtx3090ti":       return "rtx3090";
            case "rtx3080ti":       return "rtx3080";
            case "rtx3070ti":       return "rtx3070";
            case "rtx3060ti":       return "rtx3070";
            case "rtx3060":         return "rtx3060ti";
            case "rtx3050":         return "gtx1660super";
            case "rtx3050_laptop":  return "gtx1660super";
            // RTX 20 fallback
            case "rtx2080ti":       return "rtx3080";
            case "rtx2080super":    return "rtx3070";
            case "rtx2080":         return "rtx3070";
            case "rtx2070super":    return "rtx3060ti";
            case "rtx2070":         return "rtx3060ti";
            case "rtx2060super":    return "rtx3060";
            case "rtx2060":         return "rtx3060";
            // GTX fallback
            case "gtx1080ti":       return "rtx2080";
            case "gtx1080":         return "rtx2070";
            case "gtx1070ti":       return "rtx2060super";
            case "gtx1070":         return "rtx2060";
            case "gtx1660ti":       return "gtx1660super";
            case "gtx1660":         return "gtx1660super";
            case "gtx1650super":    return "gtx1650";
            case "gtx1650_laptop":  return "gtx1050ti";
            case "gtx1050_laptop":  return "gtx1050ti";
            case "gtx1060":         return "gtx1060_6gb";
            case "gtx1050":         return "gtx1050ti";
            // AMD RX fallback
            case "rx7900xtx":       return "rtx4090";
            case "rx7900xt":        return "rtx4080";
            case "rx7800xt":        return "rtx4070";
            case "rx7700xt":        return "rtx4060ti";
            case "rx7600":          return "rtx4060";
            case "rx6900xt":        return "rtx3080ti";
            case "rx6800xt":        return "rtx3080";
            case "rx6800":          return "rtx3070ti";
            case "rx6700xt":        return "rtx3060ti";
            case "rx6700":          return "rtx3060ti";
            case "rx6650xt":        return "rtx3060";
            case "rx6600xt":        return "rtx3060";
            case "rx6600":          return "rtx3060";
            case "rx6500xt":        return "gtx1650";
            case "rx5700xt":        return "rtx2070super";
            case "rx5700":          return "rtx2070";
            case "rx5600xt":        return "rtx2060";
            case "rx5500xt":        return "gtx1650super";
            case "rx590":           return "gtx1060_6gb";
            case "rx580":           return "gtx1060_6gb";
            case "rx570":           return "gtx1060";
            // Intel Arc fallback
            case "arc_a770":        return "rtx3060";
            case "arc_a750":        return "rtx3060";
            case "arc_a580":        return "gtx1660super";
            case "arc_b580":        return "rtx3060";
            case "arc_b570":        return "gtx1660super";
            // RTX 50 series (baru, fallback ke generasi sebelumnya)
            case "rtx5090":         return "rtx4090";
            case "rtx5080":         return "rtx4080";
            case "rtx5070ti":       return "rtx4070ti";
            case "rtx5070":         return "rtx4070";
            case "rtx5060ti":       return "rtx4060ti";
            case "rtx5060":         return "rtx4060";
            default:                return null;
        }
    }
}
