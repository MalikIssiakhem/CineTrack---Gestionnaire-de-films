package com.groupe.cinetrack;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.groupe.cinetrack.network.OmdbSearchItem;

import java.util.List;

/**
 * Adapter des resultats OMDb dans l'ecran d'ajout.
 * Il affiche une miniature du poster, le titre et l'annee.
 */
public class OmdbSearchAdapter extends ArrayAdapter<OmdbSearchItem> {

    private final LayoutInflater inflater;

    public OmdbSearchAdapter(@NonNull Context context, @NonNull List<OmdbSearchItem> items) {
        super(context, 0, items);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_omdb_search_result, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        OmdbSearchItem item = getItem(position);
        if (item == null) {
            return convertView;
        }

        holder.tvTitle.setText(item.getTitle());
        holder.tvSubtitle.setText(item.getYear());

        String posterUrl = normaliserPoster(item.getPoster());
        if (posterUrl == null) {
            holder.ivPoster.setImageResource(R.drawable.ic_film_placeholder);
        } else {
            Glide.with(getContext())
                    .load(posterUrl)
                    .placeholder(R.drawable.ic_film_placeholder)
                    .error(R.drawable.ic_film_placeholder)
                    .into(holder.ivPoster);
        }

        return convertView;
    }

    private String normaliserPoster(String poster) {
        if (poster == null || poster.trim().isEmpty() || "N/A".equalsIgnoreCase(poster)) {
            return null;
        }
        return poster.replace("http://", "https://");
    }

    private static class ViewHolder {
        final ImageView ivPoster;
        final TextView tvTitle;
        final TextView tvSubtitle;

        ViewHolder(View view) {
            ivPoster = view.findViewById(R.id.ivSearchPoster);
            tvTitle = view.findViewById(R.id.tvSearchTitle);
            tvSubtitle = view.findViewById(R.id.tvSearchSubtitle);
        }
    }
}
