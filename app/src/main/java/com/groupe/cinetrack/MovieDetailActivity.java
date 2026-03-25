package com.groupe.cinetrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.groupe.cinetrack.databinding.ActivityMovieDetailBinding;
import com.groupe.cinetrack.model.Movie;
import com.groupe.cinetrack.network.OmdbResponse;
import com.groupe.cinetrack.repository.MovieRepository;
import com.groupe.cinetrack.utils.MovieUtils;

public class MovieDetailActivity extends AppCompatActivity {

    private ActivityMovieDetailBinding binding;
    private MovieRepository repository;
    private Movie film;
    private boolean modeRecommandation;
    private String recoTitre;
    private String recoAnnee;
    private String recoGenre;
    private String recoPlot;
    private String recoPoster;

    private final ActivityResultLauncher<Intent> lanceurModif =
            registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        finish();
                    }
                }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMovieDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new MovieRepository(this);
        modeRecommandation = getIntent().getBooleanExtra(Constants.INTENT_MODE_RECOMMANDATION, false);

        if (modeRecommandation) {
            recoTitre = getIntent().getStringExtra(Constants.INTENT_RECO_TITRE);
            recoAnnee = getIntent().getStringExtra(Constants.INTENT_RECO_ANNEE);
            recoGenre = getIntent().getStringExtra(Constants.INTENT_RECO_GENRE);
            recoPlot = getIntent().getStringExtra(Constants.INTENT_RECO_PLOT);
            recoPoster = getIntent().getStringExtra(Constants.INTENT_RECO_POSTER);

            if (recoTitre == null || recoTitre.trim().isEmpty()) {
                finish();
                return;
            }

            afficherDonneesRecommendation();
            configurerActionsRecommendation();
            chargerDonneesOmdbPourRecommendation();
        } else {
            film = (Movie) getIntent().getSerializableExtra(Constants.INTENT_FILM);
            if (film == null) {
                finish();
                return;
            }

            afficherDonneesLocales();
            binding.btnModifierFilm.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddEditMovieActivity.class);
                intent.putExtra(Constants.INTENT_FILM, film);
                intent.putExtra(Constants.INTENT_MODE_EDIT, true);
                lanceurModif.launch(intent);
            });
            chargerDonneesOmdb();
        }
    }

    private void afficherDonneesLocales() {
        setTitle(film.getTitre());
        binding.tvDetailTitre.setText(film.getTitre());
        binding.tvDetailGenre.setText(getString(R.string.detail_genre, film.getGenre()));
        binding.tvDetailStatut.setText(getString(R.string.detail_statut, film.getStatut()));
        binding.tvDetailNote.setText(getString(R.string.detail_note, MovieUtils.formaterNote(film.getNote())));
        binding.btnModifierFilm.setVisibility(View.VISIBLE);
        binding.btnAjouterDepuisReco.setVisibility(View.GONE);
    }

    private void afficherDonneesRecommendation() {
        setTitle(recoTitre);
        binding.tvDetailTitre.setText(recoTitre);
        binding.tvDetailGenre.setText(getString(R.string.detail_genre, valeurOuDefaut(recoGenre, getString(R.string.info_non_disponible))));
        binding.tvDetailStatut.setText(getString(R.string.detail_annee, valeurOuDefaut(recoAnnee, getString(R.string.info_non_disponible))));
        binding.tvDetailNote.setText(R.string.detail_recommandation);
        binding.tvSynopsis.setText(valeurOuDefaut(recoPlot, getString(R.string.synopsis_indisponible)));
        binding.btnModifierFilm.setVisibility(View.GONE);
        binding.btnAjouterDepuisReco.setVisibility(View.VISIBLE);
        mettreAJourEtatBoutonAjout();
        afficherPosterSiDisponible(recoPoster, getString(R.string.pas_de_poster));
    }

    private void configurerActionsRecommendation() {
        binding.btnAjouterDepuisReco.setOnClickListener(v -> {
            if (repository.filmExisteDeja(recoTitre)) {
                Toast.makeText(this, R.string.film_deja_dans_liste, Toast.LENGTH_SHORT).show();
                mettreAJourEtatBoutonAjout();
                return;
            }

            Movie nouveau = new Movie();
            nouveau.setTitre(recoTitre);
            nouveau.setGenre(valeurOuDefaut(recoGenre, getString(R.string.genre_par_defaut)));
            nouveau.setStatut(Constants.STATUT_A_VOIR);
            nouveau.setNote(0);
            repository.ajouterFilm(nouveau);
            Toast.makeText(this, R.string.film_ajoute_depuis_reco, Toast.LENGTH_SHORT).show();
            mettreAJourEtatBoutonAjout();
        });
    }

    private void mettreAJourEtatBoutonAjout() {
        boolean dejaDansListe = repository.filmExisteDeja(recoTitre);
        binding.btnAjouterDepuisReco.setEnabled(!dejaDansListe);
        binding.btnAjouterDepuisReco.setText(dejaDansListe
                ? getString(R.string.btn_film_deja_ajoute)
                : getString(R.string.btn_ajouter_a_ma_liste));
    }

    private void chargerDonneesOmdb() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.imageViewPoster.setVisibility(View.GONE);
        binding.noPosterContainer.setVisibility(View.GONE);
        binding.tvNoPoster.setVisibility(View.GONE);
        binding.tvSynopsis.setText("");

        repository.getPosterPourFilm(film.getTitre(), new MovieRepository.OmdbCallback() {
            @Override
            public void onSuccess(OmdbResponse response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvSynopsis.setText(response.getPlot());
                afficherPosterSiDisponible(response.getPoster(), getString(R.string.pas_de_poster));
            }

            @Override
            public void onEchec(String messageErreur) {
                binding.progressBar.setVisibility(View.GONE);
                binding.imageViewPoster.setVisibility(View.GONE);
                binding.noPosterContainer.setVisibility(View.VISIBLE);
                binding.tvNoPoster.setVisibility(View.VISIBLE);
                binding.tvNoPoster.setText(messageErreur);
            }
        });
    }

    private void chargerDonneesOmdbPourRecommendation() {
        binding.progressBar.setVisibility(View.VISIBLE);
        repository.getPosterPourFilm(recoTitre, new MovieRepository.OmdbCallback() {
            @Override
            public void onSuccess(OmdbResponse response) {
                binding.progressBar.setVisibility(View.GONE);
                recoGenre = valeurOuDefaut(response.getGenre(), recoGenre);
                recoAnnee = valeurOuDefaut(response.getYear(), recoAnnee);
                recoPlot = valeurOuDefaut(response.getPlot(), recoPlot);
                recoPoster = valeurOuDefaut(response.getPoster(), recoPoster);

                binding.tvDetailGenre.setText(getString(R.string.detail_genre, valeurOuDefaut(recoGenre, getString(R.string.info_non_disponible))));
                binding.tvDetailStatut.setText(getString(R.string.detail_annee, valeurOuDefaut(recoAnnee, getString(R.string.info_non_disponible))));
                binding.tvSynopsis.setText(valeurOuDefaut(recoPlot, getString(R.string.synopsis_indisponible)));
                afficherPosterSiDisponible(recoPoster, getString(R.string.pas_de_poster));
            }

            @Override
            public void onEchec(String messageErreur) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvSynopsis.setText(valeurOuDefaut(recoPlot, getString(R.string.synopsis_indisponible)));
                afficherPosterSiDisponible(recoPoster, messageErreur);
            }
        });
    }

    private void afficherPosterSiDisponible(String posterUrl, String messageSiAbsent) {
        String urlPoster = posterUrl;
        if (urlPoster != null) {
            urlPoster = urlPoster.replace("http://", "https://");
        }
        if (urlPoster == null || urlPoster.equals("N/A") || urlPoster.trim().isEmpty()) {
            binding.imageViewPoster.setVisibility(View.GONE);
            binding.noPosterContainer.setVisibility(View.VISIBLE);
            binding.tvNoPoster.setVisibility(View.VISIBLE);
            binding.tvNoPoster.setText(messageSiAbsent);
        } else {
            binding.imageViewPoster.setVisibility(View.VISIBLE);
            binding.noPosterContainer.setVisibility(View.GONE);
            binding.tvNoPoster.setVisibility(View.GONE);
            Glide.with(this)
                    .load(urlPoster)
                    .placeholder(R.drawable.ic_film_placeholder)
                    .error(R.drawable.ic_film_placeholder)
                    .into(binding.imageViewPoster);
        }
    }

    private String valeurOuDefaut(String valeur, String valeurDefaut) {
        return (valeur == null || valeur.trim().isEmpty() || "N/A".equalsIgnoreCase(valeur)) ? valeurDefaut : valeur;
    }
}
