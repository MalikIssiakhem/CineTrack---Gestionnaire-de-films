package com.groupe.cinetrack;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.groupe.cinetrack.databinding.ActivityAddEditMovieBinding;
import com.groupe.cinetrack.model.Movie;
import com.groupe.cinetrack.network.OmdbResponse;
import com.groupe.cinetrack.network.OmdbSearchItem;
import com.groupe.cinetrack.repository.MovieRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * AddEditMovieActivity : formulaire d'ajout ET de modification.
 *
 * En mode ajout, le film doit d'abord être recherché via OMDb pour rester
 * cohérent avec la démonstration API de la soutenance. L'utilisateur choisit
 * un résultat OMDb, puis l'écran pré-remplit les informations locales
 * (titre + genre) avant la sauvegarde SQLite.
 */
public class AddEditMovieActivity extends AppCompatActivity {

    private ActivityAddEditMovieBinding binding;
    private MovieRepository repository;
    private Movie filmAModifier;

    private final List<OmdbSearchItem> resultatsOmdb = new ArrayList<>();
    private OmdbSearchAdapter adapterResultats;
    private boolean filmSelectionneDepuisApi = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditMovieBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        repository = new MovieRepository(this);

        initialiserSpinners();
        initialiserListeResultats();

        boolean modeEdit = getIntent().getBooleanExtra(Constants.INTENT_MODE_EDIT, false);
        if (modeEdit) {
            filmAModifier = (Movie) getIntent().getSerializableExtra(Constants.INTENT_FILM);
            preRemplirFormulaire(filmAModifier);
            binding.cardRechercheFilm.setVisibility(View.GONE);
            binding.etTitre.setEnabled(true);
            binding.etGenre.setEnabled(true);
            setTitle(getString(R.string.titre_modifier_film));
        } else {
            filmAModifier = null;
            configurerModeAjoutDepuisApi();
            setTitle(getString(R.string.titre_ajouter_film));
        }

        binding.btnRechercherFilm.setOnClickListener(v -> lancerRechercheOmdb());
        binding.btnSauvegarder.setOnClickListener(v -> sauvegarder());
    }

    private void initialiserSpinners() {
        ArrayAdapter<CharSequence> adapterStatut = ArrayAdapter.createFromResource(
                this, R.array.statuts_film, android.R.layout.simple_spinner_item);
        adapterStatut.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerStatut.setAdapter(adapterStatut);

        ArrayAdapter<CharSequence> adapterNote = ArrayAdapter.createFromResource(
                this, R.array.notes_film, android.R.layout.simple_spinner_item);
        adapterNote.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerNote.setAdapter(adapterNote);
    }

    private void initialiserListeResultats() {
        adapterResultats = new OmdbSearchAdapter(this, resultatsOmdb);
        binding.lvResultatsFilms.setAdapter(adapterResultats);
        binding.lvResultatsFilms.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= resultatsOmdb.size()) {
                return;
            }
            OmdbSearchItem item = resultatsOmdb.get(position);
            chargerDetailsFilmSelectionne(item);
        });
    }

    private void configurerModeAjoutDepuisApi() {
        binding.etTitre.setEnabled(false);
        binding.etGenre.setEnabled(false);
        binding.etTitre.setHint(R.string.hint_titre_api);
        binding.etGenre.setHint(R.string.hint_genre_api);
        binding.tvSelectionAide.setVisibility(View.VISIBLE);
    }

    private void lancerRechercheOmdb() {
        String requete = binding.etRechercheFilm.getText().toString().trim();
        filmSelectionneDepuisApi = false;
        binding.etTitre.setText("");
        binding.etGenre.setText("");
        binding.tvSelectionAide.setText(R.string.selection_api_attendue);

        binding.progressRechercheFilm.setVisibility(View.VISIBLE);
        binding.lvResultatsFilms.setVisibility(View.GONE);
        binding.tvRechercheEtat.setVisibility(View.GONE);

        repository.rechercherFilmsPourAjout(requete, new MovieRepository.SearchMoviesCallback() {
            @Override
            public void onSuccess(List<OmdbSearchItem> filmsTrouves) {
                binding.progressRechercheFilm.setVisibility(View.GONE);
                resultatsOmdb.clear();
                resultatsOmdb.addAll(filmsTrouves);

                adapterResultats.notifyDataSetChanged();
                binding.lvResultatsFilms.setVisibility(View.VISIBLE);
            }

            @Override
            public void onEchec(String messageErreur) {
                binding.progressRechercheFilm.setVisibility(View.GONE);
                resultatsOmdb.clear();
                adapterResultats.notifyDataSetChanged();
                binding.lvResultatsFilms.setVisibility(View.GONE);
                binding.tvRechercheEtat.setVisibility(View.VISIBLE);
                binding.tvRechercheEtat.setText(messageErreur);
            }
        });
    }

    private void chargerDetailsFilmSelectionne(OmdbSearchItem item) {
        binding.progressRechercheFilm.setVisibility(View.VISIBLE);
        binding.tvRechercheEtat.setVisibility(View.VISIBLE);
        binding.tvRechercheEtat.setText(R.string.chargement_details_film);

        repository.getFilmParImdbId(item.getImdbId(), new MovieRepository.OmdbCallback() {
            @Override
            public void onSuccess(OmdbResponse response) {
                binding.progressRechercheFilm.setVisibility(View.GONE);
                binding.tvRechercheEtat.setVisibility(View.VISIBLE);
                binding.tvRechercheEtat.setText(getString(R.string.film_selectionne_api, response.getTitle()));
                binding.etTitre.setText(response.getTitle());
                binding.etGenre.setText(response.getGenre());
                filmSelectionneDepuisApi = true;
                binding.tvSelectionAide.setText(R.string.selection_api_ok);
            }

            @Override
            public void onEchec(String messageErreur) {
                binding.progressRechercheFilm.setVisibility(View.GONE);
                binding.tvRechercheEtat.setVisibility(View.VISIBLE);
                binding.tvRechercheEtat.setText(messageErreur);
                filmSelectionneDepuisApi = false;
            }
        });
    }

    private void preRemplirFormulaire(Movie film) {
        if (film == null) {
            return;
        }

        binding.etTitre.setText(film.getTitre());
        binding.etGenre.setText(film.getGenre());

        if (Constants.STATUT_VU.equals(film.getStatut())) {
            binding.spinnerStatut.setSelection(1);
        } else {
            binding.spinnerStatut.setSelection(0);
        }

        binding.spinnerNote.setSelection(film.getNote());
    }

    private void sauvegarder() {
        String titre = binding.etTitre.getText().toString().trim();
        String genre = binding.etGenre.getText().toString().trim();

        if (filmAModifier == null && !filmSelectionneDepuisApi) {
            Toast.makeText(this, R.string.erreur_selection_api, Toast.LENGTH_SHORT).show();
            return;
        }

        if (titre.isEmpty()) {
            binding.etTitre.setError(getString(R.string.erreur_titre_vide));
            return;
        }

        String statut = binding.spinnerStatut.getSelectedItem().toString();
        int note = binding.spinnerNote.getSelectedItemPosition();

        if (filmAModifier == null) {
            Movie nouveau = new Movie();
            nouveau.setTitre(titre);
            nouveau.setGenre(genre);
            nouveau.setStatut(statut);
            nouveau.setNote(note);
            repository.ajouterFilm(nouveau);
            Toast.makeText(this, R.string.film_ajoute, Toast.LENGTH_SHORT).show();
        } else {
            filmAModifier.setTitre(titre);
            filmAModifier.setGenre(genre);
            filmAModifier.setStatut(statut);
            filmAModifier.setNote(note);
            repository.modifierFilm(filmAModifier);
            Toast.makeText(this, R.string.film_modifie, Toast.LENGTH_SHORT).show();
        }

        setResult(RESULT_OK);
        finish();
    }
}
