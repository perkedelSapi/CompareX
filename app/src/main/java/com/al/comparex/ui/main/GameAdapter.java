package com.al.comparex.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.al.comparex.R;
import com.al.comparex.data.model.Game;
import com.al.comparex.databinding.ItemGameBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    public interface OnGameClickListener {
        void onCardClick(Game game);
        boolean onCheckboxToggle(Game game);
    }

    private List<Game> games = new ArrayList<>();
    private final OnGameClickListener listener;

    public GameAdapter(OnGameClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Game> newList) {
        games = newList != null ? new ArrayList<>(newList) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<Game> getCurrentList() { return games; }

    @NonNull @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGameBinding binding = ItemGameBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new GameViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        holder.bind(games.get(position));
    }

    @Override public int getItemCount() { return games.size(); }

    class GameViewHolder extends RecyclerView.ViewHolder {
        private final ItemGameBinding b;

        GameViewHolder(ItemGameBinding binding) {
            super(binding.getRoot());
            b = binding;
        }

        void bind(Game game) {
            b.tvGameName.setText(game.getName());
            b.tvReleaseYear.setText(formatYear(game.getReleased()));

            // Metacritic badge
            if (game.getMetacritic() != null && game.getMetacritic() > 0) {
                b.tvMetacritic.setVisibility(View.VISIBLE);
                b.tvMetacritic.setText(String.valueOf(game.getMetacritic()));
                int color;
                if (game.getMetacritic() >= 75)      color = R.color.metacritic_green;
                else if (game.getMetacritic() >= 50) color = R.color.metacritic_yellow;
                else                                  color = R.color.metacritic_red;
                b.tvMetacritic.setBackgroundTintList(
                        ContextCompat.getColorStateList(b.getRoot().getContext(), color));
            } else {
                b.tvMetacritic.setVisibility(View.GONE);
            }

            // Platform chips — maks 2 platform
            bindPlatformChips(game, b.getRoot().getContext());

            // Cover image
            Glide.with(b.getRoot().getContext())
                    .load(game.getBackgroundImage())
                    .placeholder(R.drawable.ic_game_placeholder)
                    .error(R.drawable.ic_game_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(b.imgGame);

            // Checkbox
            b.checkboxSelect.setOnCheckedChangeListener(null);
            b.checkboxSelect.setChecked(game.isSelected());

            b.cardGame.setCardBackgroundColor(
                    ContextCompat.getColor(b.getRoot().getContext(),
                            game.isSelected() ? R.color.card_selected : R.color.card_normal));

            b.cardGame.setOnClickListener(v -> {
                if (listener != null) listener.onCardClick(game);
            });

            b.checkboxSelect.setOnCheckedChangeListener((btn, isChecked) -> {
                if (listener != null) {
                    boolean allowed = listener.onCheckboxToggle(game);
                    if (!allowed) {
                        btn.setOnCheckedChangeListener(null);
                        btn.setChecked(false);
                        game.setSelected(false);
                    }
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_ID) notifyItemChanged(pos);
                }
            });
        }

        private void bindPlatformChips(Game game, Context ctx) {
            b.layoutGenres.removeAllViews();
            String platformNames = game.getPlatformNames();
            if (platformNames == null || platformNames.isEmpty() || platformNames.equals("N/A")) return;

            String[] genres = platformNames.split(",");
            int max = Math.min(genres.length, 2);
            for (int i = 0; i < max; i++) {
                String genre = genres[i].trim();
                if (genre.isEmpty()) continue;

                TextView chip = new TextView(ctx);
                chip.setText(genre);
                chip.setTextSize(10f);
                chip.setTextColor(ContextCompat.getColor(ctx, R.color.accent));
                chip.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bg_genre_chip));
                chip.setPadding(dp(ctx, 8), dp(ctx, 3), dp(ctx, 8), dp(ctx, 3));

                ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                if (i > 0) lp.leftMargin = dp(ctx, 6);
                chip.setLayoutParams(lp);

                b.layoutGenres.addView(chip);
            }
        }

        private int dp(Context ctx, int dp) {
            return (int) (dp * ctx.getResources().getDisplayMetrics().density);
        }

        private String formatYear(String released) {
            if (released == null || released.isEmpty()) return "TBA";
            if (released.length() >= 4) return released.substring(0, 4);
            return released;
        }
    }
}
