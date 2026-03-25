package com.groupe.cinetrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.groupe.cinetrack.databinding.ActivityMovieListBinding;
import com.groupe.cinetrack.model.Movie;
import com.groupe.cinetrack.repository.MovieRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * MovieListActivity : affiche la liste de tous les films.
 *
 * On conserve la ListView demandée par le projet, mais avec un adapter
 * personnalisé (MovieAdapter) pour un rendu plus moderne et plus lisible.
 */
public class MovieListActivity extends AppCompatActivity {

    private ActivityMovieListBinding binding;
    private MovieRepository repository;
    private List<Movie> listFilms;
    private MovieAdapter adapter;

    private final ActivityResultLauncher<Intent> lanceurAjoutModif =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            rechargerListe();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMovieListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new MovieRepository(this);
        listFilms = new ArrayList<>();
        adapter = new MovieAdapter(this, listFilms, this::confirmerSuppression);
        binding.listViewFilms.setAdapter(adapter);

        binding.listViewFilms.setOnItemClickListener((parent, view, position, id) -> {
            Movie filmSelectionne = listFilms.get(position);
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(Constants.INTENT_FILM, filmSelectionne);
            startActivity(intent);
        });

        binding.listViewFilms.setOnItemLongClickListener((parent, view, position, id) -> {
            afficherDialogOptions(position);
            return true;
        });

        binding.btnAjouterFilm.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditMovieActivity.class);
            lanceurAjoutModif.launch(intent);
        });

        rechargerListe();
    }

    private void rechargerListe() {
        listFilms.clear();
        listFilms.addAll(repository.getTousLesFilms());
        adapter.notifyDataSetChanged();

        boolean listeVide = listFilms.isEmpty();
        binding.tvEmptyState.setVisibility(listeVide ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.listViewFilms.setVisibility(listeVide ? android.view.View.GONE : android.view.View.VISIBLE);
    }

    private void afficherDialogOptions(final int position) {
        final Movie film = listFilms.get(position);

        new AlertDialog.Builder(this)
                .setTitle(film.getTitre())
                .setItems(
                        new String[]{getString(R.string.action_modifier), getString(R.string.action_supprimer)},
                        (dialog, which) -> {
                            if (which == 0) {
                                ouvrirModification(film);
                            } else {
                                confirmerSuppression(film);
                            }
                        })
                .show();
    }

    private void ouvrirModification(Movie film) {
        Intent intent = new Intent(this, AddEditMovieActivity.class);
        intent.putExtra(Constants.INTENT_FILM, film);
        intent.putExtra(Constants.INTENT_MODE_EDIT, true);
        lanceurAjoutModif.launch(intent);
    }

    private void confirmerSuppression(final Movie film) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_suppr_titre)
                .setMessage(getString(R.string.dialog_suppr_message, film.getTitre()) + "\n\n" + getString(R.string.dialog_suppr_message_detail))
                .setPositiveButton(R.string.btn_oui, (d, w) -> {
                    repository.supprimerFilm(film.getId());
                    rechargerListe();
                    boolean listeVide = listFilms.isEmpty();
                    binding.tvEmptyState.setVisibility(listeVide ? android.view.View.VISIBLE : android.view.View.GONE);
                    binding.listViewFilms.setVisibility(listeVide ? android.view.View.GONE : android.view.View.VISIBLE);
                    Toast.makeText(this, R.string.film_supprime, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.btn_non, null)
                .show();
    }
}
