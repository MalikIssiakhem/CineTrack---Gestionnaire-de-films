package com.groupe.cinetrack;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.groupe.cinetrack.databinding.ActivityProfileBinding;

/**
 * ProfileActivity : gestion du profil utilisateur.
 *
 * Données persistées dans SharedPreferences (et non SQLite) car :
 * → Ce sont des préférences simples (une seule valeur par clé),
 *   pas des données structurées à filtrer/trier.
 *   SharedPreferences est conçu exactement pour ça.
 *
 * La rotation d'écran ne provoque aucune perte de données car :
 * → On charge depuis SharedPreferences dans onResume (données déjà sauvées)
 * → On utilise aussi onSaveInstanceState pour les modifications en cours
 */
public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private SharedPreferences prefs;

    // Clé pour sauvegarder les données en cours de saisie (rotation)
    private static final String KEY_PRENOM_TEMP = "prenom_temp";
    private static final String KEY_EMAIL_TEMP  = "email_temp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences(Constants.SP_NOM, MODE_PRIVATE);

        // Spinner pour le genre favori
        ArrayAdapter<CharSequence> adapterGenre = ArrayAdapter.createFromResource(
                this, R.array.genres_film, android.R.layout.simple_spinner_item);
        adapterGenre.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerGenreFavori.setAdapter(adapterGenre);

        // Si on a des données sauvegardées lors d'une rotation, on les restaure
        // sinon on charge depuis SharedPreferences
        if (savedInstanceState != null) {
            // Rotation détectée : on restaure les saisies en cours
            binding.etPrenom.setText(savedInstanceState.getString(KEY_PRENOM_TEMP, ""));
            binding.etEmail.setText(savedInstanceState.getString(KEY_EMAIL_TEMP, ""));
        } else {
            // Premier démarrage : on charge les données sauvegardées
            chargerProfil();
        }

        binding.btnSauvegarderProfil.setOnClickListener(v -> sauvegarderProfil());
    }

    /** Charge le profil depuis SharedPreferences et remplit le formulaire */
    private void chargerProfil() {
        binding.etPrenom.setText(prefs.getString(Constants.SP_PRENOM,    ""));
        binding.etEmail.setText( prefs.getString(Constants.SP_EMAIL,     ""));

        // Restaurer la bonne position du Spinner à partir de la valeur texte sauvegardée
        String genreFav = prefs.getString(Constants.SP_GENRE_FAV, "");
        String[] genres = getResources().getStringArray(R.array.genres_film);
        for (int i = 0; i < genres.length; i++) {
            if (genres[i].equals(genreFav)) {
                binding.spinnerGenreFavori.setSelection(i);
                break;
            }
        }
    }

    /** Valide et sauvegarde le profil dans SharedPreferences */
    private void sauvegarderProfil() {
        String prenom   = binding.etPrenom.getText().toString().trim();
        String email    = binding.etEmail.getText().toString().trim();
        String genreFav = binding.spinnerGenreFavori.getSelectedItem().toString();

        // Validation : prénom obligatoire
        if (prenom.isEmpty()) {
            binding.etPrenom.setError(getString(R.string.erreur_prenom_vide));
            return;
        }

        // Validation : email via Patterns.EMAIL_ADDRESS (classe Android standard)
        // Patterns.EMAIL_ADDRESS est une expression régulière fournie par Android,
        // pas besoin de bibliothèque externe.
        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError(getString(R.string.erreur_email_invalide));
            return;
        }

        // Sauvegarde dans SharedPreferences
        prefs.edit()
             .putString(Constants.SP_PRENOM,    prenom)
             .putString(Constants.SP_EMAIL,     email)
             .putString(Constants.SP_GENRE_FAV, genreFav)
             .apply();

        Toast.makeText(this, R.string.profil_sauvegarde, Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * onSaveInstanceState est appelé juste avant la destruction de l'Activity
     * (ex: rotation d'écran). On y sauvegarde les saisies en cours (non encore validées)
     * pour les restaurer dans onCreate si savedInstanceState != null.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_PRENOM_TEMP, binding.etPrenom.getText().toString());
        outState.putString(KEY_EMAIL_TEMP,  binding.etEmail.getText().toString());
        // Le Spinner est automatiquement restauré par Android (il a un id dans le XML)
    }
}
