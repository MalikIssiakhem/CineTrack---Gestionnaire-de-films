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

import com.groupe.cinetrack.model.Movie;
import com.groupe.cinetrack.utils.MovieUtils;

import java.util.List;

/**
 * Adapter personnalisé pour afficher les films sous forme de cartes plus propres.
 * On conserve la ListView de la consigne, mais on remplace le rendu texte brut
 * par une ligne personnalisée pour se rapprocher d'une vraie interface mobile.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {

    public interface MovieActionListener {
        void onDeleteRequested(Movie film);
    }

    private final LayoutInflater inflater;
    private final MovieActionListener actionListener;

    public MovieAdapter(@NonNull Context context, @NonNull List<Movie> films, @Nullable MovieActionListener actionListener) {
        super(context, 0, films);
        this.inflater = LayoutInflater.from(context);
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_film, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Movie film = getItem(position);
        if (film == null) return convertView;

        holder.tvTitre.setText(film.getTitre());
        holder.tvGenre.setText(film.getGenre());
        holder.tvNote.setText(MovieUtils.formaterNote(film.getNote()));

        boolean estVu = Constants.STATUT_VU.equals(film.getStatut());
        holder.tvStatut.setText(estVu ? R.string.statut_vu_court : R.string.statut_a_voir_court);
        holder.tvStatut.setSelected(estVu);
        holder.ivChevron.setImageResource(R.drawable.ic_chevron_right);
        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteRequested(film);
            }
        });

        if (estVu) {
            holder.ivStatut.setImageResource(R.drawable.ic_status_seen);
            holder.tvStatut.setBackgroundResource(R.drawable.bg_chip_seen);
        } else {
            holder.ivStatut.setImageResource(R.drawable.ic_status_watchlist);
            holder.tvStatut.setBackgroundResource(R.drawable.bg_chip_watchlist);
        }

        return convertView;
    }

    private static class ViewHolder {
        final TextView tvTitre;
        final TextView tvGenre;
        final TextView tvStatut;
        final TextView tvNote;
        final ImageView ivStatut;
        final ImageView ivChevron;
        final ImageView btnDelete;

        ViewHolder(View view) {
            tvTitre = view.findViewById(R.id.tvItemTitre);
            tvGenre = view.findViewById(R.id.tvItemGenre);
            tvStatut = view.findViewById(R.id.tvItemStatut);
            tvNote = view.findViewById(R.id.tvItemNote);
            ivStatut = view.findViewById(R.id.ivItemStatut);
            ivChevron = view.findViewById(R.id.ivItemChevron);
            btnDelete = view.findViewById(R.id.btnDeleteFilm);
        }
    }
}
