package com.al.comparex.utils;

import java.util.ArrayList;
import com.al.comparex.data.model.SpekUser;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Database CPU dan GPU dengan tier score 0-100.
 *
 * Struktur:
 *  - CPU dikelompokkan: [Laptop] dan [PC Desktop], lalu per-brand/tier
 *  - GPU dikelompokkan: [Laptop GPU], [iGPU / Integrated], [PC Desktop GPU]
 *
 * Format nama: "Intel Core i5-12500H [Laptop]" dsb.  Label [Laptop]/[PC]
 * hanya ada di getCpuNames()/getGpuNames() untuk ditampilkan di dropdown —
 * skor dicari dengan fuzzy-contains sehingga tetap cocok meski ada label.
 *
 * Intel UHD/HD Graphics:
 *  - PC (mis. UHD 770 di desktop): skor lebih tinggi dari laptop
 *  - Laptop UHD/HD: skor ~15-25% lebih rendah karena TDP terbatas
 */
public class HardwareData {

    // =========================================================================
    //  CPU MAP  (nama asli tanpa label → score)
    // =========================================================================
    public static final Map<String, Integer> CPU_MAP = new LinkedHashMap<>();

    // Label grup untuk dropdown (tidak mempengaruhi score lookup)
    private static final List<String> CPU_DISPLAY_LIST  = new ArrayList<>();
    private static final List<String> GPU_DISPLAY_LIST  = new ArrayList<>();

    static {
        // =====================================================================
        //  ── LAPTOP CPU ──────────────────────────────────────────────────────
        // =====================================================================

        // ── Intel Celeron Laptop (N/J series) ────────────────────────────────
        addCpu("Intel Celeron N3350",    2,  true);
        addCpu("Intel Celeron N3450",    2,  true);
        addCpu("Intel Celeron N4000",    2,  true);
        addCpu("Intel Celeron N4100",    3,  true);
        addCpu("Intel Celeron N4120",   3,  true);
        addCpu("Intel Celeron N4500",   4,  true);
        addCpu("Intel Celeron N5100",   5,  true);
        addCpu("Intel Celeron N5105",   5,  true);
        addCpu("Intel Atom x5",          1,  true);
        addCpu("Intel Atom x7",          1,  true);

        // ── Intel Pentium Laptop ──────────────────────────────────────────────
        addCpu("Intel Pentium N4200",    2,  true);
        addCpu("Intel Pentium N5030",   3,  true);
        addCpu("Intel Pentium N6000",   4,  true);
        addCpu("Intel Pentium Silver N5000",  3, true);
        addCpu("Intel Pentium Silver N6000",  4, true);
        addCpu("Intel Pentium Silver J5040",  3, true);
        addCpu("Intel Pentium Gold 7505",     8, true);

        // ── Intel Core i3 Laptop (U series) ──────────────────────────────────
        addCpu("Intel Core i3-1005G1",  8,  true);
        addCpu("Intel Core i3-1115G4",  10,  true);
        addCpu("Intel Core i3-1215U",   13,  true);
        addCpu("Intel Core i3-1305U",   15,  true);

        // ── Intel Core i5 Laptop (U/H/P) ─────────────────────────────────────
        addCpu("Intel Core i5-1035G1",  13,  true);
        addCpu("Intel Core i5-1035G7",  14,  true);
        addCpu("Intel Core i5-1135G7",  17,  true);
        addCpu("Intel Core i5-1155G7",  18,  true);
        addCpu("Intel Core i5-1235U",   20,  true);
        addCpu("Intel Core i5-1240P",   25,  true);
        addCpu("Intel Core i5-1245U",   21,  true);
        addCpu("Intel Core i5-1335U",   23,  true);
        addCpu("Intel Core i5-1340P",   26,  true);
        addCpu("Intel Core i5-12500H",  32,  true);
        addCpu("Intel Core i5-12450H",  27,  true);
        addCpu("Intel Core i5-13420H",  32,  true);
        addCpu("Intel Core i5-13500H",  35,  true);

        // ── Intel Core i7 Laptop (U/H/P/HX) ──────────────────────────────────
        addCpu("Intel Core i7-1065G7",  15,  true);
        addCpu("Intel Core i7-1165G7",  18,  true);
        addCpu("Intel Core i7-1185G7",  20,  true);
        addCpu("Intel Core i7-1255U",   24,  true);
        addCpu("Intel Core i7-1260P",   30,  true);
        addCpu("Intel Core i7-1265U",   25,  true);
        addCpu("Intel Core i7-1355U",   26,  true);
        addCpu("Intel Core i7-1360P",   33,  true);
        addCpu("Intel Core i7-12700H",  42,  true);
        addCpu("Intel Core i7-12650H",  36,  true);
        addCpu("Intel Core i7-13700H",  49,  true);
        addCpu("Intel Core i7-13700HX", 54,  true);
        addCpu("Intel Core i7-13900HX", 62,  true);
        addCpu("Intel Core i7-14700H",  55,  true);
        addCpu("Intel Core i7-14700HX", 58,  true);

        // ── Intel Core i9 Laptop (H/HX) ──────────────────────────────────────
        addCpu("Intel Core i9-12900H",  56,  true);
        addCpu("Intel Core i9-12900HK", 59,  true);
        addCpu("Intel Core i9-13900H",  64,  true);
        addCpu("Intel Core i9-13900HK", 65,  true);
        addCpu("Intel Core i9-13900HX", 69,  true);
        addCpu("Intel Core i9-14900H",  67,  true);
        addCpu("Intel Core i9-14900HX", 73,  true);

        // ── Intel Core Ultra Laptop ───────────────────────────────────────────
        addCpu("Intel Core Ultra 5 125H",  38, true);
        addCpu("Intel Core Ultra 7 155H",  45, true);
        addCpu("Intel Core Ultra 9 185H",  53, true);
        addCpu("Intel Core Ultra 5 125U",  24, true);
        addCpu("Intel Core Ultra 7 155U",  29, true);

        // ── AMD Ryzen Laptop U-series ─────────────────────────────────────────
        addCpu("AMD Ryzen 3 5300U",    15, true);
        addCpu("AMD Ryzen 3 7330U",    16, true);
        addCpu("AMD Ryzen 5 4500U",    17, true);
        addCpu("AMD Ryzen 5 5500U",    22, true);
        addCpu("AMD Ryzen 5 5625U",    23, true);
        addCpu("AMD Ryzen 5 6600U",    27, true);
        addCpu("AMD Ryzen 5 7530U",    27, true);
        addCpu("AMD Ryzen 5 7535U",    29, true);
        addCpu("AMD Ryzen 5 7540U",    30, true);
        addCpu("AMD Ryzen 7 4700U",    24, true);
        addCpu("AMD Ryzen 7 5700U",    29, true);
        addCpu("AMD Ryzen 7 5825U",    31, true);
        addCpu("AMD Ryzen 7 6800U",    35, true);
        addCpu("AMD Ryzen 7 7730U",    36, true);
        addCpu("AMD Ryzen 7 7735U",    38, true);

        // ── AMD Ryzen Laptop H-series ─────────────────────────────────────────
        // ── AMD Ryzen Laptop H-series (35-45W) ──────────────────────────────────
        addCpu("AMD Ryzen 5 4600H",    24, true);
        addCpu("AMD Ryzen 5 5600H",    29, true);
        addCpu("AMD Ryzen 5 6600H",    33, true);
        addCpu("AMD Ryzen 5 7535HS",   34, true);  // Ryzen 7000 HS — laptop gaming
        addCpu("AMD Ryzen 5 7545U",    32, true);
        addCpu("AMD Ryzen 5 8500G",    36, true);   // Phoenix desktop APU
        addCpu("AMD Ryzen 5 8600G",    40, true);
        addCpu("AMD Ryzen 5 8645HS",   38, true);   // Hawk Point HS
        addCpu("AMD Ryzen 7 4800H",    32, true);
        addCpu("AMD Ryzen 7 5700",     35, true);
        addCpu("AMD Ryzen 7 5800H",    39, true);
        addCpu("AMD Ryzen 7 5800HS",   37, true);
        addCpu("AMD Ryzen 7 6800H",    43, true);
        addCpu("AMD Ryzen 7 6800HS",   41, true);
        addCpu("AMD Ryzen 7 7735HS",   44, true);
        addCpu("AMD Ryzen 7 7745HX",   53, true);
        addCpu("AMD Ryzen 7 7840HS",   49, true);   // Phoenix HS
        addCpu("AMD Ryzen 7 7840HX",   56, true);
        addCpu("AMD Ryzen 7 8845HS",   53, true);   // Hawk Point HS
        addCpu("AMD Ryzen 7 8845HX",   60, true);
        addCpu("AMD Ryzen 9 5900HX",   49, true);
        addCpu("AMD Ryzen 9 5980HX",   53, true);
        addCpu("AMD Ryzen 9 6900HX",   54, true);
        addCpu("AMD Ryzen 9 6980HX",   57, true);
        addCpu("AMD Ryzen 9 7940HX",   67, true);
        addCpu("AMD Ryzen 9 7945HX",   73, true);
        addCpu("AMD Ryzen 9 7945HX3D", 75, true);
        addCpu("AMD Ryzen 9 8945HS",   62, true);   // Hawk Point HS
        addCpu("AMD Ryzen AI 9 HX 370",  69, true); // Strix Point
        addCpu("AMD Ryzen AI 9 365",     62, true);
        addCpu("AMD Ryzen AI 7 Pro 360", 53, true);

        // ── AMD Ryzen Desktop tambahan ────────────────────────────────────────
        addCpu("AMD Ryzen 5 5600X3D",  36, false);
        addCpu("AMD Ryzen 5 7600X3D",  45, false);
        addCpu("AMD Ryzen 7 5700X3D",  44, false);
        addCpu("AMD Ryzen 7 7700X",    69, false);  // jika belum ada
        addCpu("AMD Ryzen 9 5900",     63, false);
        addCpu("AMD Ryzen 9 7900",     69, false);
        addCpu("AMD Ryzen 9 7900X3D",  74, false);
        addCpu("AMD Ryzen 9 9900X",    78, false);  // Zen 5
        addCpu("AMD Ryzen 9 9950X",    85, false);

        // =====================================================================
        //  ── PC DESKTOP CPU ──────────────────────────────────────────────────
        // =====================================================================

        // ── Intel Celeron Desktop ─────────────────────────────────────────────
        addCpu("Intel Celeron G4900",   5, false);
        addCpu("Intel Celeron G4930",   5, false);
        addCpu("Intel Celeron G5900",   5, false);
        addCpu("Intel Celeron G5905",   6, false);
        addCpu("Intel Celeron G6900",   6, false);

        // ── Intel Pentium Desktop ─────────────────────────────────────────────
        addCpu("Intel Pentium G4560",   7, false);
        addCpu("Intel Pentium G4600",   7, false);
        addCpu("Intel Pentium G5400",   8, false);
        addCpu("Intel Pentium G5600",   8, false);
        addCpu("Intel Pentium G6400",   8, false);
        addCpu("Intel Pentium Gold G7400", 7, false);

        // ── Intel Core i3 Desktop ─────────────────────────────────────────────
        addCpu("Intel Core i3-6100",   8, false);
        addCpu("Intel Core i3-7100",   9, false);
        addCpu("Intel Core i3-8100",   12, false);
        addCpu("Intel Core i3-9100",   13, false);
        addCpu("Intel Core i3-10100",  15, false);
        addCpu("Intel Core i3-10105",  16, false);
        addCpu("Intel Core i3-12100",  22, false);
        addCpu("Intel Core i3-12100F", 22, false);
        addCpu("Intel Core i3-13100",  24, false);
        addCpu("Intel Core i3-14100",  25, false);

        // ── Intel Core i5 Desktop ─────────────────────────────────────────────
        addCpu("Intel Core i5-6500",   15, false);
        addCpu("Intel Core i5-6600",   15, false);
        addCpu("Intel Core i5-7500",   16, false);
        addCpu("Intel Core i5-8400",   20, false);
        addCpu("Intel Core i5-8500",   21, false);
        addCpu("Intel Core i5-9400",   22, false);
        addCpu("Intel Core i5-9600",   24, false);
        addCpu("Intel Core i5-10400",  25, false);
        addCpu("Intel Core i5-10400F", 26, false);
        addCpu("Intel Core i5-10600",  27, false);
        addCpu("Intel Core i5-11400",  29, false);
        addCpu("Intel Core i5-11400F", 30, false);
        addCpu("Intel Core i5-11600K", 33, false);
        addCpu("Intel Core i5-12400",  36, false);
        addCpu("Intel Core i5-12400F", 37, false);
        addCpu("Intel Core i5-12600K", 45, false);
        addCpu("Intel Core i5-13400",  42, false);
        addCpu("Intel Core i5-13400F", 43, false);
        addCpu("Intel Core i5-13600K", 55, false);
        addCpu("Intel Core i5-14400",  44, false);
        addCpu("Intel Core i5-14600K", 57, false);

        // ── Intel Core i7 Desktop ─────────────────────────────────────────────
        addCpu("Intel Core i7-6700",   21, false);
        addCpu("Intel Core i7-6700K",  23, false);
        addCpu("Intel Core i7-7700",   24, false);
        addCpu("Intel Core i7-7700K",  25, false);
        addCpu("Intel Core i7-8700",   30, false);
        addCpu("Intel Core i7-8700K",  33, false);
        addCpu("Intel Core i7-9700",   35, false);
        addCpu("Intel Core i7-9700K",  38, false);
        addCpu("Intel Core i7-10700",  40, false);
        addCpu("Intel Core i7-10700K", 44, false);
        addCpu("Intel Core i7-11700",  47, false);
        addCpu("Intel Core i7-11700K", 51, false);
        addCpu("Intel Core i7-12700",  58, false);
        addCpu("Intel Core i7-12700K", 64, false);
        addCpu("Intel Core i7-12700F", 56, false);
        addCpu("Intel Core i7-13700",  69, false);
        addCpu("Intel Core i7-13700K", 76, false);
        addCpu("Intel Core i7-14700",  75, false);
        addCpu("Intel Core i7-14700K", 82, false);

        // ── Intel Core i9 Desktop ─────────────────────────────────────────────
        addCpu("Intel Core i9-9900K",  38, false);
        addCpu("Intel Core i9-10900",  47, false);
        addCpu("Intel Core i9-10900K", 53, false);
        addCpu("Intel Core i9-11900K", 56, false);
        addCpu("Intel Core i9-12900",  75, false);
        addCpu("Intel Core i9-12900K", 80, false);
        addCpu("Intel Core i9-12900F", 73, false);
        addCpu("Intel Core i9-13900",  89, false);
        addCpu("Intel Core i9-13900K", 95, false);
        addCpu("Intel Core i9-14900",  93, false);
        addCpu("Intel Core i9-14900K", 100, false);

        // ── Intel Core Ultra Desktop (Arrow Lake) ─────────────────────────────
        addCpu("Intel Core Ultra 5 245K",  55, false);
        addCpu("Intel Core Ultra 7 265K",  73, false);
        addCpu("Intel Core Ultra 9 285K",  84, false);

        // ── AMD Ryzen Desktop 3 ──────────────────────────────────────────────
        addCpu("AMD Ryzen 3 1200",    10, false);
        addCpu("AMD Ryzen 3 2200G",   12, false);
        addCpu("AMD Ryzen 3 3100",    16, false);
        addCpu("AMD Ryzen 3 3300X",   20, false);
        addCpu("AMD Ryzen 3 4300G",   22, false);
        addCpu("AMD Ryzen 3 5300G",   25, false);

        // ── AMD Ryzen Desktop 5 ──────────────────────────────────────────────
        addCpu("AMD Ryzen 5 1600",    18, false);
        addCpu("AMD Ryzen 5 2600",    22, false);
        addCpu("AMD Ryzen 5 2600X",   24, false);
        addCpu("AMD Ryzen 5 3600",    29, false);
        addCpu("AMD Ryzen 5 3600X",   31, false);
        addCpu("AMD Ryzen 5 5500",    35, false);
        addCpu("AMD Ryzen 5 5600",    40, false);
        addCpu("AMD Ryzen 5 5600G",   36, false);
        addCpu("AMD Ryzen 5 5600X",   42, false);
        addCpu("AMD Ryzen 5 7500F",   53, false);
        addCpu("AMD Ryzen 5 7600",    56, false);
        addCpu("AMD Ryzen 5 7600X",   60, false);

        // ── AMD Ryzen Desktop 7 ──────────────────────────────────────────────
        addCpu("AMD Ryzen 7 2700X",   27, false);
        addCpu("AMD Ryzen 7 3700X",   36, false);
        addCpu("AMD Ryzen 7 3800X",   40, false);
        addCpu("AMD Ryzen 7 5700G",   44, false);
        addCpu("AMD Ryzen 7 5700X",   47, false);
        addCpu("AMD Ryzen 7 5800X",   52, false);
        addCpu("AMD Ryzen 7 5800X3D", 55, false);
        addCpu("AMD Ryzen 7 7700",    65, false);
        addCpu("AMD Ryzen 7 7700X",   69, false);
        addCpu("AMD Ryzen 7 7800X3D", 67, false);

        // ── AMD Ryzen Desktop 9 ──────────────────────────────────────────────
        addCpu("AMD Ryzen 9 3900X",   56, false);
        addCpu("AMD Ryzen 9 5900X",   69, false);
        addCpu("AMD Ryzen 9 5950X",   80, false);
        addCpu("AMD Ryzen 9 7900X",   80, false);
        addCpu("AMD Ryzen 9 7950X",   91, false);
        addCpu("AMD Ryzen 9 7950X3D", 95, false);
    }

    // =========================================================================
    //  GPU MAP  (nama asli tanpa label → score)
    // =========================================================================
    public static final Map<String, Integer> GPU_MAP      = new LinkedHashMap<>();
    /** VRAM in GB for each GPU. 0 = iGPU/shared. */
    public static final Map<String, Integer> GPU_VRAM_MAP = new LinkedHashMap<>();

    static {
        // =====================================================================
        //  ── LAPTOP GPU ──────────────────────────────────────────────────────
        // =====================================================================

        // ── Intel iGPU Laptop (skor lebih rendah dari PC karena TDP) ─────────
        addGpu("Intel HD Graphics 620 Laptop",   1,  true);  // U-series
        addGpu("Intel HD Graphics 630 Laptop",   1,  true);  // H-series
        addGpu("Intel UHD Graphics 620 Laptop",  1,  true);
        addGpu("Intel UHD Graphics 630 Laptop", 1,  true);
        addGpu("Intel UHD Graphics Laptop",      1,  true);  // generic no number
        addGpu("Intel Iris Plus Graphics",       2,  true);
        addGpu("Intel Iris Xe Graphics",         5,  true);  // laptop lebih rendah
        addGpu("Intel Iris Xe Max",              7,  true);

        // ── AMD Radeon iGPU Laptop ────────────────────────────────────────────
        addGpu("AMD Radeon Vega 3",   1,  true);
        addGpu("AMD Radeon Vega 6",   2,  true);
        addGpu("AMD Radeon Vega 7",   2,  true);
        addGpu("AMD Radeon Vega 8",   2,  true);
        addGpu("AMD Radeon Vega 10",  2,  true);
        addGpu("AMD Radeon Vega 11",  2,  true);
        addGpu("AMD Radeon 610M",     2,  true);
        addGpu("AMD Radeon 660M",     4,  true);
        addGpu("AMD Radeon 680M",     6,  true);
        addGpu("AMD Radeon 740M",     5,  true);
        addGpu("AMD Radeon 760M",     6,  true);
        addGpu("AMD Radeon 780M",     8,  true);
        addGpu("AMD Radeon 890M",     11,  true);

        // ── Intel Arc Laptop ─────────────────────────────────────────────────
        addGpu("Intel Arc A350M",  9, true);
        addGpu("Intel Arc A370M",  10, true);
        addGpu("Intel Arc A530M",  14, true);
        addGpu("Intel Arc A550M",  16, true);
        addGpu("Intel Arc A730M",  21, true);
        addGpu("Intel Arc A770M",  25, true);

        // ── NVIDIA GTX Laptop ─────────────────────────────────────────────────
        addGpu("NVIDIA GeForce GTX 1050 Laptop",     8, true);
        addGpu("NVIDIA GeForce GTX 1050 Ti Laptop",  10, true);
        addGpu("NVIDIA GeForce GTX 1060 Laptop",     16, true);
        addGpu("NVIDIA GeForce GTX 1650 Laptop",     12, true);
        addGpu("NVIDIA GeForce GTX 1650 Ti Laptop",  14, true);
        addGpu("NVIDIA GeForce GTX 1660 Ti Laptop",  22, true);

        // ── NVIDIA RTX 20xx Laptop ────────────────────────────────────────────
        addGpu("NVIDIA GeForce RTX 2060 Laptop",       25, true);
        addGpu("NVIDIA GeForce RTX 2070 Laptop",       31, true);
        addGpu("NVIDIA GeForce RTX 2080 Laptop",       36, true);
        addGpu("NVIDIA GeForce RTX 2060 Max-Q",        23, true);
        addGpu("NVIDIA GeForce RTX 2070 Max-Q",        28, true);
        addGpu("NVIDIA GeForce RTX 2080 Max-Q",        32, true);

        // ── NVIDIA RTX 30xx Laptop ────────────────────────────────────────────
        addGpu("NVIDIA GeForce RTX 3050 Laptop",    19, true);
        addGpu("NVIDIA GeForce RTX 3050 Ti Laptop", 21, true);
        addGpu("NVIDIA GeForce RTX 3060 Laptop",    30, true);
        addGpu("NVIDIA GeForce RTX 3070 Laptop",    38, true);
        addGpu("NVIDIA GeForce RTX 3070 Ti Laptop", 41, true);
        addGpu("NVIDIA GeForce RTX 3080 Laptop",    46, true);
        addGpu("NVIDIA GeForce RTX 3080 Ti Laptop", 50, true);

        // ── NVIDIA RTX 40xx Laptop ────────────────────────────────────────────
        addGpu("NVIDIA GeForce RTX 4050 Laptop",  29, true);
        addGpu("NVIDIA GeForce RTX 4060 Laptop",  38, true);
        addGpu("NVIDIA GeForce RTX 4070 Laptop",  51, true);
        addGpu("NVIDIA GeForce RTX 4080 Laptop",  64, true);
        addGpu("NVIDIA GeForce RTX 4090 Laptop",  77, true);

        // -- NVIDIA GeForce RTX 50xx Laptop (Blackwell, 2025) --
        addGpu("NVIDIA GeForce RTX 5060 Laptop",  45, true);
        addGpu("NVIDIA GeForce RTX 5070 Laptop",  59, true);
        addGpu("NVIDIA GeForce RTX 5070 Ti Laptop", 70, true);
        addGpu("NVIDIA GeForce RTX 5080 Laptop",  81, true);

        // ── AMD RX Mobile ─────────────────────────────────────────────────────
        addGpu("AMD Radeon RX 5300M",   10, true);
        addGpu("AMD Radeon RX 5500M",   14, true);
        addGpu("AMD Radeon RX 5600M",   20, true);
        addGpu("AMD Radeon RX 6500M",   13, true);
        addGpu("AMD Radeon RX 6600M",   26, true);
        addGpu("AMD Radeon RX 6700M",   35, true);
        addGpu("AMD Radeon RX 6800M",   42, true);
        addGpu("AMD Radeon RX 7600M",   30, true);
        addGpu("AMD Radeon RX 7600M XT",33, true);
        addGpu("AMD Radeon RX 7700S",   35, true);
        addGpu("AMD Radeon RX 7900M",   59, true);

        // =====================================================================
        //  ── PC DESKTOP GPU ───────────────────────────────────────────────────
        // =====================================================================

        // ── Intel iGPU Desktop (lebih tinggi dari laptop karena TDP bebas) ────
        addGpu("Intel HD Graphics 530",   1, false);  // desktop Skylake
        addGpu("Intel HD Graphics 630",   1, false);  // desktop Kaby Lake / CFL
        addGpu("Intel UHD Graphics 630",  1, false);  // Coffee Lake
        addGpu("Intel UHD Graphics 730",  2, false);  // Rocket Lake
        addGpu("Intel UHD Graphics 750",  2, false);  // Rocket Lake iGPU
        addGpu("Intel UHD Graphics 770",  2, false);  // Alder Lake
        addGpu("Intel UHD Graphics",      1, false);  // generic no number
        addGpu("Intel HD Graphics",       1, false);  // generic no number
        addGpu("Intel Iris Xe Graphics",  20, false);  // desktop Iris Xe (lebih tinggi)

        // ── Intel Arc Desktop ─────────────────────────────────────────────────
        addGpu("Intel Arc A380",  14, false);
        addGpu("Intel Arc A580",  26, false);
        addGpu("Intel Arc A750",  31, false);
        addGpu("Intel Arc A770",  36, false);

        // ── AMD iGPU Desktop (APU) ────────────────────────────────────────────
        addGpu("AMD Radeon 610M Desktop",  2, false);
        addGpu("AMD Radeon 680M Desktop",  6, false);
        addGpu("AMD Radeon 780M Desktop",  8, false);
        addGpu("AMD Radeon 890M Desktop",  12, false);

        // ── NVIDIA GeForce GT ─────────────────────────────────────────────────
        addGpu("NVIDIA GeForce GT 710",    1, false);
        addGpu("NVIDIA GeForce GT 730",    1, false);
        addGpu("NVIDIA GeForce GT 1030",  3, false);
        addGpu("NVIDIA GeForce GTX 750",  4, false);
        addGpu("NVIDIA GeForce GTX 750 Ti",6, false);
        addGpu("NVIDIA GeForce GTX 950",  8, false);
        addGpu("NVIDIA GeForce GTX 960",  10, false);
        addGpu("NVIDIA GeForce GTX 970",  15, false);
        addGpu("NVIDIA GeForce GTX 980",  19, false);
        addGpu("NVIDIA GeForce GTX 980 Ti",24, false);

        // ── NVIDIA GTX 10xx Desktop ───────────────────────────────────────────
        addGpu("NVIDIA GeForce GTX 1050",      9, false);
        addGpu("NVIDIA GeForce GTX 1050 Ti",   13, false);
        addGpu("NVIDIA GeForce GTX 1060 3GB",  17, false);
        addGpu("NVIDIA GeForce GTX 1060 6GB",  21, false);
        addGpu("NVIDIA GeForce GTX 1060",      19, false);
        addGpu("NVIDIA GeForce GTX 1070",      29, false);
        addGpu("NVIDIA GeForce GTX 1070 Ti",   33, false);
        addGpu("NVIDIA GeForce GTX 1080",      39, false);
        addGpu("NVIDIA GeForce GTX 1080 Ti",   45, false);

        // ── NVIDIA GTX 16xx Desktop ───────────────────────────────────────────
        addGpu("NVIDIA GeForce GTX 1630",       9, false);
        addGpu("NVIDIA GeForce GTX 1650",       15, false);
        addGpu("NVIDIA GeForce GTX 1650 Super", 19, false);
        addGpu("NVIDIA GeForce GTX 1660",       25, false);
        addGpu("NVIDIA GeForce GTX 1660 Super", 30, false);
        addGpu("NVIDIA GeForce GTX 1660 Ti",    32, false);

        // ── NVIDIA RTX 20xx Desktop ───────────────────────────────────────────
        addGpu("NVIDIA GeForce RTX 2060",        32, false);
        addGpu("NVIDIA GeForce RTX 2060 Super",  36, false);
        addGpu("NVIDIA GeForce RTX 2070",        40, false);
        addGpu("NVIDIA GeForce RTX 2070 Super",  44, false);
        addGpu("NVIDIA GeForce RTX 2080",        48, false);
        addGpu("NVIDIA GeForce RTX 2080 Super",  50, false);
        addGpu("NVIDIA GeForce RTX 2080 Ti",     55, false);

        // ── NVIDIA RTX 30xx Desktop ───────────────────────────────────────────
        addGpu("NVIDIA GeForce RTX 3050",        26, false);
        addGpu("NVIDIA GeForce RTX 3050 Ti",     28, false);
        addGpu("NVIDIA GeForce RTX 3060",        38, false);
        addGpu("NVIDIA GeForce RTX 3060 Ti",     48, false);
        addGpu("NVIDIA GeForce RTX 3070",        57, false);
        addGpu("NVIDIA GeForce RTX 3070 Ti",     61, false);
        addGpu("NVIDIA GeForce RTX 3080",        69, false);
        addGpu("NVIDIA GeForce RTX 3080 Ti",     75, false);
        addGpu("NVIDIA GeForce RTX 3090",        80, false);
        addGpu("NVIDIA GeForce RTX 3090 Ti",     84, false);

        // ── NVIDIA RTX 40xx Desktop ───────────────────────────────────────────
        addGpu("NVIDIA GeForce RTX 4060",         47, false);
        addGpu("NVIDIA GeForce RTX 4060 Ti",      59, false);
        addGpu("NVIDIA GeForce RTX 4060 Ti 16GB", 60, false);
        addGpu("NVIDIA GeForce RTX 4070",         72, false);
        addGpu("NVIDIA GeForce RTX 4070 Super",   78, false);
        addGpu("NVIDIA GeForce RTX 4070 Ti",      84, false);
        addGpu("NVIDIA GeForce RTX 4070 Ti Super",88, false);
        addGpu("NVIDIA GeForce RTX 4080",         93, false);
        addGpu("NVIDIA GeForce RTX 4080 Super",   96, false);
        addGpu("NVIDIA GeForce RTX 4090",        100, false);

        // -- NVIDIA GeForce RTX 50xx Desktop (Blackwell, 2025) --
        addGpu("NVIDIA GeForce RTX 5060",         61, false);
        addGpu("NVIDIA GeForce RTX 5060 Ti",      68, false);
        addGpu("NVIDIA GeForce RTX 5070",         85, false);
        addGpu("NVIDIA GeForce RTX 5070 Ti",      95, false);
        addGpu("NVIDIA GeForce RTX 5080",         100, false);
        addGpu("NVIDIA GeForce RTX 5090",        100, false);

        // ── AMD Radeon RX 400/500 Desktop ────────────────────────────────────
        addGpu("AMD Radeon RX 460",      9, false);
        addGpu("AMD Radeon RX 470",      14, false);
        addGpu("AMD Radeon RX 480",      18, false);
        addGpu("AMD Radeon RX 480 8GB",  18, false);
        addGpu("AMD Radeon RX 560",      10, false);
        addGpu("AMD Radeon RX 570",      16, false);
        addGpu("AMD Radeon RX 580",      19, false);
        addGpu("AMD Radeon RX 580 8GB",  20, false);
        addGpu("AMD Radeon RX 590",      22, false);

        // ── AMD Radeon RX 5000 Desktop ────────────────────────────────────────
        addGpu("AMD Radeon RX 5500 XT",    20, false);
        addGpu("AMD Radeon RX 5600 XT",    28, false);
        addGpu("AMD Radeon RX 5700",       34, false);
        addGpu("AMD Radeon RX 5700 XT",    38, false);

        // ── AMD Radeon RX 6000 Desktop ────────────────────────────────────────
        addGpu("AMD Radeon RX 6400",       16, false);
        addGpu("AMD Radeon RX 6500 XT",    18, false);
        addGpu("AMD Radeon RX 6600",       36, false);
        addGpu("AMD Radeon RX 6600 XT",    41, false);
        addGpu("AMD Radeon RX 6650 XT",    43, false);
        addGpu("AMD Radeon RX 6700",       47, false);
        addGpu("AMD Radeon RX 6700 XT",    53, false);
        addGpu("AMD Radeon RX 6750 XT",    55, false);
        addGpu("AMD Radeon RX 6800",       65, false);
        addGpu("AMD Radeon RX 6800 XT",    72, false);
        addGpu("AMD Radeon RX 6900 XT",    77, false);
        addGpu("AMD Radeon RX 6950 XT",    80, false);

        // ── AMD Radeon RX 7000 Desktop ────────────────────────────────────────
        addGpu("AMD Radeon RX 7600",       43, false);
        addGpu("AMD Radeon RX 7600 XT",    50, false);
        addGpu("AMD Radeon RX 7700 XT",    59, false);
        addGpu("AMD Radeon RX 7800 XT",    70, false);
        addGpu("AMD Radeon RX 7900 GRE",   78, false);
        addGpu("AMD Radeon RX 7900 XT",    86, false);
        addGpu("AMD Radeon RX 7900 XTX",   92, false);
        addGpu("AMD Radeon RX 9070",       80, false);
        addGpu("AMD Radeon RX 9070 XT",    89, false);
    }

    // =========================================================================
    //  Helper: add with label to display list
    // =========================================================================
    private static void addCpu(String name, int score, boolean isLaptop) {
        CPU_MAP.put(name, score);
        CPU_DISPLAY_LIST.add(name);
    }

    private static void addGpu(String name, int score, boolean isLaptop) {
        GPU_MAP.put(name, score);
        GPU_VRAM_MAP.put(name, inferVram(name, score));
        GPU_DISPLAY_LIST.add(name);
    }

    /**
     * Infer VRAM from GPU name keywords or score.
     * !! LAPTOP vs DESKTOP VRAM BERBEDA !!
     *   RTX 3060 Desktop  = 12GB | RTX 3060 Laptop  = 6GB
     *   RTX 3080 Desktop  = 10GB | RTX 3080 Laptop  = 8GB/16GB
     *   RTX 4070 Desktop  = 12GB | RTX 4070 Laptop  = 8GB
     * Priority: name-model match > explicit GB in name > score
     */
    private static int inferVram(String name, int score) {
        String n = name.toLowerCase();
        boolean isLaptop = n.contains("laptop") || n.contains("max-q")
                        || n.contains("mobile") || n.endsWith("m");

        // Explicit VRAM in name takes priority (e.g. "RTX 4060 Ti 16GB")
        if (n.contains("16gb") || n.contains("16 gb")) return 16;
        if (n.contains("12gb") || n.contains("12 gb")) return 12;
        if (n.contains("10gb") || n.contains("10 gb")) return 10;
        if (n.contains("8gb")  || n.contains("8 gb"))  return 8;
        if (n.contains("6gb")  || n.contains("6 gb"))  return 6;
        if (n.contains("4gb")  || n.contains("4 gb"))  return 4;
        if (n.contains("3gb")  || n.contains("3 gb"))  return 3;
        if (n.contains("2gb")  || n.contains("2 gb"))  return 2;

        // iGPU → 0 (shared)
        if (n.contains("intel hd") || n.contains("intel uhd") || n.contains("iris")
                || n.contains("vega 3") || n.contains("vega 6") || n.contains("vega 7")
                || n.contains("vega 8") || n.contains("vega 10") || n.contains("vega 11")
                || n.contains("radeon 610m") || n.contains("radeon 660m")
                || n.contains("radeon 680m") || n.contains("radeon 740m")
                || n.contains("radeon 760m") || n.contains("radeon 780m")
                || n.contains("radeon 890m")) return 0;

        // ── NVIDIA RTX 40xx (Laptop VRAM berbeda dari Desktop!) ───────────────
        if (n.contains("4090"))              return isLaptop ? 16 : 24;
        if (n.contains("4080 super"))        return isLaptop ? 12 : 16;
        if (n.contains("4080"))              return isLaptop ? 12 : 16;
        if (n.contains("4070 ti super"))     return 16;  // desktop only
        if (n.contains("4070 ti"))           return 16;  // desktop only
        if (n.contains("4070 super"))        return isLaptop ?  8 : 12;
        if (n.contains("4070"))              return isLaptop ?  8 : 12;
        if (n.contains("4060 ti 16"))        return 16;  // desktop special
        if (n.contains("4060 ti"))           return isLaptop ?  8 :  8;
        if (n.contains("4060"))              return isLaptop ?  8 :  8;
        if (n.contains("4050"))              return isLaptop ?  6 :  8;
        // ── NVIDIA RTX 30xx ────────────────────────────────────────────────
        if (n.contains("3090 ti"))           return 24;
        if (n.contains("3090"))              return 24;
        if (n.contains("3080 ti"))           return isLaptop ? 16 : 12;
        if (n.contains("3080 12"))           return 12;
        if (n.contains("3080"))              return isLaptop ?  8 : 10;
        if (n.contains("3070 ti"))           return isLaptop ?  8 :  8;
        if (n.contains("3070"))              return isLaptop ?  8 :  8;
        if (n.contains("3060 ti"))           return  8;  // desktop only
        // !! CRITICAL: RTX 3060 Desktop=12GB, Laptop=6GB !!
        if (n.contains("3060"))              return isLaptop ?  6 : 12;
        if (n.contains("3050 ti"))           return isLaptop ?  4 :  8;
        if (n.contains("3050"))              return isLaptop ?  4 :  8;
        // ── NVIDIA RTX 20xx ────────────────────────────────────────────────
        if (n.contains("2080 ti"))           return 11;
        if (n.contains("2080"))              return  8;
        if (n.contains("2070 super"))        return  8;
        if (n.contains("2070"))              return  8;
        if (n.contains("2060 super"))        return  8;
        if (n.contains("2060"))              return isLaptop ?  6 :  6;
        // ── NVIDIA GTX 16xx ────────────────────────────────────────────────
        if (n.contains("1660 ti"))           return isLaptop ?  6 :  6;
        if (n.contains("1660 super"))        return  6;
        if (n.contains("1660"))              return  6;
        if (n.contains("1650 super"))        return  4;
        if (n.contains("1650"))              return isLaptop ?  4 :  4;
        // ── NVIDIA GTX 10xx ────────────────────────────────────────────────
        if (n.contains("1080 ti"))           return 11;
        if (n.contains("1080"))              return  8;
        if (n.contains("1070 ti"))           return  8;
        if (n.contains("1070"))              return  8;
        if (n.contains("1060 6") || (n.contains("1060") && !n.contains("3"))) return 6;
        if (n.contains("1060 3"))            return  3;
        if (n.contains("1060"))              return isLaptop ?  6 :  6;
        if (n.contains("1050 ti"))           return  4;
        if (n.contains("1050"))              return isLaptop ?  2 :  2;
        // ── AMD RX 7000 ────────────────────────────────────────────────────
        if (n.contains("7900 xtx"))          return 24;
        if (n.contains("7900"))              return isLaptop ? 16 : 20;
        if (n.contains("7800 xt"))           return 16;
        if (n.contains("7700 xt"))           return 12;
        if (n.contains("7600m xt"))          return  8;
        if (n.contains("7600m"))             return  8;
        if (n.contains("7600"))              return  8;
        // ── AMD RX 6000 ────────────────────────────────────────────────────
        if (n.contains("6950 xt"))           return 16;
        if (n.contains("6900 xt"))           return 16;
        if (n.contains("6800m"))             return 12;
        if (n.contains("6800"))              return 16;
        if (n.contains("6750 xt"))           return 12;
        if (n.contains("6700m"))             return 10;
        if (n.contains("6700 xt") || n.contains("6700")) return 12;
        if (n.contains("6650 xt"))           return  8;
        if (n.contains("6600m"))             return  8;
        if (n.contains("6600 xt") || n.contains("6600")) return  8;
        if (n.contains("6500m"))             return  4;
        if (n.contains("6500 xt"))           return  4;
        if (n.contains("6400"))              return  4;
        // ── AMD RX 5000 ────────────────────────────────────────────────────
        if (n.contains("5700 xt") || n.contains("5700")) return  8;
        if (n.contains("5600m"))             return  6;
        if (n.contains("5600 xt"))           return  6;
        if (n.contains("5500m"))             return  4;
        if (n.contains("5500 xt"))           return  8;
        // ── AMD RX 400/500 ─────────────────────────────────────────────────
        if (n.contains("590") || n.contains("580") || n.contains("570")) return 8;
        if (n.contains("480") || n.contains("470")) return 8;
        if (n.contains("460"))               return  4;
        // ── Intel Arc Desktop ──────────────────────────────────────────────
        if (n.contains("arc a770"))          return 16;
        if (n.contains("arc a750"))          return  8;
        if (n.contains("arc a580"))          return  8;
        if (n.contains("arc a380"))          return  6;
        // ── Intel Arc Laptop ───────────────────────────────────────────────
        if (n.contains("arc a770m"))         return 16;
        if (n.contains("arc a730m"))         return 12;
        if (n.contains("arc a550m"))         return  8;
        if (n.contains("arc a530m"))         return  8;
        if (n.contains("arc a370m"))         return  4;
        if (n.contains("arc a350m"))         return  4;
        // ── Fallback by score tier ─────────────────────────────────────────
        if (score >= 90)  return isLaptop ? 12 : 16;
        if (score >= 75)  return isLaptop ?  8 : 12;
        if (score >= 60)  return isLaptop ?  6 :  8;
        if (score >= 45)  return isLaptop ?  6 :  6;
        if (score >= 30)  return  4;
        if (score >= 20)  return  2;
        return 0; // iGPU or unknown
    }

    // =========================================================================
    //  Accessors
    // =========================================================================

    /** Returns CPU names grouped with clear Laptop / PC Desktop headers. */
    /**
     * Returns VRAM in GB for a given GPU name.
     * Returns 0 for iGPU (shared VRAM), -1 if unknown.
     */
    public static int getVramForGpu(String name) {
        if (name == null || name.isEmpty()) return -1;
        Integer v = GPU_VRAM_MAP.get(name);
        if (v != null) return v;
        // Fuzzy fallback
        String lower = name.toLowerCase();
        for (Map.Entry<String, Integer> e : GPU_VRAM_MAP.entrySet())
            if (lower.contains(e.getKey().toLowerCase())) return e.getValue();
        return SpekUser.estimateVramFromScore(getGpuScore(name));
    }

    public static String[] getCpuNames() {
        List<String> result = new ArrayList<>();
        result.add("━━━ LAPTOP ━━━━━━━━━━━━━━━━━━━━━━");
        boolean inLaptop = true;
        for (String name : CPU_DISPLAY_LIST) {
            if (inLaptop && name.equals("Intel Celeron G4900")) {
                result.add("━━━  PC DESKTOP ━━━━━━━━━━━━━━━");
                inLaptop = false;
            }
            result.add(name);
        }
        return result.toArray(new String[0]);
    }

    /** Returns GPU names grouped with Laptop / PC Desktop headers. */
    public static String[] getGpuNames() {
        List<String> result = new ArrayList<>();
        result.add("━━━ LAPTOP GPU ━━━━━━━━━━━━━━━━━━");
        boolean inLaptop = true;
        for (String name : GPU_DISPLAY_LIST) {
            if (inLaptop && name.equals("Intel HD Graphics 530")) {
                result.add("━━━  PC DESKTOP GPU ━━━━━━━━━━━");
                inLaptop = false;
            }
            result.add(name);
        }
        return result.toArray(new String[0]);
    }

    /**
     * Lookup CPU score with fuzzy fallback.
     * 1. Exact match
     * 2. Case-insensitive contains (longest key wins to avoid false positives)
     */
    public static int getCpuScore(String name) {
        if (name == null || name.isEmpty()) return 0;
        // 1. Exact match
        Integer exact = CPU_MAP.get(name);
        if (exact != null) return exact;
        // 2. Fuzzy: find longest matching key (avoids "i5" matching "i5-12500H")
        String lower = name.toLowerCase().trim();
        String bestKey = null;
        int bestScore = 0;
        for (Map.Entry<String, Integer> e : CPU_MAP.entrySet()) {
            String k = e.getKey().toLowerCase();
            if (lower.contains(k) && k.length() > (bestKey != null ? bestKey.length() : 0)) {
                bestKey   = k;
                bestScore = e.getValue();
            }
        }
        return bestScore;
    }

    /**
     * Lookup GPU score with fuzzy fallback.
     * 1. Exact match
     * 2. Case-insensitive contains (longest key wins)
     */
    public static int getGpuScore(String name) {
        if (name == null || name.isEmpty()) return 0;
        // 1. Exact match
        Integer exact = GPU_MAP.get(name);
        if (exact != null) return exact;
        // 2. Fuzzy
        String lower = name.toLowerCase().trim();
        String bestKey = null;
        int bestScore = 0;
        for (Map.Entry<String, Integer> e : GPU_MAP.entrySet()) {
            String k = e.getKey().toLowerCase();
            if (lower.contains(k) && k.length() > (bestKey != null ? bestKey.length() : 0)) {
                bestKey   = k;
                bestScore = e.getValue();
            }
        }
        return bestScore;
    }

    // =========================================================================
    //  Bottleneck detection
    // =========================================================================
    public static String detectBottleneck(
            int userCpu, int userGpu, int userRam,
            int reqCpu,  int reqGpu,  int reqRam) {
        double cpuRatio = reqCpu > 0 ? (double) userCpu / reqCpu : 1.0;
        double gpuRatio = reqGpu > 0 ? (double) userGpu / reqGpu : 1.0;
        double ramRatio = reqRam > 0 ? (double) userRam / reqRam : 1.0;
        if (ramRatio < 1.0) return "RAM";
        double cpuSlack = cpuRatio - 1.0;
        double gpuSlack = gpuRatio - 1.0;
        if (gpuSlack < cpuSlack - 0.25 && gpuRatio < 1.5) return "GPU";
        if (cpuSlack < gpuSlack - 0.25 && cpuRatio < 1.5) return "CPU";
        if (gpuRatio < 1.2 && cpuRatio >= 1.3) return "GPU";
        if (cpuRatio < 1.2 && gpuRatio >= 1.3) return "CPU";
        if (gpuRatio < 1.0) return "GPU";
        if (cpuRatio < 1.0) return "CPU";
        return "None";
    }
}
