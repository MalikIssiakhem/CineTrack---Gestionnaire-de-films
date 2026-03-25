package com.groupe.cinetrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.card.MaterialCardView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.groupe.cinetrack.databinding.ActivityMainBinding;
import com.groupe.cinetrack.network.OmdbResponse;
import com.groupe.cinetrack.repository.MovieRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MovieRepository repository;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new MovieRepository(this);
        prefs = getSharedPreferences(Constants.SP_NOM, MODE_PRIVATE);

        binding.btnVoirFilms.setOnClickListener(v -> startActivity(new Intent(this, MovieListActivity.class)));
        binding.btnProfil.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        binding.btnRechargerReco.setOnClickListener(v -> chargerRecommandations());
    }

    @Override
    protected void onResume() {
        super.onResume();
        verifierEtAfficherPrenom();
        mettreAJourCompteurs();
        chargerRecommandations();
    }

    private void verifierEtAfficherPrenom() {
        String prenom = prefs.getString(Constants.SP_PRENOM, null);
        if (prenom == null || prenom.isEmpty()) {
            afficherDialogBienvenue();
        } else {
            binding.tvBonjour.setText(getString(R.string.bonjour_prenom, prenom));
        }
    }

    private void afficherDialogBienvenue() {
        final EditText editPrenom = new EditText(this);
        editPrenom.setHint(getString(R.string.hint_prenom));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_bienvenue_titre)
                .setMessage(R.string.dialog_bienvenue_message)
                .setView(editPrenom)
                .setCancelable(false)
                .setPositiveButton(R.string.btn_valider, (d, w) -> {
                    String prenom = editPrenom.getText().toString().trim();
                    prefs.edit().putString(Constants.SP_PRENOM, prenom).apply();
                    binding.tvBonjour.setText(getString(R.string.bonjour_prenom, prenom));
                })
                .create();

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        editPrenom.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(s.toString().trim().length() > 0);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void mettreAJourCompteurs() {
        int nbVus = repository.compterFilmsParStatut(Constants.STATUT_VU);
        int nbAVoir = repository.compterFilmsParStatut(Constants.STATUT_A_VOIR);
        float moyenne = repository.getMoyenneNotes();

        binding.tvNbVus.setText(getString(R.string.compteur_vus, nbVus));
        binding.tvNbAVoir.setText(getString(R.string.compteur_a_voir, nbAVoir));
        binding.tvMoyenne.setText(getString(R.string.compteur_moyenne, String.format("%.1f", moyenne)));
    }

    private void chargerRecommandations() {
        initialiserCarteReco(binding.tvRecoTitre1, binding.tvRecoPlot1, binding.ivReco1, binding.tvRecoFallback1);
        initialiserCarteReco(binding.tvRecoTitre2, binding.tvRecoPlot2, binding.ivReco2, binding.tvRecoFallback2);
        initialiserCarteReco(binding.tvRecoTitre3, binding.tvRecoPlot3, binding.ivReco3, binding.tvRecoFallback3);
        binding.tvHorsLigne.setVisibility(View.GONE);

        chargerRecommendationPrincipale();
        chargerRecommendationsSecondaires();
    }


    private void chargerRecommendationPrincipale() {
        repository.getRecommandation(new MovieRepository.RecoCallback() {
            @Override
            public void onSuccess(OmdbResponse response, boolean estHorsLigne) {
                binding.tvRecoTitre1.setText(getString(R.string.reco_titre, response.getTitle(), response.getYear()));
                binding.tvRecoPlot1.setText(response.getPlot());

                String posterUrl = securiserUrlPoster(response.getPoster());
                if (posterUrl == null) {
                    binding.ivReco1.setVisibility(View.GONE);
                    binding.tvRecoFallback1.setVisibility(View.VISIBLE);
                } else {
                    binding.tvRecoFallback1.setVisibility(View.GONE);
                    binding.ivReco1.setVisibility(View.VISIBLE);
                    Glide.with(MainActivity.this)
                            .load(posterUrl)
                            .placeholder(R.drawable.ic_film_placeholder)
                            .error(R.drawable.ic_film_placeholder)
                            .into(binding.ivReco1);
                }

                binding.tvHorsLigne.setVisibility(estHorsLigne ? View.VISIBLE : View.GONE);
                ouvrirDetailDepuisRecommendation(binding.cardReco1, response);
            }

            @Override
            public void onEchec(String messageErreur) {
                binding.tvRecoTitre1.setText(R.string.reco_indisponible);
                binding.tvRecoPlot1.setText(messageErreur);
                binding.ivReco1.setVisibility(View.GONE);
                binding.tvRecoFallback1.setVisibility(View.VISIBLE);
                binding.tvHorsLigne.setVisibility(View.GONE);
            }
        });
    }

    private void chargerRecommendationsSecondaires() {
        List<String> pool = new ArrayList<>(Arrays.asList(
                "Inception", "Interstellar", "The Dark Knight",
                "Parasite", "Whiplash", "The Matrix", "Arrival"
        ));
        Collections.shuffle(pool);

        chargerCarteRecommendation(pool.get(0), binding.cardReco2, binding.tvRecoTitre2, binding.tvRecoPlot2, binding.ivReco2, binding.tvRecoFallback2);
        chargerCarteRecommendation(pool.get(1), binding.cardReco3, binding.tvRecoTitre3, binding.tvRecoPlot3, binding.ivReco3, binding.tvRecoFallback3);
    }

    private void initialiserCarteReco(android.widget.TextView titre, android.widget.TextView plot, android.widget.ImageView image, android.widget.TextView fallback) {
        titre.setText(R.string.chargement);
        plot.setText("");
        image.setVisibility(View.INVISIBLE);
        fallback.setVisibility(View.GONE);
    }

    private void chargerCarteRecommendation(String titreFilm, MaterialCardView card, android.widget.TextView titre, android.widget.TextView plot, android.widget.ImageView image, android.widget.TextView fallback) {
        repository.getPosterPourFilm(titreFilm, new MovieRepository.OmdbCallback() {
            @Override
            public void onSuccess(OmdbResponse response) {
                titre.setText(getString(R.string.reco_titre, response.getTitle(), response.getYear()));
                plot.setText(response.getPlot());

                String posterUrl = securiserUrlPoster(response.getPoster());
                if (posterUrl == null) {
                    image.setVisibility(View.GONE);
                    fallback.setVisibility(View.VISIBLE);
                } else {
                    fallback.setVisibility(View.GONE);
                    image.setVisibility(View.VISIBLE);
                    Glide.with(MainActivity.this)
                            .load(posterUrl)
                            .placeholder(R.drawable.ic_film_placeholder)
                            .error(R.drawable.ic_film_placeholder)
                            .into(image);
                }

                ouvrirDetailDepuisRecommendation(card, response);
            }

            @Override
            public void onEchec(String messageErreur) {
                titre.setText(R.string.reco_indisponible);
                plot.setText(messageErreur);
                image.setVisibility(View.GONE);
                fallback.setVisibility(View.VISIBLE);
            }
        });
    }

    private void ouvrirDetailDepuisRecommendation(MaterialCardView card, OmdbResponse response) {
        card.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MovieDetailActivity.class);
            intent.putExtra(Constants.INTENT_MODE_RECOMMANDATION, true);
            intent.putExtra(Constants.INTENT_RECO_TITRE, response.getTitle());
            intent.putExtra(Constants.INTENT_RECO_ANNEE, response.getYear());
            intent.putExtra(Constants.INTENT_RECO_GENRE, response.getGenre());
            intent.putExtra(Constants.INTENT_RECO_PLOT, response.getPlot());
            intent.putExtra(Constants.INTENT_RECO_POSTER, securiserUrlPoster(response.getPoster()));
            startActivity(intent);
        });
    }

    private String securiserUrlPoster(String posterUrl) {
        if (posterUrl == null || posterUrl.trim().isEmpty() || "N/A".equalsIgnoreCase(posterUrl)) {
            return null;
        }
        return posterUrl.replace("http://", "https://");
    }
}
