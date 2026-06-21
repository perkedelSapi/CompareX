package com.al.comparex.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fuzzy-search adapter untuk dropdown CPU/GPU.
 *
 * Fitur:
 *  - Section headers (baris yang diawali "━━━") ditampilkan dengan style berbeda
 *    dan tidak bisa dipilih
 *  - Fuzzy search: semua token dalam query harus ada (AND logic)
 *  - Saat search aktif, section headers disembunyikan agar tidak berantakan
 */
public class FuzzySearchAdapter extends ArrayAdapter<String> {

    private static final String HEADER_PREFIX = "━━━";

    private final List<String> allItems;
    private List<String> filteredItems;
    private final Filter filter = new FuzzyFilter();

    public FuzzySearchAdapter(Context ctx, List<String> items) {
        super(ctx, android.R.layout.simple_dropdown_item_1line, items);
        this.allItems      = new ArrayList<>(items);
        this.filteredItems = new ArrayList<>(items);
    }

    @Override public int getCount()          { return filteredItems.size(); }
    @Override public String getItem(int pos) { return filteredItems.get(pos); }

    /** Section headers are not enabled (not selectable). */
    @Override
    public boolean isEnabled(int position) {
        String item = getItem(position);
        return item != null && !item.startsWith(HEADER_PREFIX);
    }

    @Override
    public boolean areAllItemsEnabled() { return false; }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String item = getItem(position);
        boolean isHeader = item != null && item.startsWith(HEADER_PREFIX);

        // Inflate different layout for header vs normal item
        int layoutRes = isHeader
                ? android.R.layout.simple_list_item_1      // will be re-styled below
                : android.R.layout.simple_dropdown_item_1line;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(layoutRes, parent, false);
        }

        TextView tv = convertView.findViewById(android.R.id.text1);
        if (tv != null) {
            tv.setText(item);
            if (isHeader) {
                // Header style: accent color, smaller, non-clickable feel
                tv.setTextColor(0xFF58A6FF);   // accent blue
                tv.setTextSize(11f);
                tv.setAlpha(0.85f);
                tv.setPadding(24, 20, 16, 6);
                tv.setBackgroundColor(0xFF1C2128); // slightly different bg
            } else {
                tv.setTextColor(0xFFE6EDF3);
                tv.setTextSize(14f);
                tv.setAlpha(1.0f);
                tv.setPadding(32, 14, 16, 14);
                tv.setBackgroundColor(0x00000000);
            }
        }
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() { return filter; }

    // ─────────────────────────────────────────────────────────────────────────

    private class FuzzyFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                // Show full grouped list
                results.values = new ArrayList<>(allItems);
                results.count  = allItems.size();
                return results;
            }

            // While searching: skip headers, match by tokens
            String[] tokens = constraint.toString().toLowerCase(Locale.ROOT).trim().split("\\s+");
            List<String> matched = new ArrayList<>();

            for (String item : allItems) {
                if (item.startsWith(HEADER_PREFIX)) continue; // hide headers in search results
                String lower = item.toLowerCase(Locale.ROOT);
                boolean allMatch = true;
                for (String token : tokens) {
                    if (!lower.contains(token)) { allMatch = false; break; }
                }
                if (allMatch) matched.add(item);
            }

            results.values = matched;
            results.count  = matched.size();
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredItems = (List<String>) results.values;
            if (filteredItems == null) filteredItems = new ArrayList<>();
            if (results.count > 0) notifyDataSetChanged();
            else                   notifyDataSetInvalidated();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return (String) resultValue;
        }
    }
}
