package com.al.comparex.ui.compare;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.al.comparex.R;
import com.al.comparex.data.model.GameDetail;
import com.al.comparex.data.model.GeminiRequest;
import com.al.comparex.data.model.GeminiResponse;
import com.al.comparex.data.model.SpekUser;
import com.al.comparex.data.model.SteamRequirement;
import com.al.comparex.data.model.SteamSearchResponse;
import com.al.comparex.data.network.ApiClient;
import com.al.comparex.data.repository.GameRepository;
import com.al.comparex.utils.AiAnalysisBuilder;
import com.al.comparex.utils.LangPrefs;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * AndroidViewModel agar punya akses Application Context untuk getString()
 * sehingga semua pesan error/status mengikuti bahasa yang dipilih user.
 */
public class CompareViewModel extends AndroidViewModel {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final int  MAX_RETRIES   = 3;
    private static final long BASE_DELAY_MS = 5000L;
    private int retryCount = 0;

    // Game 1
    private final MutableLiveData<GameRepository.Resource<GameDetail>>       detail1 = new MutableLiveData<>();
    private final MutableLiveData<GameRepository.Resource<SteamRequirement>> steam1  = new MutableLiveData<>();
    // Game 2
    private final MutableLiveData<GameRepository.Resource<GameDetail>>       detail2 = new MutableLiveData<>();
    private final MutableLiveData<GameRepository.Resource<SteamRequirement>> steam2  = new MutableLiveData<>();
    // AI
    private final MutableLiveData<String>  aiAnalysis    = new MutableLiveData<>();
    private final MutableLiveData<Boolean> aiHasAnchorData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> aiLoading     = new MutableLiveData<>(false);
    private final MutableLiveData<String>  aiError       = new MutableLiveData<>();
    private final MutableLiveData<String>  aiRetryStatus = new MutableLiveData<>();

    private Call<GameDetail>          activeDetail1Call;
    private Call<GameDetail>          activeDetail2Call;
    private Call<SteamSearchResponse> activeSteamSearch1Call;
    private Call<SteamSearchResponse> activeSteamSearch2Call;
    private Call<SteamRequirement>    activeSteamReq1Call;
    private Call<SteamRequirement>    activeSteamReq2Call;
    private Call<GeminiResponse>      activeAiCall;

    private boolean loaded = false;

    public CompareViewModel(@NonNull Application application) {
        super(application);
    }

    private String s(int resId)                      { return getApplication().getString(resId); }
    private String s(int resId, Object... formatArgs) { return getApplication().getString(resId, formatArgs); }
    private boolean isEnglish()                       { return LangPrefs.isEnglish(getApplication()); }

    // ── Exposed LiveData ──────────────────────────────────────────────────────
    public LiveData<GameRepository.Resource<GameDetail>>       getDetail1()       { return detail1;       }
    public LiveData<GameRepository.Resource<SteamRequirement>> getSteam1()        { return steam1;        }
    public LiveData<GameRepository.Resource<GameDetail>>       getDetail2()       { return detail2;       }
    public LiveData<GameRepository.Resource<SteamRequirement>> getSteam2()        { return steam2;        }
    public LiveData<String>                                    getAiAnalysis()      { return aiAnalysis;      }
    public LiveData<Boolean>                                   getAiHasAnchorData() { return aiHasAnchorData; }
    public LiveData<Boolean>                                   getAiLoading()     { return aiLoading;     }
    public LiveData<String>                                    getAiError()       { return aiError;       }
    public LiveData<String>                                    getAiRetryStatus() { return aiRetryStatus; }

    // ── Load games ────────────────────────────────────────────────────────────

    public void loadGames(int gameId1, int gameId2) {
        if (loaded) return;
        loaded = true;
        loadGameDetail(gameId1, true);
        loadGameDetail(gameId2, false);
    }

    private void loadGameDetail(int gameId, boolean isGame1) {
        MutableLiveData<GameRepository.Resource<GameDetail>> detailLd = isGame1 ? detail1 : detail2;
        detailLd.setValue(GameRepository.Resource.loading(null));

        Call<GameDetail> call = ApiClient.getRawgApi().getGameDetail(gameId, ApiClient.RAWG_API_KEY);
        if (isGame1) activeDetail1Call = call; else activeDetail2Call = call;

        call.enqueue(new Callback<GameDetail>() {
            @Override public void onResponse(Call<GameDetail> c, Response<GameDetail> r) {
                if (r.isSuccessful() && r.body() != null) {
                    GameDetail gd = r.body();
                    detailLd.setValue(GameRepository.Resource.success(gd));
                    String steamId = (gd.getSteamAppId() != null && gd.getSteamAppId() > 0)
                            ? String.valueOf(gd.getSteamAppId()) : null;
                    fetchSteamRequirements(steamId, gd.getName(), isGame1);
                } else {
                    detailLd.setValue(GameRepository.Resource.error(
                            s(R.string.msg_detail_load_failed, r.code()), null));
                }
            }
            @Override public void onFailure(Call<GameDetail> c, Throwable t) {
                if (!c.isCanceled())
                    detailLd.setValue(GameRepository.Resource.error(
                            s(R.string.msg_connection_failed, String.valueOf(t.getMessage())), null));
            }
        });
    }

    private void fetchSteamRequirements(String steamAppId, String gameName, boolean isGame1) {
        MutableLiveData<GameRepository.Resource<SteamRequirement>> steamLd = isGame1 ? steam1 : steam2;
        steamLd.setValue(GameRepository.Resource.loading(null));
        if (steamAppId != null && !steamAppId.isEmpty() && !steamAppId.equals("0")) {
            fetchSteamByAppId(steamAppId, isGame1);
        } else {
            searchSteamStore(gameName, isGame1);
        }
    }

    private void searchSteamStore(String gameName, boolean isGame1) {
        Call<SteamSearchResponse> call = ApiClient.getSteamStoreApi()
                .searchGame(gameName, "english", "US");
        if (isGame1) activeSteamSearch1Call = call; else activeSteamSearch2Call = call;

        MutableLiveData<GameRepository.Resource<SteamRequirement>> steamLd = isGame1 ? steam1 : steam2;
        call.enqueue(new Callback<SteamSearchResponse>() {
            @Override public void onResponse(Call<SteamSearchResponse> c, Response<SteamSearchResponse> r) {
                if (r.isSuccessful() && r.body() != null
                        && r.body().getItems() != null
                        && !r.body().getItems().isEmpty()) {
                    fetchSteamByAppId(String.valueOf(r.body().getItems().get(0).getId()), isGame1);
                } else {
                    steamLd.setValue(GameRepository.Resource.error(s(R.string.msg_steam_not_found), null));
                }
            }
            @Override public void onFailure(Call<SteamSearchResponse> c, Throwable t) {
                if (!c.isCanceled())
                    steamLd.setValue(GameRepository.Resource.error(s(R.string.msg_steam_search_failed), null));
            }
        });
    }

    private void fetchSteamByAppId(String appId, boolean isGame1) {
        Call<SteamRequirement> call = ApiClient.getSteamApi().getRequirements(appId);
        if (isGame1) activeSteamReq1Call = call; else activeSteamReq2Call = call;

        MutableLiveData<GameRepository.Resource<SteamRequirement>> steamLd = isGame1 ? steam1 : steam2;
        call.enqueue(new Callback<SteamRequirement>() {
            @Override public void onResponse(Call<SteamRequirement> c, Response<SteamRequirement> r) {
                if (r.isSuccessful() && r.body() != null && r.body().isSuccess())
                    steamLd.setValue(GameRepository.Resource.success(r.body()));
                else
                    steamLd.setValue(GameRepository.Resource.error(s(R.string.msg_steam_req_unavailable), null));
            }
            @Override public void onFailure(Call<SteamRequirement> c, Throwable t) {
                if (!c.isCanceled())
                    steamLd.setValue(GameRepository.Resource.error(s(R.string.msg_steam_api_failed), null));
            }
        });
    }

    // ── Requirements resolvers ────────────────────────────────────────────────

    public SteamRequirement.RequirementSpec getMinSpec(boolean isGame1) { return resolveSpec(true,  isGame1); }
    public SteamRequirement.RequirementSpec getRecSpec(boolean isGame1) { return resolveSpec(false, isGame1); }

    private SteamRequirement.RequirementSpec resolveSpec(boolean isMin, boolean isGame1) {
        GameRepository.Resource<SteamRequirement> steamRes  = isGame1 ? steam1.getValue()  : steam2.getValue();
        GameRepository.Resource<GameDetail>        detailRes = isGame1 ? detail1.getValue() : detail2.getValue();

        if (steamRes != null && steamRes.isSuccess() && steamRes.getData() != null
                && steamRes.getData().getData() != null
                && steamRes.getData().getData().getSystemRequirements() != null) {
            SteamRequirement.SystemRequirements sys = steamRes.getData().getData().getSystemRequirements();
            SteamRequirement.RequirementSpec spec = isMin ? sys.getMinimum() : sys.getRecommended();
            if (spec != null) return spec;
        }
        if (detailRes != null && detailRes.getData() != null) {
            String raw = isMin ? detailRes.getData().getRawgMinRequirements()
                               : detailRes.getData().getRawgRecRequirements();
            if (raw != null && !raw.isEmpty()) return parseRawg(raw);
        }
        return null;
    }

    // ── AI Analysis ───────────────────────────────────────────────────────────

    public void requestAiAnalysis(SpekUser user) {
        GameRepository.Resource<GameDetail> gd1 = detail1.getValue();
        GameRepository.Resource<GameDetail> gd2 = detail2.getValue();

        if (gd1 == null || !gd1.isSuccess() || gd1.getData() == null
         || gd2 == null || !gd2.isSuccess() || gd2.getData() == null
         || user == null) {
            aiError.setValue(s(R.string.msg_compare_data_not_ready));
            return;
        }
        retryCount = 0;
        aiError.setValue(null);
        aiRetryStatus.setValue(null);
        aiAnalysis.setValue(null);

        boolean isEnglish = isEnglish();
        boolean hasAnchor = AiAnalysisBuilder.hasAnyAnchorData(
                user.getGpuName(), gd1.getData().getName(), gd2.getData().getName());
        aiHasAnchorData.setValue(hasAnchor);

        String prompt = AiAnalysisBuilder.buildComparePrompt(
                user,
                gd1.getData(), resolveSpec(true, true),
                gd2.getData(), resolveSpec(true, false), isEnglish);
        callDeepSeek(new GeminiRequest(AiAnalysisBuilder.getCompareSystemPrompt(isEnglish), prompt));
    }

    // ── DeepSeek call with auto-retry ─────────────────────────────────────────

    private void callDeepSeek(GeminiRequest request) {
        aiLoading.setValue(true);
        if (activeAiCall != null) activeAiCall.cancel();
        activeAiCall = ApiClient.getGeminiApi()
                .generate("Bearer " + ApiClient.AI_API_KEY, request);
        activeAiCall.enqueue(new Callback<GeminiResponse>() {
            @Override public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GeminiResponse body = response.body();
                    if (body.getError() != null) {
                        handleFailure(s(R.string.msg_deepseek_error, body.getError().getMessage()), request);
                        return;
                    }
                    String text = body.getFirstText();
                    if (text != null && !text.isEmpty()) {
                        aiLoading.setValue(false);
                        aiRetryStatus.setValue(null);
                        aiAnalysis.setValue(text);
                    } else {
                        handleFailure(s(R.string.msg_ai_response_empty), request);
                    }
                    return;
                }
                int code = response.code();
                if      (code == 429)                              scheduleRetry(request, parseRetryDelay(response), "quota");
                else if (code == 503 || code == 502 || code == 504) scheduleRetry(request, 10_000L, "server");
                else if (code == 403) { aiLoading.setValue(false); aiError.setValue(s(R.string.msg_invalid_api_key_with_link)); }
                else                  { aiLoading.setValue(false); aiError.setValue(s(R.string.msg_deepseek_failed, code)); }
            }
            @Override public void onFailure(Call<GeminiResponse> call, Throwable t) {
                if (!call.isCanceled())
                    handleFailure(s(R.string.msg_connection_failed, String.valueOf(t.getMessage())), request);
            }
        });
    }

    private void scheduleRetry(GeminiRequest request, long delayMs, String reason) {
        retryCount++;
        if (retryCount > MAX_RETRIES) {
            aiLoading.setValue(false);
            aiRetryStatus.setValue(null);
            aiError.setValue("server".equals(reason)
                    ? s(R.string.msg_server_overload_minutes, MAX_RETRIES)
                    : s(R.string.msg_quota_exceeded_minutes, MAX_RETRIES));
            return;
        }
        showCountdown((delayMs + 999) / 1000, request, reason);
    }

    private void showCountdown(long secondsLeft, GeminiRequest request, String reason) {
        if (secondsLeft <= 0) { aiRetryStatus.setValue(null); callDeepSeek(request); return; }
        String label = "server".equals(reason) ? s(R.string.label_server_overload) : s(R.string.label_quota_full);
        aiRetryStatus.setValue(s(R.string.msg_retry_countdown, label, retryCount, MAX_RETRIES, secondsLeft));
        mainHandler.postDelayed(() -> showCountdown(secondsLeft - 1, request, reason), 1000);
    }

    private void handleFailure(String msg, GeminiRequest request) {
        if (retryCount < 1) {
            retryCount++;
            aiRetryStatus.setValue(s(R.string.msg_connection_retry));
            mainHandler.postDelayed(() -> callDeepSeek(request), 3000);
        } else {
            aiLoading.setValue(false);
            aiRetryStatus.setValue(null);
            aiError.setValue(msg);
        }
    }

    private long parseRetryDelay(Response<GeminiResponse> response) {
        try {
            okhttp3.ResponseBody errorBody = response.errorBody();
            if (errorBody == null) return BASE_DELAY_MS;
            String json = errorBody.string();
            JSONObject root    = new JSONObject(json);
            JSONObject error   = root.getJSONObject("error");
            org.json.JSONArray details = error.getJSONArray("details");
            for (int i = 0; i < details.length(); i++) {
                JSONObject detail = details.getJSONObject(i);
                if (detail.has("retryDelay")) {
                    String digits = detail.getString("retryDelay").replaceAll("[^0-9]", "");
                    if (!digits.isEmpty()) return (Long.parseLong(digits) + 2) * 1000L;
                }
            }
        } catch (Exception ignored) {}
        return BASE_DELAY_MS;
    }

    // ── RAWG text parser ──────────────────────────────────────────────────────

    private SteamRequirement.RequirementSpec parseRawg(String raw) {
        return new SteamRequirement.RequirementSpec(
                extract(raw, "OS", "Operating System"),
                extract(raw, "Processor", "CPU"),
                extract(raw, "Memory", "RAM"),
                extract(raw, "Graphics", "GPU", "Video Card"),
                extract(raw, "DirectX"),
                extract(raw, "Storage", "Hard Drive", "Disk Space")
        );
    }

    private String extract(String text, String... keys) {
        if (text == null) return "N/A";
        String lower = text.toLowerCase();
        for (String kw : keys) {
            int idx = lower.indexOf(kw.toLowerCase());
            if (idx >= 0) {
                int colon = text.indexOf(':', idx);
                if (colon >= 0 && colon < text.length() - 1) {
                    int end = text.indexOf('\n', colon);
                    if (end < 0) end = Math.min(colon + 120, text.length());
                    String val = text.substring(colon + 1, end).trim()
                            .replaceAll("<[^>]+>", "").trim();
                    if (!val.isEmpty()) return val;
                }
            }
        }
        return "N/A";
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mainHandler.removeCallbacksAndMessages(null);
        if (activeDetail1Call      != null) activeDetail1Call.cancel();
        if (activeDetail2Call      != null) activeDetail2Call.cancel();
        if (activeSteamSearch1Call != null) activeSteamSearch1Call.cancel();
        if (activeSteamSearch2Call != null) activeSteamSearch2Call.cancel();
        if (activeSteamReq1Call    != null) activeSteamReq1Call.cancel();
        if (activeSteamReq2Call    != null) activeSteamReq2Call.cancel();
        if (activeAiCall           != null) activeAiCall.cancel();
    }
}
