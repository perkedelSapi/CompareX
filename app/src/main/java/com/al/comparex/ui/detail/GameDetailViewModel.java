package com.al.comparex.ui.detail;

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
public class GameDetailViewModel extends AndroidViewModel {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final int  MAX_RETRIES     = 3;
    private static final long BASE_DELAY_MS   = 5000L;
    private static final long STEAM_TIMEOUT_MS = 15_000L;

    private int retryCount = 0;

    private final MutableLiveData<GameRepository.Resource<GameDetail>>       gameDetail   = new MutableLiveData<>();
    private final MutableLiveData<GameRepository.Resource<SteamRequirement>> steamReq     = new MutableLiveData<>();
    private final MutableLiveData<String>  aiAnalysis    = new MutableLiveData<>();
    private final MutableLiveData<Boolean> aiHasAnchorData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> aiLoading     = new MutableLiveData<>(false);
    private final MutableLiveData<String>  aiError       = new MutableLiveData<>();
    private final MutableLiveData<String>  aiRetryStatus = new MutableLiveData<>();

    private Call<GameDetail>          activeDetailCall;
    private Call<SteamSearchResponse> activeSteamSearchCall;
    private Call<SteamRequirement>    activeSteamReqCall;
    private Call<GeminiResponse>      activeAiCall;

    private Runnable steamTimeoutRunnable;
    private int loadedGameId = -1;

    public GameDetailViewModel(@NonNull Application application) {
        super(application);
    }

    private String s(int resId)                      { return getApplication().getString(resId); }
    private String s(int resId, Object... formatArgs) { return getApplication().getString(resId, formatArgs); }
    private boolean isEnglish()                       { return LangPrefs.isEnglish(getApplication()); }

    public LiveData<GameRepository.Resource<GameDetail>>       getGameDetail()    { return gameDetail;    }
    public LiveData<GameRepository.Resource<SteamRequirement>> getSteamReq()      { return steamReq;      }
    public LiveData<String>                                    getAiAnalysis()      { return aiAnalysis;      }
    public LiveData<Boolean>                                   getAiHasAnchorData() { return aiHasAnchorData; }
    public LiveData<Boolean>                                   getAiLoading()     { return aiLoading;     }
    public LiveData<String>                                    getAiError()       { return aiError;       }
    public LiveData<String>                                    getAiRetryStatus() { return aiRetryStatus; }

    // ── Load detail ───────────────────────────────────────────────────────────

    public void loadDetail(int gameId) {
        GameRepository.Resource<GameDetail> current = gameDetail.getValue();
        if (loadedGameId == gameId && current != null && current.isSuccess()) return;

        loadedGameId = gameId;

        cancelSteamTimeout();
        if (activeDetailCall != null) activeDetailCall.cancel();
        if (activeSteamSearchCall != null) activeSteamSearchCall.cancel();
        if (activeSteamReqCall != null) activeSteamReqCall.cancel();

        gameDetail.setValue(GameRepository.Resource.loading(null));
        steamReq.setValue(GameRepository.Resource.loading(null));

        activeDetailCall = ApiClient.getRawgApi().getGameDetail(gameId, ApiClient.RAWG_API_KEY);
        activeDetailCall.enqueue(new Callback<GameDetail>() {
            @Override public void onResponse(Call<GameDetail> c, Response<GameDetail> r) {
                if (r.isSuccessful() && r.body() != null) {
                    GameDetail gd = r.body();
                    gameDetail.setValue(GameRepository.Resource.success(gd));
                    String steamId = (gd.getSteamAppId() != null && gd.getSteamAppId() > 0)
                            ? String.valueOf(gd.getSteamAppId()) : null;
                    fetchSteamRequirements(steamId, gd.getName());
                } else {
                    gameDetail.setValue(GameRepository.Resource.error(
                            s(R.string.msg_detail_load_failed, r.code()), null));
                    steamReq.setValue(GameRepository.Resource.error(s(R.string.msg_req_load_failed), null));
                }
            }
            @Override public void onFailure(Call<GameDetail> c, Throwable t) {
                if (!c.isCanceled()) {
                    gameDetail.setValue(GameRepository.Resource.error(
                            s(R.string.msg_connection_failed, String.valueOf(t.getMessage())), null));
                    steamReq.setValue(GameRepository.Resource.error(s(R.string.msg_connection_failed_simple), null));
                }
            }
        });
    }

    // ── Steam requirements resolution chain ───────────────────────────────────

    private void fetchSteamRequirements(String steamAppId, String gameName) {
        steamReq.setValue(GameRepository.Resource.loading(null));
        scheduleSteamTimeout(gameName);

        if (steamAppId != null && !steamAppId.isEmpty() && !steamAppId.equals("0")) {
            fetchSteamByAppId(steamAppId, gameName);
        } else {
            searchSteamStore(gameName);
        }
    }

    private void scheduleSteamTimeout(String gameName) {
        cancelSteamTimeout();
        steamTimeoutRunnable = () -> {
            if (activeSteamSearchCall != null) activeSteamSearchCall.cancel();
            if (activeSteamReqCall != null) activeSteamReqCall.cancel();
            tryRawgFallbackOrUnavailable();
        };
        mainHandler.postDelayed(steamTimeoutRunnable, STEAM_TIMEOUT_MS);
    }

    private void cancelSteamTimeout() {
        if (steamTimeoutRunnable != null) {
            mainHandler.removeCallbacks(steamTimeoutRunnable);
            steamTimeoutRunnable = null;
        }
    }

    private void searchSteamStore(String gameName) {
        if (activeSteamSearchCall != null) activeSteamSearchCall.cancel();
        activeSteamSearchCall = ApiClient.getSteamStoreApi()
                .searchGame(gameName, "english", "US");
        activeSteamSearchCall.enqueue(new Callback<SteamSearchResponse>() {
            @Override public void onResponse(Call<SteamSearchResponse> c, Response<SteamSearchResponse> r) {
                if (r.isSuccessful() && r.body() != null
                        && r.body().getItems() != null
                        && !r.body().getItems().isEmpty()) {
                    fetchSteamByAppId(String.valueOf(r.body().getItems().get(0).getId()), gameName);
                } else {
                    cancelSteamTimeout();
                    tryRawgFallbackOrUnavailable();
                }
            }
            @Override public void onFailure(Call<SteamSearchResponse> c, Throwable t) {
                if (!c.isCanceled()) {
                    cancelSteamTimeout();
                    tryRawgFallbackOrUnavailable();
                }
            }
        });
    }

    private void fetchSteamByAppId(String appId, String gameName) {
        if (activeSteamReqCall != null) activeSteamReqCall.cancel();
        activeSteamReqCall = ApiClient.getSteamApi().getRequirements(appId);
        activeSteamReqCall.enqueue(new Callback<SteamRequirement>() {
            @Override public void onResponse(Call<SteamRequirement> c, Response<SteamRequirement> r) {
                cancelSteamTimeout();
                if (r.isSuccessful() && r.body() != null && r.body().isSuccess()
                        && r.body().getData() != null
                        && r.body().getData().getSystemRequirements() != null
                        && hasAnySpec(r.body().getData().getSystemRequirements())) {
                    steamReq.setValue(GameRepository.Resource.success(r.body()));
                } else {
                    tryRawgFallbackOrUnavailable();
                }
            }
            @Override public void onFailure(Call<SteamRequirement> c, Throwable t) {
                if (!c.isCanceled()) {
                    cancelSteamTimeout();
                    tryRawgFallbackOrUnavailable();
                }
            }
        });
    }

    private boolean hasAnySpec(SteamRequirement.SystemRequirements req) {
        if (req == null) return false;
        SteamRequirement.RequirementSpec min = req.getMinimum();
        SteamRequirement.RequirementSpec rec = req.getRecommended();
        boolean minOk = min != null && (
                isValidField(min.getOs()) || isValidField(min.getProcessor())
                || isValidField(min.getGraphics()) || isValidField(min.getMemory()));
        boolean recOk = rec != null && (
                isValidField(rec.getOs()) || isValidField(rec.getProcessor())
                || isValidField(rec.getGraphics()) || isValidField(rec.getMemory()));
        return minOk || recOk;
    }

    private boolean isValidField(String val) {
        return val != null && !val.isEmpty() && !"N/A".equals(val);
    }

    private void tryRawgFallbackOrUnavailable() {
        GameRepository.Resource<GameDetail> gdRes = gameDetail.getValue();
        if (gdRes != null && gdRes.getData() != null) {
            GameDetail gd = gdRes.getData();
            if (gd.getRawgMinRequirements() != null || gd.getRawgRecRequirements() != null) {
                steamReq.setValue(GameRepository.Resource.error("rawg_fallback", null));
                return;
            }
        }
        steamReq.setValue(GameRepository.Resource.error(s(R.string.msg_req_not_available), null));
    }

    // ── Spec resolvers ────────────────────────────────────────────────────────

    public SteamRequirement.RequirementSpec resolveMinSpec() {
        GameRepository.Resource<SteamRequirement> sr = steamReq.getValue();
        if (sr != null && sr.isSuccess() && sr.getData() != null
                && sr.getData().getData() != null
                && sr.getData().getData().getSystemRequirements() != null) {
            SteamRequirement.RequirementSpec min =
                    sr.getData().getData().getSystemRequirements().getMinimum();
            if (min != null) return min;
        }
        GameRepository.Resource<GameDetail> gd = gameDetail.getValue();
        if (gd != null && gd.getData() != null) {
            String rawMin = gd.getData().getRawgMinRequirements();
            if (rawMin != null && !rawMin.isEmpty()) return parseRawgText(rawMin);
        }
        return null;
    }

    public SteamRequirement.RequirementSpec resolveRecSpec() {
        GameRepository.Resource<SteamRequirement> sr = steamReq.getValue();
        if (sr != null && sr.isSuccess() && sr.getData() != null
                && sr.getData().getData() != null
                && sr.getData().getData().getSystemRequirements() != null) {
            SteamRequirement.RequirementSpec rec =
                    sr.getData().getData().getSystemRequirements().getRecommended();
            if (rec != null) return rec;
        }
        GameRepository.Resource<GameDetail> gd = gameDetail.getValue();
        if (gd != null && gd.getData() != null) {
            String rawRec = gd.getData().getRawgRecRequirements();
            if (rawRec != null && !rawRec.isEmpty()) return parseRawgText(rawRec);
        }
        return null;
    }

    // ── AI Analysis ───────────────────────────────────────────────────────────

    public void requestAiAnalysis(SpekUser user) {
        GameRepository.Resource<GameDetail> gdRes = gameDetail.getValue();
        if (gdRes == null || !gdRes.isSuccess() || gdRes.getData() == null || user == null) {
            aiError.setValue(s(R.string.msg_data_not_ready));
            return;
        }
        retryCount = 0;
        aiError.setValue(null);
        aiRetryStatus.setValue(null);
        aiAnalysis.setValue(null);

        boolean isEnglish = isEnglish();
        boolean hasAnchor = AiAnalysisBuilder.hasAnchorData(user.getGpuName(), gdRes.getData().getName());
        aiHasAnchorData.setValue(hasAnchor);

        String prompt = AiAnalysisBuilder.buildSinglePrompt(
                user, gdRes.getData(), resolveMinSpec(), resolveRecSpec(), isEnglish);
        callDeepSeek(new GeminiRequest(AiAnalysisBuilder.getSystemPrompt(isEnglish), prompt));
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
                        handleAiFailure(s(R.string.msg_deepseek_error, body.getError().getMessage()), request);
                        return;
                    }
                    String text = body.getFirstText();
                    if (text != null && !text.isEmpty()) {
                        aiLoading.setValue(false);
                        aiRetryStatus.setValue(null);
                        aiAnalysis.setValue(text);
                    } else {
                        handleAiFailure(s(R.string.msg_ai_response_empty), request);
                    }
                    return;
                }
                int code = response.code();
                if      (code == 429)                              scheduleAiRetry(request, parseRetryDelay(response), "quota");
                else if (code == 503 || code == 502 || code == 504) scheduleAiRetry(request, 10_000L, "server");
                else if (code == 403) { aiLoading.setValue(false); aiError.setValue(s(R.string.msg_invalid_api_key)); }
                else                  { aiLoading.setValue(false); aiError.setValue(s(R.string.msg_deepseek_failed, code)); }
            }
            @Override public void onFailure(Call<GeminiResponse> call, Throwable t) {
                if (!call.isCanceled())
                    handleAiFailure(s(R.string.msg_connection_failed, String.valueOf(t.getMessage())), request);
            }
        });
    }

    private void scheduleAiRetry(GeminiRequest request, long delayMs, String reason) {
        retryCount++;
        if (retryCount > MAX_RETRIES) {
            aiLoading.setValue(false);
            aiRetryStatus.setValue(null);
            aiError.setValue("server".equals(reason)
                    ? s(R.string.msg_server_overload, MAX_RETRIES)
                    : s(R.string.msg_quota_exceeded, MAX_RETRIES));
            return;
        }
        showAiCountdown((delayMs + 999) / 1000, request, reason);
    }

    private void showAiCountdown(long secondsLeft, GeminiRequest request, String reason) {
        if (secondsLeft <= 0) { aiRetryStatus.setValue(null); callDeepSeek(request); return; }
        String label = "server".equals(reason) ? s(R.string.label_server_overload) : s(R.string.label_quota_full);
        aiRetryStatus.setValue(s(R.string.msg_retry_countdown, label, retryCount, MAX_RETRIES, secondsLeft));
        mainHandler.postDelayed(() -> showAiCountdown(secondsLeft - 1, request, reason), 1000);
    }

    private void handleAiFailure(String msg, GeminiRequest request) {
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

    private SteamRequirement.RequirementSpec parseRawgText(String raw) {
        return new SteamRequirement.RequirementSpec(
                extractField(raw, "OS", "Operating System"),
                extractField(raw, "Processor", "CPU"),
                extractField(raw, "Memory", "RAM"),
                extractField(raw, "Graphics", "GPU", "Video Card"),
                extractField(raw, "DirectX"),
                extractField(raw, "Storage", "Hard Drive", "Disk Space")
        );
    }

    private String extractField(String text, String... keywords) {
        if (text == null) return "N/A";
        String lower = text.toLowerCase();
        for (String kw : keywords) {
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
        cancelSteamTimeout();
        if (activeDetailCall      != null) activeDetailCall.cancel();
        if (activeSteamSearchCall != null) activeSteamSearchCall.cancel();
        if (activeSteamReqCall    != null) activeSteamReqCall.cancel();
        if (activeAiCall          != null) activeAiCall.cancel();
    }
}
