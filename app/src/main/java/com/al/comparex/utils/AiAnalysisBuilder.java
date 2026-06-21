package com.al.comparex.utils;

import com.al.comparex.utils.LangPrefs;

import com.al.comparex.data.model.GameDetail;
import com.al.comparex.data.model.SpekUser;
import com.al.comparex.data.model.SteamRequirement;

/**
 * Membangun prompt AI yang akurat, anti-kontradiksi, dan berbasis data benchmark nyata.
 *
 * v3 — Perubahan utama:
 *  1. Resolusi tidak terpatok 1080p — AI menalar dari High 1080p turun bertahap
 *     sampai resolusi terkecil (640x480 / 4:3) jika dibutuhkan
 *  2. AI wajib merekomendasikan display mode: Fullscreen / Borderless / Windowed
 *     beserta alasan dampak performa
 *  3. Tangga resolusi: High 1080p → Medium 1080p → Low 1080p
 *     → Low 900p → Low 720p → Low 480p (4:3) → Low 360p (4:3)
 */
public class AiAnalysisBuilder {

    // =========================================================================
    //  RESOLUTION LADDER — dipakai di kedua system prompt
    // =========================================================================

    private static final String RESOLUTION_LADDER =
        "=== TANGGA RESOLUSI & DISPLAY MODE ===\n" +
        "AI WAJIB menalar performa secara bertahap dari setting terbaik ke paling rendah.\n" +
        "Ikuti tangga ini sampai menemukan setting yang mencapai target playable (≥45 FPS sustained):\n\n" +
        "  TANGGA SETTING (urutkan dari atas ke bawah):\n" +
        "  1. High / Ultra  — 1920x1080 (1080p)\n" +
        "  2. Medium        — 1920x1080 (1080p)\n" +
        "  3. Low           — 1920x1080 (1080p)\n" +
        "  4. Low           — 1600x900  (900p)\n" +
        "  5. Low           — 1280x720  (720p)\n" +
        "  6. Low           — 1024x768  (768p 4:3)\n" +
        "  7. Low           — 800x600   (600p 4:3)\n" +
        "  8. Low           — 640x480   (480p 4:3) — ini resolusi terkecil umum\n\n" +
        "  ATURAN TANGGA:\n" +
        "  • Mulai dari tangga 1. Jika estimasi ≥45 FPS → STOP, ini setting rekomendasinya.\n" +
        "  • Jika < 45 FPS → turun ke tangga berikutnya, ulangi evaluasi.\n" +
        "  • Jika bahkan tangga 8 tidak mencapai 30 FPS → TIDAK PLAYABLE.\n" +
        "  • Wajib tulis estimasi FPS di SETIAP tangga yang dievaluasi.\n\n" +
        "=== REKOMENDASI DISPLAY MODE ===\n" +
        "Setelah menentukan setting optimal, WAJIB rekomendasikan salah satu:\n\n" +
        "  FULLSCREEN EKSKLUSIF\n" +
        "  + Performa terbaik: GPU akses langsung ke display, tanpa overhead compositor\n" +
        "  + FPS bisa 5-15% lebih tinggi vs Windowed\n" +
        "  + Input latency paling rendah\n" +
        "  - Alt+Tab lambat, tidak bisa multi-monitor mudah\n" +
        "  Rekomendasikan jika: hardware pas-pasan, game CPU/GPU-bound, atau butuh setiap FPS\n\n" +
        "  BORDERLESS WINDOW\n" +
        "  + Fleksibel: Alt+Tab instan, multi-monitor, overlay (Discord, Steam)\n" +
        "  + Performa hampir setara Fullscreen di Windows 10/11 modern (DWM optimized)\n" +
        "  - Di Windows versi lama atau game lama: bisa 5-10% lebih lambat dari Fullscreen\n" +
        "  Rekomendasikan jika: hardware mid-range ke atas, streaming/overlay aktif\n\n" +
        "  WINDOWED\n" +
        "  - Performa paling buruk: compositor selalu aktif, tearing possible\n" +
        "  - Tidak pernah direkomendasikan kecuali game tidak support mode lain\n" +
        "  Rekomendasikan HANYA jika: game tidak punya opsi lain\n\n" +
        "======================================\n\n";

    // =========================================================================
    //  ANCHOR DATA AVAILABILITY CHECK
    //  Dipakai ViewModel untuk tahu apakah hasil analisis ditopang data
    //  benchmark komunitas nyata, atau murni estimasi pengetahuan AI.
    // =========================================================================

    public static boolean hasAnchorData(String gpuName, String gameName) {
        return !BenchmarkHintBuilder.getBenchmarkHint(gpuName, gameName).isEmpty();
    }

    public static boolean hasAnyAnchorData(String gpuName, String gameName1, String gameName2) {
        return !BenchmarkHintBuilder.getBenchmarkHints(gpuName, gameName1, gameName2).isEmpty();
    }

    // =========================================================================
    //  SYSTEM PROMPTS
    // =========================================================================

    public static String getSystemPrompt(boolean isEnglish) {
        return
            (isEnglish ?
            "You are an ACCURATE, REALISTIC, and TECHNICAL PC/laptop game performance analyst. " +
            "15 years of testing hundreds of games across various hardware. " +
            "You know benchmarks from Digital Foundry, TechPowerUp, GamersNexus, and Reddit/Steam communities.\n\n"
            :
            "Kamu adalah analis performa game PC/laptop yang AKURAT, REALISTIS, dan TEKNIS. " +
            "Sudah 15 tahun menguji ratusan game di berbagai hardware. " +
            "Kamu hafal benchmark dari Digital Foundry, TechPowerUp, GamersNexus, dan komunitas Reddit/Steam.\n\n") +

            "=== ATURAN AKURASI ESTIMASI FPS ===\n" +
            "PRIORITAS UTAMA: Estimasi FPS AKURAT berdasarkan realita, bukan aman-amanan.\n\n" +

            "HIERARKI REFERENSI — ikuti urutan ini:\n" +
            "  1. DATA BENCHMARK KOMUNITAS (jika tersedia di atas) → gunakan sebagai anchor WAJIB\n" +
            "     Jangan abaikan data ini. Jika data komunitas bilang 55 FPS, jangan tulis 35 FPS.\n" +
            "  2. Benchmark GPU tier terdekat yang kamu ketahui → interpolasi proporsional\n" +
            "  3. Karakteristik engine + genre → hanya jika tidak ada referensi sama sekali\n\n" +

            "CARA BENAR JIKA TIDAK ADA DATA PERSIS:\n" +
            "  Jangan tulis angka pasti yang menyesatkan. Tulis range yang mencerminkan ketidakpastian.\n" +
            "  Contoh benar: '48-62 FPS (interpolasi dari RTX 3060, data terbatas untuk GPU ini)'\n" +
            "  Contoh salah: '38-46 FPS' tanpa dasar — deflasi karena asumsi 'entry-level'\n\n" +

            "URUTAN ANALISIS WAJIB:\n" +
            "  (a) Identifikasi hardware secara konkret:\n" +
            "      - GPU: seri, generasi, TDP tipikal di laptop ini, VRAM, setara desktop apa\n" +
            "      - CPU: arsitektur, core/thread, performa gaming nyata\n\n" +
            "  (b) Analisis engine + optimasi game:\n" +
            "      - Engine apa? Seberapa dioptimasi untuk hardware ini?\n" +
            "      - CPU-bound atau GPU-bound? Di area mana drop paling parah?\n" +
            "      - FSR/DLSS/XeSS tersedia? Estimasi gain FPS?\n\n" +
            "  (c) Faktor negatif yang BENAR-BENAR relevan (jangan tambah jika tidak ada):\n" +
            "      - Thermal throttle: HANYA jika game ini dikenal bikin CPU/GPU panas sustained\n" +
            "      - VRAM: HANYA warning jika mepet di setting yang direkomendasikan\n" +
            "      - CPU bottleneck: HANYA jika game CPU-bound dan CPU user memang lemah\n\n" +
            "  (d) Estimasi FPS: gunakan hierarki referensi di atas, tulis range nyata\n\n" +

            "FAKTOR THROTTLE LAPTOP:\n" +
            "  Laptop gaming modern (2021+) dengan cooling decent: throttle 5-15% sustained\n" +
            "  Laptop tipis/ultrabook atau game sangat berat: throttle 20-35%\n" +
            "  Jangan asumsi throttle parah tanpa indikasi spesifik.\n\n" +

            RESOLUTION_LADDER +

            "PRINSIP OUTPUT:\n" +
            "1. Akurat berbasis data. Jika ada data benchmark, gunakan. Jika tidak, interpolasi transparan.\n" +
            "2. Jika hardware JELAS tidak mampu (iGPU + game AAA berat), bilang TIDAK PLAYABLE.\n" +
            "3. Hitung real-world RAM: Windows ~2-3GB, iGPU shared 1-2GB, background ~0.5GB.\n" +
            "4. Engine matters: RE Engine efisien, UE5 Lumen berat, Java engine GC-prone.\n" +
            "5. Throttle laptop: hanya apply penalti jika game ini memang panas sustained.\n" +
            "6. VRAM warning: hanya jika benar-benar mepet di setting yang dianalisis.\n" +
            "7. Sebutkan area/boss SPESIFIK dari game — bukan generik.\n" +
            "8. Format: TANPA Markdown tabel/bold/##header. HURUF KAPITAL untuk header, bullet •.\n" +
            "9. Range FPS MAKSIMAL 10 angka. WAJIB ada 1% Low FPS.\n" +
            "10. Bahasa Indonesia santai tapi teknis.\n\n" +

            "SKALA FPS:\n" +
            "• Tidak playable: < 20 FPS\n• Barely playable: 20-29 FPS\n" +
            "• Minimum: 30-44 FPS\n• Playable: 45-59 FPS\n" +
            "• Smooth: 60-74 FPS\n• Very smooth: 75-99 FPS\n• High refresh: 100+ FPS";
    }

    public static String getCompareSystemPrompt(boolean isEnglish) {
        return
            (isEnglish ?
            "You are an ACCURATE and REALISTIC PC/laptop game performance analyst. " +
            "Comparing two games based on user hardware. " +
            "You know benchmarks from Digital Foundry, TechPowerUp, GamersNexus, Reddit, and Steam communities.\n\n"
            :
            "Kamu adalah analis performa game PC/laptop yang AKURAT dan REALISTIS. " +
            "Membandingkan dua game berdasarkan hardware user. " +
            "Kamu hafal benchmark dari Digital Foundry, TechPowerUp, GamersNexus, Reddit, dan komunitas Steam.\n\n") +

            "=== ATURAN AKURASI ESTIMASI FPS ===\n" +
            "HIERARKI REFERENSI — ikuti urutan ini:\n" +
            "  1. DATA BENCHMARK KOMUNITAS (jika tersedia di atas) → anchor WAJIB, jangan abaikan\n" +
            "  2. Benchmark GPU tier terdekat → interpolasi proporsional, sebutkan dasar interpolasi\n" +
            "  3. Karakteristik engine + genre → fallback terakhir\n\n" +

            "CARA BENAR JIKA TIDAK ADA DATA PERSIS:\n" +
            "  Tulis range yang mencerminkan ketidakpastian, bukan deflasi aman-amanan.\n" +
            "  Contoh: '50-65 FPS (interpolasi dari RTX 3060Ti, tidak ada data persis GPU ini)'\n\n" +

            "URUTAN ANALISIS per game:\n" +
            "  (a) Identifikasi hardware: seri, generasi, TDP, VRAM, setara desktop apa\n" +
            "  (b) Engine masing-masing game + referensi benchmark jika ada\n" +
            "  (c) Faktor negatif HANYA jika relevan (throttle, VRAM, CPU bottleneck)\n" +
            "  (d) Estimasi FPS: gunakan hierarki di atas, range max 10 angka, wajib 1% Low\n\n" +

            RESOLUTION_LADDER +

            "PRINSIP UTAMA:\n" +
            "1. Akurat berbasis data. Gunakan benchmark anchor jika tersedia.\n" +
            "2. Jika salah satu game tidak playable, bilang TIDAK PLAYABLE tegas.\n" +
            "3. Hitung real-world RAM: Windows ~2-3GB, iGPU shared 1-2GB.\n" +
            "4. Engine berbeda = FPS bisa sangat berbeda walau hardware sama.\n" +
            "5. Throttle laptop: hanya apply jika game ini memang panas sustained.\n" +
            "6. VRAM warning: hanya jika benar-benar mepet di setting dianalisis.\n" +
            "7. Area/boss SPESIFIK per game — bukan generik.\n" +
            "8. Format: TANPA Markdown tabel/bold. HURUF KAPITAL header, bullet •.\n" +
            "9. Range FPS MAKSIMAL 10 angka. WAJIB 1% Low.\n" +
            "10. Bahasa Indonesia santai tapi teknis.\n" +
            "11. Rekomendasi akhir tegas: game mana lebih layak di spek ini.";
    }

    // =========================================================================
    //  ENGINE DETECTION
    // =========================================================================

    public static String detectEngine(String gameName, String developers, String released) {
        if (gameName == null) gameName = "";
        if (developers == null) developers = "";
        String n = gameName.toLowerCase();

        if (n.contains("project zomboid") || n.contains("minecraft")
         || n.contains("runescape") || n.contains("terraria")
         || n.contains("starbound") || n.contains("stardew"))
            return "JAVA_ENGINE";

        if (n.contains("resident evil") || n.contains("devil may cry 5")
         || n.contains("monster hunter") || n.contains("devil may cry")
         || n.contains("pragmata"))
            return "RE_ENGINE";

        if (n.contains("black myth") || n.contains("wukong")
         || n.contains("senua's saga") || n.contains("hellblade ii")
         || n.contains("indiana jones") || n.contains("remnant ii")
         || n.contains("alan wake 2") || n.contains("lords of the fallen")
         || n.contains("avowed") || n.contains("kingdom come: deliverance ii"))
            return "UE5";

        if (n.contains("counter-strike") || n.contains("cs2") || n.contains("cs:go")
         || n.contains("dota 2") || n.contains("team fortress")
         || n.contains("left 4 dead") || n.contains("half-life"))
            return "SOURCE_ENGINE";

        if (n.contains("starfield") || n.contains("skyrim") || n.contains("fallout 4")
         || n.contains("fallout 76") || n.contains("oblivion"))
            return "CREATION_ENGINE";

        if (n.contains("gta") || n.contains("grand theft auto")
         || n.contains("red dead redemption"))
            return "RAGE_ENGINE";

        if (n.contains("death stranding") || n.contains("horizon"))
            return "DECIMA_ENGINE";

        if (n.contains("elden ring") || n.contains("sekiro")
         || n.contains("dark souls") || n.contains("armored core vi")
         || n.contains("nightreign"))
            return "FROMSOFTWARE_ENGINE";

        return "GENERIC";
    }

    public static String getEngineNote(String engine, String gameName) {
        switch (engine) {
            case "JAVA_ENGINE":
                return "ENGINE: Java/JVM-based. " +
                    "PENTING: Java engine butuh JVM heap tambahan — alokasikan minimal 2GB RAM hanya untuk game. " +
                    "Di sistem RAM 4GB dengan Windows, praktis tidak ada sisa RAM. " +
                    "Stuttering karena Garbage Collection (GC pause) muncul tiap 10-30 detik bahkan di hardware bagus. " +
                    "Rekomendasi minimum REAL WORLD: 8GB RAM untuk pengalaman yang layak.";
            case "RE_ENGINE":
                return "ENGINE: RE Engine (Capcom). " +
                    "Salah satu engine paling efisien di industri. FPS 20-30% lebih tinggi dibanding engine AAA lain. " +
                    "DLSS/FSR support baik. GPU-bound di setting tinggi, tapi tidak seberat UE5.";
            case "UE5":
                return "ENGINE: Unreal Engine 5 dengan Nanite + Lumen. " +
                    "BERAT SEKALI di hardware mid-range ke bawah. Lumen (RT software) makan GPU 20-40% lebih. " +
                    "GPU score < 55/100 akan sangat tersiksa di High+. Shader compilation stutter di awal sesi.";
            case "SOURCE_ENGINE":
                return "ENGINE: Source/Source 2 (Valve). " +
                    "CPU-bound engine. GPU kuat tidak banyak membantu — yang penting CPU IPC tinggi. " +
                    "Biasanya FPS sangat tinggi di hardware mid-range karena engine tua.";
            case "CREATION_ENGINE":
                return "ENGINE: Creation Engine (Bethesda). " +
                    "Notorious CPU-bound, tidak ter-multithread dengan baik. " +
                    "Stuttering dari papyrus scripting adalah bug engine. VRAM overflow di Starfield bisa crash.";
            case "RAGE_ENGINE":
                return "ENGINE: RAGE Engine (Rockstar). " +
                    "CPU-heavy di kota padat dengan banyak NPC. " +
                    "RDR2 salah satu port PC terbagus — sangat scalable tapi butuh CPU+GPU seimbang.";
            case "FROMSOFTWARE_ENGINE":
                return "ENGINE: FromSoftware in-house engine. " +
                    "Port PC sering bermasalah — Elden Ring notorious frame pacing issues. " +
                    "CPU-bound di area terbuka. Boss arena biasanya GPU-bound.";
            default:
                return "";
        }
    }

    // =========================================================================
    //  SPIN-OFF / VERSION GUARD
    // =========================================================================

    public static String getSpinoffGuard(String gameName) {
        if (gameName == null) return "";
        String n = gameName.toLowerCase();

        if (n.contains("nightreign"))
            return "PENTING — SPIN-OFF GUARD: " + gameName + " adalah STANDALONE SPIN-OFF, " +
                "BUKAN Elden Ring original. " +
                "Game ini roguelite co-op 3 pemain dengan night cycle dan Nightlord bosses. " +
                "JANGAN sebut Malenia, Radahn, Margit, area Limgrave/Liurnia/Farum Azula — itu Elden Ring original. " +
                "Sebutkan: Nightlord bosses, eksplorasi map bersama, node battle, respawn system dalam run.";

        if (n.contains("resident evil 4 remake") || (n.contains("resident evil 4") && n.contains("2023")))
            return "PENTING: Resident Evil 4 REMAKE (2023). Area: Desa Eropa, Kastil Salazar, Pulau militer. " +
                "Boss: El Gigante (desa), Garrador (kastil), Krauser (pulau).";

        if (n.contains("dark souls") && !n.contains("elden ring"))
            return "PENTING: Ini Dark Souls, bukan Elden Ring. Area spesifik Dark Souls: " +
                "Undead Burg, Anor Londo, Blighttown, Quelaag, Ornstein & Smough.";

        if (n.contains("ragnarok") && n.contains("god of war"))
            return "PENTING: God of War Ragnarok. Area: Nine Realms termasuk Vanaheim, Svartalfheim, Asgard. " +
                "Bukan area GoW 2018 (Midgard saja).";

        return "";
    }

    // =========================================================================
    //  REAL-WORLD HARDWARE ANALYSIS
    // =========================================================================

    public static String buildRealWorldHardwareAnalysis(SpekUser user) {
        boolean isIGpu   = isIntegratedGpu(user.getGpuName());
        boolean isLaptop = isLaptopCpu(user.getCpuName()) || isLaptopGpu(user.getGpuName());
        int vram  = user.getVramGb();
        int ram   = user.getRamGb();
        int cs    = user.getCpuScore();
        int gs    = user.getGpuScore();

        StringBuilder sb = new StringBuilder();
        sb.append("=== ANALISIS HARDWARE REAL-WORLD ===\n");
        sb.append("Platform : ").append(isLaptop ? "LAPTOP" : "PC DESKTOP").append("\n");
        sb.append("CPU      : ").append(user.getCpuName()).append(" (skor ").append(cs).append("/100)\n");
        sb.append("GPU      : ").append(user.getGpuName())
          .append(" (skor ").append(gs).append("/100, ")
          .append(isIGpu ? "iGPU — shared VRAM" : vram + "GB VRAM dedicated").append(")\n");
        sb.append("RAM total: ").append(ram).append("GB\n");

        sb.append("\nKALKULASI RAM REAL-WORLD:\n");
        int windowsOverhead = 3;
        int iGpuShared = 0;
        if (isIGpu) {
            iGpuShared = ram <= 8 ? 1 : 2;
            sb.append("• iGPU shared memory : -").append(iGpuShared).append("GB\n");
        }
        sb.append("• Windows + services : -").append(windowsOverhead).append("GB\n");
        int availableForGame = ram - windowsOverhead - iGpuShared;
        sb.append("• Sisa untuk game    : ~").append(Math.max(0, availableForGame)).append("GB\n");

        if (availableForGame <= 0) {
            sb.append("KRITIS: RAM tidak cukup bahkan untuk booting game! TIDAK PLAYABLE.\n");
        } else if (availableForGame <= 1) {
            sb.append("KRITIS: Hanya ~").append(availableForGame).append("GB untuk game. Swap parah, stutter konstan.\n");
        } else if (availableForGame <= 3) {
            sb.append("PERINGATAN: RAM terbatas (~").append(availableForGame).append("GB). Stutter saat loading area baru.\n");
        } else {
            sb.append("RAM aman: ~").append(availableForGame).append("GB tersedia.\n");
        }

        if (!isIGpu) {
            sb.append("\nANALISIS VRAM:\n");
            if (vram <= 2) {
                sb.append("• ").append(vram).append("GB VRAM — SANGAT TERBATAS. Texture harus Low/Medium. Risiko crash post-2018.\n");
            } else if (vram <= 4) {
                sb.append("• ").append(vram).append("GB VRAM — Terbatas. Aman Low/Medium, High texture bisa overflow.\n");
            } else if (vram <= 6) {
                sb.append("• ").append(vram).append("GB VRAM — Cukup untuk Medium/High, Ultra texture 2022+ bisa overflow.\n");
            } else if (vram <= 8) {
                sb.append("• ").append(vram).append("GB VRAM — Aman untuk High/Ultra sebagian besar game.\n");
            } else {
                sb.append("• ").append(vram).append("GB VRAM — Excellent, cukup untuk Ultra + RT.\n");
            }
        }

        if (isLaptop) {
            sb.append("\nLAPTOP THERMAL REALITY:\n");
            sb.append("• Menit 0-5: ~100% TDP, FPS tertinggi\n");
            sb.append("• Setelah 10-15 menit: throttle dimulai\n");
            sb.append("• Sustained 30+ menit: stabil di 60-80% peak\n");
            sb.append("• FPS di bawah adalah estimasi SUSTAINED\n");
        }

        sb.append("\nKONTEKS GPU:\n");
        if (isIGpu) {
            sb.append("• iGPU: berbagi bandwidth dengan CPU, tidak ada VRAM khusus\n");
            sb.append("• Hanya layak untuk game indie/2D/kasual atau game pre-2015 setting Low\n");
            sb.append("• Game AAA modern (post-2019) hampir pasti TIDAK PLAYABLE\n");
        } else if (gs < 35) {
            sb.append("• Entry-level: hanya game ringan/indie atau AAA lama setting Very Low\n");
        } else if (gs < 50) {
            sb.append("• Budget: game 2020-era di Low/Medium, game terbaru sangat berat\n");
        } else if (gs < 65) {
            sb.append("• Lower mid-range: Medium kebanyakan game, High di game dioptimasi\n");
        } else if (gs < 80) {
            sb.append("• Mid-range: High sebagian besar game, Ultra di game dioptimasi\n");
        } else {
            sb.append("• High-end: Ultra sebagian besar game termasuk yang demanding\n");
        }

        sb.append("=====================================\n\n");
        return sb.toString();
    }

    // =========================================================================
    //  PRESET DETECTION
    // =========================================================================

    public static boolean hasGraphicsPreset(String gameName, String genres, String released) {
        if (gameName == null) gameName = "";
        if (genres == null)   genres = "";
        String n = gameName.toLowerCase();
        String g = genres.toLowerCase();

        if (n.contains("counter-strike") || n.contains("cs2") || n.contains("valorant")
         || n.contains("dota 2") || n.contains("minecraft") || n.contains("terraria")
         || n.contains("stardew") || n.contains("among us") || n.contains("project zomboid")
         || n.contains("roblox") || n.contains("rocket league"))
            return false;

        if (n.contains("cyberpunk") || n.contains("witcher") || n.contains("red dead")
         || n.contains("gta v") || n.contains("elden ring") || n.contains("nightreign")
         || n.contains("battlefield") || n.contains("call of duty") || n.contains("far cry")
         || n.contains("horizon") || n.contains("resident evil") || n.contains("spider-man")
         || n.contains("god of war") || n.contains("forza") || n.contains("the last of us")
         || n.contains("ghost of tsushima") || n.contains("starfield")
         || n.contains("hogwarts legacy") || n.contains("alan wake") || n.contains("black myth")
         || n.contains("indiana jones") || n.contains("fortnite") || n.contains("apex legend")
         || n.contains("doom") || n.contains("death stranding") || n.contains("baldur's gate")
         || n.contains("armored core") || n.contains("sekiro") || n.contains("dark souls"))
            return true;

        boolean isAAA = g.contains("action") && (g.contains("adventure") || g.contains("shooter"))
                      && !g.contains("indie") && !g.contains("casual");
        boolean isModern = released != null && released.length() >= 4
                      && Integer.parseInt(released.substring(0, 4)) >= 2016;
        return isAAA && isModern;
    }

    // =========================================================================
    //  SCENARIO INSTRUCTIONS
    // =========================================================================

    private static String getScenarioInstruction(String genres, String gameName) {
        if (genres == null)   genres = "";
        if (gameName == null) gameName = "";
        String g = genres.toLowerCase();
        String n = gameName.toLowerCase();

        if (n.contains("elden ring") && n.contains("nightreign"))
            return "AREA SPESIFIK Elden Ring Nightreign:\n" +
                "• Awal eksplorasi map (node pertama, sepi)  = FPS tertinggi\n" +
                "• Mid-run: banyak efek sihir 3 pemain       = FPS sedang\n" +
                "• Night shrinks (night cycle boss)          = FPS turun\n" +
                "• Nightlord final boss fight                = FPS terendah";

        if (n.contains("elden ring") && !n.contains("nightreign"))
            return "AREA SPESIFIK Elden Ring:\n" +
                "• Limgrave (area awal, terbuka)              = FPS tertinggi\n" +
                "• Liurnia (luas + hujan + efek magic)        = FPS agak turun\n" +
                "• Leyndell Royal Capital (kota padat)        = FPS sedang\n" +
                "• Malenia boss fight (Scarlet Rot particles) = FPS terendah\n" +
                "• Farum Azula (debris + efek udara)          = FPS sering drop";

        if (n.contains("resident evil 4"))
            return "AREA SPESIFIK RE4 Remake:\n" +
                "• Desa (outdoor, lighting dinamis)          = FPS paling berat\n" +
                "• Kastil indoor (lorong, lighting stabil)   = FPS tertinggi\n" +
                "• Laboratorium/Pulau (linear)               = FPS sedang-tinggi\n" +
                "• El Gigante / Las Plagas explosion         = FPS drop sementara";

        if (n.contains("cyberpunk"))
            return "AREA SPESIFIK Cyberpunk 2077:\n" +
                "• Badlands / wasteland (sepi, terbuka)         = FPS tertinggi\n" +
                "• City Center (gedung tinggi + refleksi)       = FPS sedang\n" +
                "• Dogtown / Pacifica (NPC padat)               = FPS terendah\n" +
                "• Night City hujan + ray tracing aktif         = FPS sangat turun";

        if (n.contains("god of war") && n.contains("ragnarok"))
            return "AREA SPESIFIK GoW Ragnarok:\n" +
                "• Midgard / eksplorasi sepi             = FPS tertinggi\n" +
                "• Vanaheim (hutan lebat + partikel alam)= FPS sedang\n" +
                "• Asgard (banyak efek dewa + debris)    = FPS terendah\n" +
                "• God fight partikel heavy              = FPS drop signifikan";

        if (n.contains("baldur's gate"))
            return "AREA SPESIFIK BG3:\n" +
                "• Act 1 Wilderness (area awal, terbuka)       = FPS tertinggi\n" +
                "• Act 2 Shadow-Cursed Lands (darkness shader) = FPS sedang\n" +
                "• Act 3 Lower City (padat + crowded)          = FPS terendah\n" +
                "• Spell visual ramai + cutscene               = FPS drop";

        if (n.contains("black myth") || n.contains("wukong"))
            return "AREA SPESIFIK Black Myth: Wukong:\n" +
                "• Bamboo Grove / area terbuka awal             = FPS tertinggi\n" +
                "• Forest/cave dengan efek foliage mid-game     = FPS sedang\n" +
                "• Boss Guangmou / Erlang Shen (heavy particles)= FPS terendah\n" +
                "• Area dengan Lumen GI aktif                   = GPU-intensive";

        if (n.contains("project zomboid"))
            return "SKENARIO Project Zomboid:\n" +
                "• Area rural, sedikit zombie                     = FPS tertinggi\n" +
                "• Kota kecil beberapa ratus zombie               = FPS sedang\n" +
                "• Louisville (ribuan zombie)                     = FPS terendah, GC parah\n" +
                "• GC pause (tiap 10-30 detik)                   = freeze 0.5-2 detik";

        if (n.contains("starfield"))
            return "AREA SPESIFIK Starfield:\n" +
                "• Planet kosong / space                         = FPS tertinggi\n" +
                "• New Atlantis (kota utama, padat NPC)          = FPS terendah (CPU-bound)\n" +
                "• Combat banyak enemy + efek                    = FPS sedang\n" +
                "• Interior ship / dungeon                       = FPS tinggi";

        if (n.contains("red dead redemption") || n.contains("rdr2"))
            return "AREA SPESIFIK RDR2:\n" +
                "• Heartlands / Roanoke Ridge (open plains)      = FPS tertinggi\n" +
                "• Saint Denis (kota padat + refleksi)           = FPS terendah\n" +
                "• Ambarino snowstorm                            = FPS drop signifikan\n" +
                "• Dense forest + God rays                       = FPS sedang";

        if (n.contains("witcher 3"))
            return "AREA SPESIFIK The Witcher 3:\n" +
                "• Velen / White Orchard (countryside)           = FPS tertinggi\n" +
                "• Novigrad (kota padat, banyak NPC)             = FPS terendah\n" +
                "• Skellige (laut + cuaca dinamis)               = FPS sedang\n" +
                "• Boss fight sign magic                         = FPS drop ringan";

        if (g.contains("shooter") || n.contains("battlefield") || n.contains("call of duty"))
            return "SKENARIO Shooter:\n" +
                "• Lobby / indoor sepi        = FPS tertinggi\n" +
                "• Outdoor sedikit musuh      = FPS tinggi\n" +
                "• Combat banyak efek senjata = FPS sedang\n" +
                "• Full squad + smoke + explosion = FPS terendah";

        if (g.contains("racing") || n.contains("forza"))
            return "SKENARIO Racing:\n" +
                "• Time trial track kosong    = FPS tertinggi\n" +
                "• Race grid banyak mobil     = FPS sedang\n" +
                "• Hujan + malam + refleksi   = FPS terendah";

        if (g.contains("strategy") || n.contains("dota") || n.contains("total war"))
            return "SKENARIO Strategy/MOBA:\n" +
                "• Early game, sedikit unit        = FPS tertinggi\n" +
                "• Mid-game, spell + units         = FPS sedang\n" +
                "• Late game / teamfight ribuan unit= FPS terendah (CPU-bound)";

        return "SKENARIO Area:\n" +
            "• Area sepi / eksplorasi ringan     = FPS tertinggi\n" +
            "• Combat beberapa musuh             = FPS medium\n" +
            "• Area ramai / boss / efek berat    = FPS terendah";
    }

    // =========================================================================
    //  1. SINGLE GAME PROMPT
    // =========================================================================

    public static String buildSinglePrompt(
            SpekUser user,
            GameDetail game,
            SteamRequirement.RequirementSpec minReq,
            SteamRequirement.RequirementSpec recReq,
            boolean isEnglish) {

        boolean hasPreset  = hasGraphicsPreset(game.getName(), game.getGenreNames(), game.getReleased());
        String engine      = detectEngine(game.getName(), game.getDeveloperNames(), game.getReleased());
        String engineNote  = getEngineNote(engine, game.getName());
        String spinoffGuard = getSpinoffGuard(game.getName());
        boolean isIGpu     = isIntegratedGpu(user.getGpuName());
        boolean isLaptop   = isLaptopCpu(user.getCpuName()) || isLaptopGpu(user.getGpuName());
        int vram = user.getVramGb();
        int iGpuShared  = isIGpu ? (user.getRamGb() <= 8 ? 1 : 2) : 0;
        int availableRam = user.getRamGb() - 3 - iGpuShared;
        boolean likelyCantRun = (isIGpu && game.getMetacritic() != null && game.getMetacritic() > 70
                                 && game.getReleased() != null && game.getReleased().compareTo("2018") > 0)
                              || availableRam < 1;

        StringBuilder sb = new StringBuilder();

        // ── INJECT BENCHMARK DATA ─────────────────────────────────────────────
        String benchmarkHint = BenchmarkHintBuilder.getBenchmarkHint(user.getGpuName(), game.getName());
        if (!benchmarkHint.isEmpty()) {
            sb.append("DATA BENCHMARK KOMUNITAS (ANCHOR WAJIB — GUNAKAN SEBAGAI DASAR ESTIMASI FPS):\n");
            sb.append(benchmarkHint).append("\n");
            sb.append("INSTRUKSI ANCHOR: Estimasi FPS kamu HARUS konsisten dengan data di atas. " +
                      "Jika GPU user beda tier, interpolasi secara proporsional dan sebutkan dasar interpolasinya.\n\n");
        }

        sb.append(buildRealWorldHardwareAnalysis(user));
        if (!engineNote.isEmpty())   sb.append(engineNote).append("\n\n");
        if (!spinoffGuard.isEmpty()) sb.append(spinoffGuard).append("\n\n");

        // Instruksi bahasa output
        sb.append("OUTPUT LANGUAGE INSTRUCTION: Respond entirely in ")
          .append(isEnglish ? "English." : "Bahasa Indonesia (Indonesian).")
          .append(" All analysis, FPS estimates, area names (except proper game names/locations),"
               + " and recommendations must use this language.\n\n");

        sb.append("DATA GAME:\n");
        sb.append("Nama       : ").append(game.getName()).append("\n");
        sb.append("Genre      : ").append(game.getGenreNames()).append("\n");
        sb.append("Developer  : ").append(game.getDeveloperNames()).append("\n");
        sb.append("Metacritic : ").append(game.getMetacritic() != null ? game.getMetacritic() : "N/A").append("\n");
        sb.append("Rilis      : ").append(game.getReleased()).append("\n");
        appendReq(sb, "Minimum", minReq);
        appendReq(sb, "Recommended", recReq);

        // ── FORMAT OUTPUT — TERIKAT BAHASA ──────────────────────────────────────
        String recSetting = "optimal";

        if (isEnglish) {
            sb.append("=========================================\n");
            sb.append("OUTPUT FORMAT INSTRUCTION — FOLLOW EXACTLY:\n");
            sb.append("=========================================\n\n");
            sb.append("NO Markdown tables (|col|), NO **bold**, NO ##headers.\n");
            sb.append("Use UPPERCASE for section headers, bullet • for lists.\n");
            sb.append("EVERY header below MUST be written in English exactly as shown.\n\n");

            sb.append("FPS PREDICTION — ").append(game.getName().toUpperCase()).append("\n");
            sb.append("Specs: [CPU] + [GPU] [VRAM]GB + RAM [X]GB\n\n");

            if (likelyCantRun || isIGpu) {
                sb.append("VERDICT: [NOT PLAYABLE / Not Recommended / etc] — [brief real-world reason]\n\n");
            }

            sb.append("RESOLUTION LADDER EVALUATION\n");
            sb.append("Reason from top to bottom. STOP at the first tier that reaches ≥45 FPS sustained.\n");
            if (hasPreset) {
                sb.append("• High/Ultra 1080p : [X-Y FPS] — [playable/not? continue below if not]\n");
                sb.append("• Medium     1080p : [X-Y FPS] — [playable/not?]\n");
                sb.append("• Low        1080p : [X-Y FPS] — [playable/not?]\n");
                sb.append("• Low        900p  : [X-Y FPS] — [playable/not?]\n");
                sb.append("• Low        720p  : [X-Y FPS] — [playable/not?]\n");
                sb.append("• Low        480p (4:3) : [write if still not playable above]\n");
                sb.append("[STOP the ladder at the first tier that is ≥45 FPS. " +
                          "If even 480p < 30 FPS → NOT PLAYABLE]\n\n");
            } else {
                sb.append("• Full setting   1080p : [X-Y FPS] — [playable/not?]\n");
                sb.append("• Light setting  1080p : [X-Y FPS] — [playable/not?]\n");
                sb.append("• Light setting  720p  : [X-Y FPS] — [playable/not?]\n");
                sb.append("• Light setting  480p (4:3) : [if still needed]\n\n");
            }
            sb.append("RANGE RULE: MAXIMUM 10 FPS gap. 1% Low FPS REQUIRED.\n\n");

            sb.append("OPTIMAL SETTING FROM LADDER\n");
            sb.append("• Best resolution + preset reaching ≥45 FPS : [answer from ladder]\n");
            sb.append("• Estimated FPS at this setting              : [X-Y FPS] | 1% Low ~[Z FPS]\n\n");

            sb.append("PERFORMANCE PER AREA/SCENARIO (setting ").append(recSetting).append(")\n");
            sb.append("SPECIFIC area/boss names from this game:\n");
            sb.append("• [Specific area/scenario name] : [X-Y FPS] — [note]\n");
            sb.append("• [Specific area/scenario name] : [X-Y FPS] — [note]\n");
            sb.append("• [Specific area/scenario name] : [X-Y FPS] — [note]\n");
            sb.append("• [Specific area/scenario name] : [X-Y FPS] — [note]\n\n");

            if (hasPreset) {
                sb.append("OPTIMAL SETTING DETAILS\n");
                sb.append("• Base preset   : [Low/Medium/High/Ultra]\n");
                sb.append("• Resolution    : [ladder result] — [reason]\n");
                sb.append("• Texture       : [value] — [mention VRAM impact]\n");
                sb.append("• Shadow        : [value]\n");
                sb.append("• Ray Tracing   : [On/Off] — [firm reason]\n");
                sb.append("• DLSS/FSR/XeSS : [mode] — [estimated FPS gain]\n");
                sb.append("• [Most impactful game-specific setting] : [value]\n\n");
            }

            sb.append("DISPLAY MODE RECOMMENDATION\n");
            sb.append("Pick ONE that fits best and explain the performance impact:\n");
            sb.append("• Exclusive Fullscreen : [recommend if hardware is tight]\n");
            sb.append("• Borderless Window    : [recommend if mid-range+ / streaming]\n");
            sb.append("• Windowed             : [only if no other option]\n");
            sb.append("State the estimated FPS DIFFERENCE between the chosen mode vs the others.\n\n");

            sb.append("CONCLUSION\n");
            sb.append("• Best playable setting : [resolution + preset] → [FPS estimate]\n");
            sb.append("• Display mode          : [recommendation + brief reason]\n");
            sb.append("• Maximum target FPS    : [setting] → [FPS estimate]\n\n");

            sb.append("TECHNICAL NOTES\n");
            sb.append("• Main limiting component for this game\n");
            sb.append("• VRAM overflow / RAM swap / throttle if relevant\n");
            sb.append("• Engine-specific bug/issue if any\n\n");

            sb.append("=========================================\n");
            sb.append("USER SPECS:\n");
            sb.append("GPU: ").append(user.getGpuName());
            if (!isIGpu) sb.append(", VRAM=").append(vram).append("GB");
            sb.append("\nCPU: ").append(user.getCpuName()).append("\n");
            sb.append("Available RAM ~").append(Math.max(0, availableRam)).append("GB\n");
            if (isLaptop) sb.append("PLATFORM: Laptop\n");
            if (isIGpu)   sb.append("GPU: iGPU — no dedicated VRAM\n");
            sb.append("\nANALYSIS INSTRUCTIONS:\n");
            sb.append("1. Identify GPU/CPU: series, generation, TDP, VRAM, desktop equivalent\n");
            if (!benchmarkHint.isEmpty()) {
                sb.append("2. USE the community benchmark data above as the primary FPS anchor\n");
                sb.append("3. If GPU tier differs, interpolate proportionally and state the basis\n");
            } else {
                sb.append("2. Find real benchmarks for this GPU in this game — use as anchor\n");
                sb.append("3. If no exact data, interpolate from the closest GPU and state the basis\n");
            }
            sb.append("4. Use the RESOLUTION LADDER — evaluate from High 1080p down until playable\n");
            sb.append("5. Recommend display mode (Fullscreen/Borderless/Windowed) + performance reason\n");
            sb.append("6. Negative factors ONLY if truly relevant (throttle/VRAM/CPU)\n");
            sb.append("7. FPS range MAXIMUM 10 numbers gap. 1% Low REQUIRED. All numbers realistic.\n");
            sb.append("8. Write the ENTIRE response in English — including every section header above.\n");

        } else {
            sb.append("=========================================\n");
            sb.append("INSTRUKSI FORMAT OUTPUT — IKUTI PERSIS:\n");
            sb.append("=========================================\n\n");
            sb.append("TANPA Markdown tabel (|col|), TANPA **bold**, TANPA ##header.\n");
            sb.append("Gunakan HURUF KAPITAL untuk header, bullet • untuk list.\n\n");

            sb.append("PREDIKSI FPS — ").append(game.getName().toUpperCase()).append("\n");
            sb.append("Spesifikasi: [CPU] + [GPU] [VRAM]GB + RAM [X]GB\n\n");

            if (likelyCantRun || isIGpu) {
                sb.append("VERDICT: [TIDAK PLAYABLE / Tidak Direkomendasikan / dll] — [alasan real-world singkat]\n\n");
            }

            sb.append("EVALUASI TANGGA RESOLUSI\n");
            sb.append("Nalar dari atas ke bawah. STOP di tangga pertama yang mencapai ≥45 FPS sustained.\n");
            if (hasPreset) {
                sb.append("• High/Ultra 1080p  : [X-Y FPS] — [playable/tidak? lanjut ke bawah jika tidak]\n");
                sb.append("• Medium    1080p  : [X-Y FPS] — [playable/tidak?]\n");
                sb.append("• Low       1080p  : [X-Y FPS] — [playable/tidak?]\n");
                sb.append("• Low       900p   : [X-Y FPS] — [playable/tidak?]\n");
                sb.append("• Low       720p   : [X-Y FPS] — [playable/tidak?]\n");
                sb.append("• Low       480p (4:3) : [tulis jika masih belum playable di atas]\n");
                sb.append("[HENTIKAN TANGGA di setting pertama yang ≥45 FPS. " +
                          "Jika 480p pun < 30 FPS → TIDAK PLAYABLE]\n\n");
            } else {
                sb.append("• Setting penuh  1080p : [X-Y FPS] — [playable/tidak?]\n");
                sb.append("• Setting ringan 1080p : [X-Y FPS] — [playable/tidak?]\n");
                sb.append("• Setting ringan 720p  : [X-Y FPS] — [playable/tidak?]\n");
                sb.append("• Setting ringan 480p (4:3) : [jika masih dibutuhkan]\n\n");
            }
            sb.append("ATURAN RANGE: MAKSIMAL 10 angka gap. WAJIB ada 1% Low FPS.\n\n");

            sb.append("SETTING OPTIMAL HASIL TANGGA\n");
            sb.append("• Resolusi + preset terbaik yang ≥45 FPS : [jawaban dari tangga]\n");
            sb.append("• FPS estimasi pada setting ini           : [X-Y FPS] | 1% Low ~[Z FPS]\n\n");

            sb.append("PERFORMA PER AREA/SKENARIO (setting ").append(recSetting).append(")\n");
            sb.append("Nama area/boss SPESIFIK dari game ini:\n");
            sb.append("• [Nama area/skenario spesifik] : [X-Y FPS] — [catatan]\n");
            sb.append("• [Nama area/skenario spesifik] : [X-Y FPS] — [catatan]\n");
            sb.append("• [Nama area/skenario spesifik] : [X-Y FPS] — [catatan]\n");
            sb.append("• [Nama area/skenario spesifik] : [X-Y FPS] — [catatan]\n\n");

            if (hasPreset) {
                sb.append("SETTING DETAIL OPTIMAL\n");
                sb.append("• Preset dasar  : [Low/Medium/High/Ultra]\n");
                sb.append("• Resolusi      : [hasil tangga] — [alasan]\n");
                sb.append("• Texture       : [nilai] — [sebut VRAM impact]\n");
                sb.append("• Shadow        : [nilai]\n");
                sb.append("• Ray Tracing   : [On/Off] — [alasan tegas]\n");
                sb.append("• DLSS/FSR/XeSS : [mode] — [estimasi FPS gain]\n");
                sb.append("• [Setting game spesifik paling berpengaruh] : [nilai]\n\n");
            }

            sb.append("REKOMENDASI DISPLAY MODE\n");
            sb.append("Pilih SATU yang paling sesuai dan jelaskan dampak performanya:\n");
            sb.append("• Fullscreen Eksklusif : [rekomendasikan jika hardware pas-pasan]\n");
            sb.append("• Borderless Window    : [rekomendasikan jika mid-range ke atas / streaming]\n");
            sb.append("• Windowed             : [hanya jika tidak ada opsi lain]\n");
            sb.append("Sebutkan estimasi PERBEDAAN FPS antara mode yang dipilih vs mode lainnya.\n\n");

            sb.append("KESIMPULAN\n");
            sb.append("• Setting terbaik playable : [resolusi + preset] → [FPS estimasi]\n");
            sb.append("• Display mode             : [rekomendasi + alasan singkat]\n");
            sb.append("• Target FPS maksimal      : [setting] → [FPS estimasi]\n\n");

            sb.append("CATATAN TEKNIS\n");
            sb.append("• Komponen pembatas utama untuk game ini\n");
            sb.append("• VRAM overflow / RAM swap / throttle jika relevan\n");
            sb.append("• Bug/issue engine spesifik jika ada\n\n");

            sb.append("=========================================\n");
            sb.append("SPESIFIKASI USER:\n");
            sb.append("GPU: ").append(user.getGpuName());
            if (!isIGpu) sb.append(", VRAM=").append(vram).append("GB");
            sb.append("\nCPU: ").append(user.getCpuName()).append("\n");
            sb.append("RAM tersedia ~").append(Math.max(0, availableRam)).append("GB\n");
            if (isLaptop) sb.append("PLATFORM: Laptop\n");
            if (isIGpu)   sb.append("GPU: iGPU — tidak ada VRAM dedicated\n");
            sb.append("\nINSTRUKSI ANALISIS:\n");
            sb.append("1. Identifikasi GPU/CPU: seri, generasi, TDP, VRAM, setara desktop apa\n");
            if (!benchmarkHint.isEmpty()) {
                sb.append("2. GUNAKAN data benchmark komunitas di atas sebagai anchor FPS utama\n");
                sb.append("3. Jika ada perbedaan GPU tier, interpolasi proporsional dan sebut dasarnya\n");
            } else {
                sb.append("2. Cari benchmark nyata GPU ini di game ini — gunakan sebagai anchor\n");
                sb.append("3. Jika tidak ada data persis, interpolasi dari GPU terdekat dan sebut dasarnya\n");
            }
            sb.append("4. Gunakan TANGGA RESOLUSI — evaluasi dari High 1080p turun sampai playable\n");
            sb.append("5. Rekomendasikan display mode (Fullscreen/Borderless/Windowed) + alasan performa\n");
            sb.append("6. Faktor negatif HANYA jika benar-benar relevan (throttle/VRAM/CPU)\n");
            sb.append("7. Range FPS MAKSIMAL 10 angka. WAJIB 1% Low. Semua angka nyata.\n");
            sb.append("8. Tulis SELURUH respons dalam Bahasa Indonesia — termasuk setiap header section di atas.\n");
        }

        return sb.toString();
    }

    // =========================================================================
    //  2. COMPARE TWO GAMES PROMPT
    // =========================================================================

    public static String buildComparePrompt(
            SpekUser user,
            GameDetail game1, SteamRequirement.RequirementSpec minReq1,
            GameDetail game2, SteamRequirement.RequirementSpec minReq2,
            boolean isEnglish) {

        boolean preset1 = hasGraphicsPreset(game1.getName(), game1.getGenreNames(), game1.getReleased());
        boolean preset2 = hasGraphicsPreset(game2.getName(), game2.getGenreNames(), game2.getReleased());
        String engine1  = detectEngine(game1.getName(), game1.getDeveloperNames(), game1.getReleased());
        String engine2  = detectEngine(game2.getName(), game2.getDeveloperNames(), game2.getReleased());
        String note1    = getEngineNote(engine1, game1.getName());
        String note2    = getEngineNote(engine2, game2.getName());
        String guard1   = getSpinoffGuard(game1.getName());
        String guard2   = getSpinoffGuard(game2.getName());
        boolean isIGpu  = isIntegratedGpu(user.getGpuName());
        boolean isLaptop= isLaptopCpu(user.getCpuName()) || isLaptopGpu(user.getGpuName());
        int iGpuShared  = isIGpu ? (user.getRamGb() <= 8 ? 1 : 2) : 0;
        int availableRam= user.getRamGb() - 3 - iGpuShared;

        String setting1 = "optimal";
        String setting2 = "optimal";

        StringBuilder sb = new StringBuilder();

        // ── INJECT BENCHMARK DATA ─────────────────────────────────────────────
        String benchmarkHints = BenchmarkHintBuilder.getBenchmarkHints(
                user.getGpuName(), game1.getName(), game2.getName());
        if (!benchmarkHints.isEmpty()) sb.append(benchmarkHints);

        sb.append(buildRealWorldHardwareAnalysis(user));

        // Instruksi bahasa output
        sb.append("OUTPUT LANGUAGE INSTRUCTION: Respond entirely in ")
          .append(isEnglish ? "English." : "Bahasa Indonesia (Indonesian).")
          .append(" All analysis, FPS estimates, and recommendations must use this language.\n\n");

        if (!note1.isEmpty())  sb.append("ENGINE GAME 1 (").append(game1.getName()).append("):\n").append(note1).append("\n\n");
        if (!note2.isEmpty())  sb.append("ENGINE GAME 2 (").append(game2.getName()).append("):\n").append(note2).append("\n\n");
        if (!guard1.isEmpty()) sb.append(guard1).append("\n\n");
        if (!guard2.isEmpty()) sb.append(guard2).append("\n\n");

        String gameLabel1 = isEnglish ? "GAME 1: " : "GAME 1: ";
        String gameLabel2 = isEnglish ? "GAME 2: " : "GAME 2: ";
        String presetYes  = isEnglish ? "Yes" : "Ada";
        String presetNo   = isEnglish ? "No"  : "Tidak ada";

        sb.append(gameLabel1).append(game1.getName())
          .append(" [preset: ").append(preset1 ? presetYes : presetNo).append("]\n");
        sb.append("Genre: ").append(game1.getGenreNames())
          .append(" | MC: ").append(game1.getMetacritic() != null ? game1.getMetacritic() : "N/A").append("\n");
        appendReq(sb, "Min", minReq1);
        sb.append("\n");

        sb.append(gameLabel2).append(game2.getName())
          .append(" [preset: ").append(preset2 ? presetYes : presetNo).append("]\n");
        sb.append("Genre: ").append(game2.getGenreNames())
          .append(" | MC: ").append(game2.getMetacritic() != null ? game2.getMetacritic() : "N/A").append("\n");
        appendReq(sb, "Min", minReq2);
        sb.append("\n");

        String scenarioLabel = isEnglish ? "SCENARIO " : "SKENARIO ";
        sb.append(scenarioLabel).append(game1.getName()).append(":\n")
          .append(getScenarioInstruction(game1.getGenreNames(), game1.getName())).append("\n\n");
        sb.append(scenarioLabel).append(game2.getName()).append(":\n")
          .append(getScenarioInstruction(game2.getGenreNames(), game2.getName())).append("\n\n");

        if (isEnglish) {
            sb.append("=========================================\n");
            sb.append("OUTPUT FORMAT INSTRUCTION — FOLLOW EXACTLY:\n");
            sb.append("NO Markdown tables (|col|), NO **bold**. UPPERCASE headers, bullet •.\n");
            sb.append("EVERY header below MUST be written in English exactly as shown.\n");
            sb.append("=========================================\n\n");

            sb.append("COMPARISON: ").append(game1.getName().toUpperCase())
              .append(" vs ").append(game2.getName().toUpperCase()).append("\n");
            sb.append("Specs: [CPU] + [GPU] [VRAM]GB + RAM [X]GB\n\n");

            sb.append("VERDICT\n");
            sb.append("[2-3 sentences: which game fits/is playable on this spec, real-world reason]\n");
            sb.append("[If either is not playable, state NOT PLAYABLE firmly]\n\n");

            sb.append("OPTIMAL SETTING COMPARISON\n");
            sb.append("• ").append(game1.getName()).append(" : [best resolution + preset ≥45 FPS] → [X-Y FPS]\n");
            sb.append("• ").append(game2.getName()).append(" : [best resolution + preset ≥45 FPS] → [X-Y FPS]\n\n");

            sb.append("---\n\n");

            sb.append(game1.getName().toUpperCase()).append("\n\n");
            sb.append("RESOLUTION LADDER\n");
            if (preset1) {
                sb.append("• High/Ultra 1080p : [FPS] — [playable?]\n");
                sb.append("• Medium     1080p : [FPS] — [playable?]\n");
                sb.append("• Low        1080p : [FPS] — [playable?]\n");
                sb.append("• Low        720p  : [FPS if needed]\n");
                sb.append("• Low        480p (4:3) : [FPS if still needed]\n\n");
            } else {
                sb.append("• Full setting  1080p : [FPS]\n");
                sb.append("• Light setting 1080p : [FPS]\n");
                sb.append("• Light setting 720p  : [FPS if needed]\n\n");
            }
            sb.append("PERFORMANCE PER AREA (setting ").append(setting1).append(")\n");
            sb.append("• [Specific area 1] : [X-Y FPS]\n• [Specific area 2] : [X-Y FPS]\n• [Specific area 3] : [X-Y FPS]\n\n");
            sb.append("DISPLAY MODE RECOMMENDATION\n");
            sb.append("• [Fullscreen/Borderless/Windowed] — [reason + estimated FPS difference]\n\n");
            sb.append("SETTING DETAILS\n");
            sb.append("• [key setting 1] : [value] — [reason]\n• [key setting 2] : [value]\n• DLSS/FSR : [mode]\n\n");

            sb.append("---\n\n");

            sb.append(game2.getName().toUpperCase()).append("\n\n");
            sb.append("RESOLUTION LADDER\n");
            if (preset2) {
                sb.append("• High/Ultra 1080p : [FPS] — [playable?]\n");
                sb.append("• Medium     1080p : [FPS] — [playable?]\n");
                sb.append("• Low        1080p : [FPS] — [playable?]\n");
                sb.append("• Low        720p  : [FPS if needed]\n");
                sb.append("• Low        480p (4:3) : [FPS if still needed]\n\n");
            } else {
                sb.append("• Full setting  1080p : [FPS]\n");
                sb.append("• Light setting 1080p : [FPS]\n");
                sb.append("• Light setting 720p  : [FPS if needed]\n\n");
            }
            sb.append("PERFORMANCE PER AREA (setting ").append(setting2).append(")\n");
            sb.append("• [Specific area 1] : [X-Y FPS]\n• [Specific area 2] : [X-Y FPS]\n• [Specific area 3] : [X-Y FPS]\n\n");
            sb.append("DISPLAY MODE RECOMMENDATION\n");
            sb.append("• [Fullscreen/Borderless/Windowed] — [reason + estimated FPS difference]\n\n");
            sb.append("SETTING DETAILS\n");
            sb.append("• [key setting 1] : [value] — [reason]\n• [key setting 2] : [value]\n• DLSS/FSR : [mode]\n\n");

            sb.append("---\n\n");

            sb.append("TECHNICAL & REAL-WORLD NOTES\n");
            sb.append("• Limiting component for each game\n");
            sb.append("• RAM swap / VRAM overflow / throttle if relevant\n");
            sb.append("• Engine differences affecting performance\n");
            sb.append("• Brief final recommendation\n\n");

            sb.append("=========================================\n");
            sb.append("USER SPECS:\n");
            sb.append("GPU: ").append(user.getGpuName());
            if (!isIGpu) sb.append(", VRAM=").append(user.getVramGb()).append("GB");
            sb.append(", Available RAM ~").append(Math.max(0, availableRam)).append("GB\n");
            sb.append("CPU: ").append(user.getCpuName()).append("\n");
            if (isLaptop) sb.append("PLATFORM: Laptop\n");
            sb.append("\nANALYSIS INSTRUCTIONS:\n");
            if (!benchmarkHints.isEmpty()) {
                sb.append("1. USE the community benchmark data above as the primary FPS anchor\n");
                sb.append("2. Interpolate proportionally if GPU tier differs, state the basis\n");
            } else {
                sb.append("1. Find real benchmarks for this GPU per game — use as anchor\n");
                sb.append("2. Interpolate from the closest GPU if no exact data, state the basis\n");
            }
            sb.append("3. Identify hardware: series, generation, TDP, desktop equivalent\n");
            sb.append("4. Use the RESOLUTION LADDER per game — from High 1080p down to ≥45 FPS\n");
            sb.append("5. Recommend display mode per game + performance reason\n");
            sb.append("6. Analyze each game's engine separately\n");
            sb.append("7. Negative factors ONLY if relevant per game\n");
            sb.append("8. FPS range MAXIMUM 10 numbers gap. 1% Low REQUIRED. Realistic numbers.\n");
            sb.append("9. Write the ENTIRE response in English — including every section header above.\n");

        } else {
            sb.append("=========================================\n");
            sb.append("INSTRUKSI FORMAT OUTPUT — IKUTI PERSIS:\n");
            sb.append("TANPA Markdown tabel (|col|), TANPA **bold**. HURUF KAPITAL header, bullet •.\n");
            sb.append("=========================================\n\n");

            sb.append("PERBANDINGAN: ").append(game1.getName().toUpperCase())
              .append(" vs ").append(game2.getName().toUpperCase()).append("\n");
            sb.append("Spesifikasi: [CPU] + [GPU] [VRAM]GB + RAM [X]GB\n\n");

            sb.append("VERDICT\n");
            sb.append("[2-3 kalimat: game mana lebih cocok/playable di spek ini, alasan real-world]\n");
            sb.append("[Jika salah satu tidak playable, bilang TIDAK PLAYABLE tegas]\n\n");

            sb.append("PERBANDINGAN SETTING OPTIMAL\n");
            sb.append("• ").append(game1.getName()).append(" : [resolusi + preset terbaik ≥45 FPS] → [X-Y FPS]\n");
            sb.append("• ").append(game2.getName()).append(" : [resolusi + preset terbaik ≥45 FPS] → [X-Y FPS]\n\n");

            sb.append("---\n\n");

            sb.append(game1.getName().toUpperCase()).append("\n\n");
            sb.append("TANGGA RESOLUSI\n");
            if (preset1) {
                sb.append("• High/Ultra 1080p : [FPS] — [playable?]\n");
                sb.append("• Medium     1080p : [FPS] — [playable?]\n");
                sb.append("• Low        1080p : [FPS] — [playable?]\n");
                sb.append("• Low        720p  : [FPS jika dibutuhkan]\n");
                sb.append("• Low        480p (4:3) : [FPS jika masih dibutuhkan]\n\n");
            } else {
                sb.append("• Setting penuh  1080p : [FPS]\n");
                sb.append("• Setting ringan 1080p : [FPS]\n");
                sb.append("• Setting ringan 720p  : [FPS jika dibutuhkan]\n\n");
            }
            sb.append("PERFORMA PER AREA (setting ").append(setting1).append(")\n");
            sb.append("• [Area spesifik 1] : [X-Y FPS]\n• [Area spesifik 2] : [X-Y FPS]\n• [Area spesifik 3] : [X-Y FPS]\n\n");
            sb.append("DISPLAY MODE REKOMENDASI\n");
            sb.append("• [Fullscreen/Borderless/Windowed] — [alasan + estimasi perbedaan FPS]\n\n");
            sb.append("SETTING DETAIL\n");
            sb.append("• [setting kunci 1] : [nilai] — [alasan]\n• [setting kunci 2] : [nilai]\n• DLSS/FSR : [mode]\n\n");

            sb.append("---\n\n");

            sb.append(game2.getName().toUpperCase()).append("\n\n");
            sb.append("TANGGA RESOLUSI\n");
            if (preset2) {
                sb.append("• High/Ultra 1080p : [FPS] — [playable?]\n");
                sb.append("• Medium     1080p : [FPS] — [playable?]\n");
                sb.append("• Low        1080p : [FPS] — [playable?]\n");
                sb.append("• Low        720p  : [FPS jika dibutuhkan]\n");
                sb.append("• Low        480p (4:3) : [FPS jika masih dibutuhkan]\n\n");
            } else {
                sb.append("• Setting penuh  1080p : [FPS]\n");
                sb.append("• Setting ringan 1080p : [FPS]\n");
                sb.append("• Setting ringan 720p  : [FPS jika dibutuhkan]\n\n");
            }
            sb.append("PERFORMA PER AREA (setting ").append(setting2).append(")\n");
            sb.append("• [Area spesifik 1] : [X-Y FPS]\n• [Area spesifik 2] : [X-Y FPS]\n• [Area spesifik 3] : [X-Y FPS]\n\n");
            sb.append("DISPLAY MODE REKOMENDASI\n");
            sb.append("• [Fullscreen/Borderless/Windowed] — [alasan + estimasi perbedaan FPS]\n\n");
            sb.append("SETTING DETAIL\n");
            sb.append("• [setting kunci 1] : [nilai] — [alasan]\n• [setting kunci 2] : [nilai]\n• DLSS/FSR : [mode]\n\n");

            sb.append("---\n\n");

            sb.append("CATATAN TEKNIS & REAL-WORLD\n");
            sb.append("• Komponen pembatas di masing-masing game\n");
            sb.append("• RAM swap / VRAM overflow / throttle jika relevan\n");
            sb.append("• Engine differences yang mempengaruhi performa\n");
            sb.append("• Saran akhir singkat\n\n");

            sb.append("=========================================\n");
            sb.append("SPESIFIKASI USER:\n");
            sb.append("GPU: ").append(user.getGpuName());
            if (!isIGpu) sb.append(", VRAM=").append(user.getVramGb()).append("GB");
            sb.append(", RAM tersedia ~").append(Math.max(0, availableRam)).append("GB\n");
            sb.append("CPU: ").append(user.getCpuName()).append("\n");
            if (isLaptop) sb.append("PLATFORM: Laptop\n");
            sb.append("\nINSTRUKSI ANALISIS:\n");
            if (!benchmarkHints.isEmpty()) {
                sb.append("1. GUNAKAN data benchmark komunitas di atas sebagai anchor FPS utama\n");
                sb.append("2. Interpolasi proporsional jika GPU beda tier, sebutkan dasarnya\n");
            } else {
                sb.append("1. Cari benchmark nyata GPU ini per game — gunakan sebagai anchor\n");
                sb.append("2. Interpolasi dari GPU terdekat jika tidak ada data persis, sebut dasarnya\n");
            }
            sb.append("3. Identifikasi hardware: seri, generasi, TDP, setara desktop apa\n");
            sb.append("4. Gunakan TANGGA RESOLUSI per game — dari High 1080p turun sampai ≥45 FPS\n");
            sb.append("5. Rekomendasikan display mode per game + alasan performa\n");
            sb.append("6. Analisis engine masing-masing game secara terpisah\n");
            sb.append("7. Faktor negatif HANYA jika relevan per game\n");
            sb.append("8. Range FPS MAKSIMAL 10 angka. WAJIB 1% Low. Angka nyata.\n");
            sb.append("9. Tulis SELURUH respons dalam Bahasa Indonesia — termasuk setiap header section di atas.\n");
        }

        return sb.toString();
    }

    // =========================================================================
    //  SHARED HELPERS
    // =========================================================================

    private static void appendReq(StringBuilder sb, String label,
                                   SteamRequirement.RequirementSpec req) {
        if (req == null) { sb.append(label).append(": N/A\n"); return; }
        sb.append(label).append(": CPU=").append(req.getProcessor())
          .append(" | RAM=").append(req.getMemory())
          .append(" | GPU=").append(req.getGraphics()).append("\n");
    }

    public static boolean isLaptopCpu(String name) {
        if (name == null) return false;
        String u = name.toUpperCase();
        return u.matches(".*\\d[HUP]X?\\b.*") || u.contains("HX") || u.contains("HS")
            || u.contains("LAPTOP") || u.matches(".*\\d{4}[HU].*")
            || u.matches(".*\\d{4}P\\b.*") || u.matches(".*\\d{3}G\\d.*")
            || u.contains("N4") || u.contains("N5") || u.contains("N6")
            || u.contains("SILVER") || u.contains("ATOM");
    }

    public static boolean isLaptopGpu(String name) {
        if (name == null) return false;
        String u = name.toUpperCase();
        return u.contains("LAPTOP") || u.contains("MOBILE") || u.contains("MAX-Q")
            || u.contains("MAX-P") || u.matches(".*RTX \\d{4}M.*")
            || u.matches(".*RX \\d{4}M.*") || u.contains("MX ");
    }

    public static boolean isIntegratedGpu(String name) {
        if (name == null) return false;
        String u = name.toUpperCase();
        return u.contains("IRIS") || u.contains("INTEL HD") || u.contains("INTEL UHD")
            || u.contains("VEGA") || u.contains("RADEON 6") || u.contains("RADEON 7")
            || u.contains("RADEON 8") || u.contains("780M") || u.contains("680M")
            || u.contains("760M") || u.contains("740M") || u.contains("890M")
            || u.contains("INTEGRATED");
    }
}
