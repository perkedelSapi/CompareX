package com.al.comparex.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.al.comparex.R;
import com.al.comparex.data.model.HistoryEntry;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryAdapter extends ListAdapter<HistoryEntry, HistoryAdapter.VH> {

    public interface OnHistoryClickListener {
        void onClick(HistoryEntry entry);
        void onLongClick(HistoryEntry entry);
    }

    private final OnHistoryClickListener listener;

    public HistoryAdapter(OnHistoryClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        HistoryEntry e = getItem(position);

        // Cover image
        if (e.getBackgroundImage() != null && !e.getBackgroundImage().isEmpty()) {
            Glide.with(h.cover.getContext())
                    .load(e.getBackgroundImage())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_game_placeholder)
                            .error(R.drawable.ic_game_placeholder)
                            .transform(new RoundedCorners(12)))
                    .into(h.cover);
        } else {
            h.cover.setImageResource(R.drawable.ic_game_placeholder);
        }

        h.tvName.setText(e.getGameName());

        // Release year only
        String year = "";
        if (e.getReleased() != null && e.getReleased().length() >= 4)
            year = e.getReleased().substring(0, 4);
        h.tvYear.setText(year);

        // Metacritic badge
        if (e.getMetacritic() != null) {
            h.tvMeta.setVisibility(View.VISIBLE);
            h.tvMeta.setText(String.valueOf(e.getMetacritic()));
            int score = e.getMetacritic();
            int color = score >= 75
                    ? h.itemView.getContext().getColor(R.color.metacritic_green)
                    : score >= 50
                    ? h.itemView.getContext().getColor(R.color.metacritic_yellow)
                    : h.itemView.getContext().getColor(R.color.metacritic_red);
            h.tvMeta.setTextColor(color);
        } else {
            h.tvMeta.setVisibility(View.GONE);
        }

        // Compat setting badge
        String setting = e.getCompatSetting();
        if (setting != null && !setting.isEmpty() && !setting.equals("N/A")) {
            h.tvSetting.setVisibility(View.VISIBLE);
            h.tvSetting.setText(setting);
            // Color by setting tier
            int col;
            switch (setting.toLowerCase()) {
                case "ultra": case "high":
                    col = h.itemView.getContext().getColor(R.color.status_powerful); break;
                case "medium":
                    col = h.itemView.getContext().getColor(R.color.status_enough); break;
                case "low":
                    col = h.itemView.getContext().getColor(R.color.status_weak); break;
                default:
                    col = h.itemView.getContext().getColor(R.color.status_below); break;
            }
            h.tvSetting.setTextColor(col);
        } else {
            h.tvSetting.setVisibility(View.GONE);
        }

        // Relative timestamp
        h.tvTime.setText(relativeTime(e.getCheckedAt()));

        h.itemView.setOnClickListener(v -> listener.onClick(e));
        h.itemView.setOnLongClickListener(v -> { listener.onLongClick(e); return true; });
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class VH extends RecyclerView.ViewHolder {
        final ImageView cover;
        final TextView  tvName, tvYear, tvMeta, tvSetting, tvTime;

        VH(@NonNull View v) {
            super(v);
            cover     = v.findViewById(R.id.iv_cover);
            tvName    = v.findViewById(R.id.tv_name);
            tvYear    = v.findViewById(R.id.tv_year);
            tvMeta    = v.findViewById(R.id.tv_metacritic);
            tvSetting = v.findViewById(R.id.tv_setting);
            tvTime    = v.findViewById(R.id.tv_time);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String relativeTime(long epochMs) {
        long diff = System.currentTimeMillis() - epochMs;
        long minutes = diff / 60_000;
        if (minutes < 1)    return "Baru saja";
        if (minutes < 60)   return minutes + " mnt lalu";
        long hours = minutes / 60;
        if (hours < 24)     return hours + " jam lalu";
        long days = hours / 24;
        if (days < 7)       return days + " hari lalu";
        // Fallback to date
        return new SimpleDateFormat("dd MMM", new Locale("id")).format(new Date(epochMs));
    }

    private static final DiffUtil.ItemCallback<HistoryEntry> DIFF =
            new DiffUtil.ItemCallback<HistoryEntry>() {
        @Override public boolean areItemsTheSame(@NonNull HistoryEntry a, @NonNull HistoryEntry b) {
            return a.getGameId() == b.getGameId();
        }
        @Override public boolean areContentsTheSame(@NonNull HistoryEntry a, @NonNull HistoryEntry b) {
            return a.getCheckedAt() == b.getCheckedAt()
                    && a.getCompatSetting().equals(b.getCompatSetting());
        }
    };
}
