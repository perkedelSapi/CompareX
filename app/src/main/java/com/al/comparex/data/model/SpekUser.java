package com.al.comparex.data.model;

/**
 * Spesifikasi hardware user.
 *
 * Fields:
 *  cpuName / cpuScore  — nama dan tier score CPU (0-100)
 *  gpuName / gpuScore  — nama dan tier score GPU (0-100)
 *  vramGb              — kapasitas VRAM GPU dalam GB (penting untuk resolusi tinggi)
 *  ramGb               — kapasitas RAM sistem dalam GB
 */
public class SpekUser {

    private String cpuName;
    private int    cpuScore;

    private String gpuName;
    private int    gpuScore;
    private int    vramGb;   // VRAM GPU

    private int    ramGb;    // RAM sistem

    public SpekUser(String cpuName, int cpuScore,
                    String gpuName, int gpuScore, int vramGb,
                    int ramGb) {
        this.cpuName  = cpuName;
        this.cpuScore = cpuScore;
        this.gpuName  = gpuName;
        this.gpuScore = gpuScore;
        this.vramGb   = vramGb;
        this.ramGb    = ramGb;
    }

    /** Backward-compat: tanpa VRAM (estimasi dari GPU score). */
    public SpekUser(String cpuName, int cpuScore,
                    String gpuName, int gpuScore,
                    int ramGb) {
        this(cpuName, cpuScore, gpuName, gpuScore,
             estimateVramFromScore(gpuScore), ramGb);
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getCpuName()  { return cpuName;  }
    public int    getCpuScore() { return cpuScore; }
    public String getGpuName()  { return gpuName;  }
    public int    getGpuScore() { return gpuScore; }
    public int    getVramGb()   { return vramGb;   }
    public int    getRamGb()    { return ramGb;    }

    @Override
    public String toString() {
        return "CPU : " + cpuName + "\n"
             + "GPU : " + gpuName
             + (vramGb > 0 ? "  VRAM: " + vramGb + "GB" : "") + "\n"
             + "RAM : " + ramGb + " GB";
    }

    /**
     * Estimasi VRAM dari tier score jika user tidak input manual.
     * Digunakan sebagai fallback saat load dari SharedPreferences lama.
     */
    public static int estimateVramFromScore(int gpuScore) {
        if (gpuScore <= 0)  return 0;   // iGPU / tidak diketahui
        if (gpuScore <= 12) return 0;   // iGPU (shared VRAM)
        if (gpuScore <= 20) return 2;   // GTX 750 Ti era
        if (gpuScore <= 32) return 4;   // GTX 1050 Ti / RX 570
        if (gpuScore <= 45) return 6;   // GTX 1060 6GB / RX 580
        if (gpuScore <= 60) return 8;   // RTX 3050 / RX 6600
        if (gpuScore <= 75) return 8;   // RTX 3060 / RX 6700 XT
        if (gpuScore <= 88) return 12;  // RTX 3080 / RX 6800 XT
        return 16;                      // RTX 4080+ / RX 7900 XTX
    }
}
