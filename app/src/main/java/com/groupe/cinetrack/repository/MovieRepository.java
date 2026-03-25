package com.groupe.cinetrack.repository;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.groupe.cinetrack.Constants;
import com.groupe.cinetrack.database.DatabaseHelper;
import com.groupe.cinetrack.model.Movie;
import com.groupe.cinetrack.network.OmdbResponse;
import com.groupe.cinetrack.network.OmdbSearchItem;
import com.groupe.cinetrack.network.OmdbSearchResponse;
import com.groupe.cinetrack.network.RetrofitClient;
import com.groupe.cinetrack.utils.MovieUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * MovieRepository : point central de toutes les données.
 *
 * RÈGLE FONDAMENTALE respectée ici :
 * - Aucune Activity n'accède directement à SQLiteDatabase
 * - Aucune Activity ne crée d'appel Retrofit
 * → Tout passe par ce repository. Les Activities reçoivent les résultats
 *   via des callbacks (interfaces Java simples).
 *
 * Pourquoi ce pattern ?
 * → Séparation des responsabilités : l'Activity gère l'UI, le repository
 *   gère les données. Si on change la source de données (SQLite → Room),
 *   on ne touche qu'au repository.
 */
public class MovieRepository {

    private final DatabaseHelper dbHelper;
    private final SharedPreferences prefs;

    public MovieRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
        this.prefs    = context.getSharedPreferences(Constants.SP_NOM, Context.MODE_PRIVATE);
    }

    // -------------------------------------------------------
    // INTERFACES CALLBACK
    // Permettent au repository de "répondre" à l'Activity
    // sans créer de dépendance directe vers l'Activity.
    // -------------------------------------------------------

    /** Callback pour un résultat OMDb (poster + infos) */
    public interface OmdbCallback {
        void onSuccess(OmdbResponse response);
        void onEchec(String messageErreur);
    }

    /** Callback pour la recommandation (peut venir du cache) */
    public interface RecoCallback {
        void onSuccess(OmdbResponse response, boolean estHorsLigne);
        void onEchec(String messageErreur);
    }

    /** Callback pour une recherche de films OMDb */
    public interface SearchMoviesCallback {
        void onSuccess(List<OmdbSearchItem> filmsTrouves);
        void onEchec(String messageErreur);
    }

    // -------------------------------------------------------
    // CRUD SQLITE - FILMS
    // SQLiteDatabase est utilisé ICI, jamais dans les Activities.
    // -------------------------------------------------------

    /**
     * Récupère tous les films depuis SQLite.
     * On utilise un Cursor pour parcourir les résultats ligne par ligne.
     */
    public List<Movie> getTousLesFilms() {
        List<Movie> films = new ArrayList<>();
        // getReadableDatabase() ouvre la base en lecture seule (plus léger)
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                Constants.TABLE_FILMS,  // table
                null,                   // toutes les colonnes
                null,                   // pas de filtre WHERE
                null,                   // pas de valeurs pour WHERE
                null, null,             // pas de GROUP BY / HAVING
                Constants.COL_TITRE + " ASC" // tri par titre
        );

        // On parcourt le curseur ligne par ligne
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Movie film = cursorVersFilm(cursor);
                films.add(film);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return films;
    }

    /**
     * Ajoute un film dans SQLite.
     * ContentValues fonctionne comme une Map<colonne, valeur>.
     * Retourne l'id généré automatiquement (AUTOINCREMENT).
     */
    public long ajouterFilm(Movie film) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.COL_TITRE,  film.getTitre());
        values.put(Constants.COL_GENRE,  film.getGenre());
        values.put(Constants.COL_STATUT, film.getStatut());
        values.put(Constants.COL_NOTE,   film.getNote());
        long id = db.insert(Constants.TABLE_FILMS, null, values);
        db.close();
        return id;
    }

    /**
     * Met à jour un film existant dans SQLite.
     * On cible la ligne par son id (clause WHERE id = ?).
     */
    public void modifierFilm(Movie film) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.COL_TITRE,  film.getTitre());
        values.put(Constants.COL_GENRE,  film.getGenre());
        values.put(Constants.COL_STATUT, film.getStatut());
        values.put(Constants.COL_NOTE,   film.getNote());
        db.update(
                Constants.TABLE_FILMS,
                values,
                Constants.COL_ID + " = ?",
                new String[]{ String.valueOf(film.getId()) }
        );
        db.close();
    }

    /**
     * Supprime un film par son id.
     * La confirmation AlertDialog est gérée dans MovieListActivity,
     * cette méthode ne fait que la suppression effective.
     */
    public void supprimerFilm(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(
                Constants.TABLE_FILMS,
                Constants.COL_ID + " = ?",
                new String[]{ String.valueOf(id) }
        );
        db.close();
    }

    // -------------------------------------------------------
    // STATISTIQUES DASHBOARD
    // -------------------------------------------------------


    /** Verifie si un film existe deja en base par son titre */
    public boolean filmExisteDeja(String titre) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + Constants.TABLE_FILMS + " WHERE LOWER(" + Constants.COL_TITRE + ") = LOWER(?)",
                new String[]{ titre }
        );
        boolean existe = false;
        if (cursor != null && cursor.moveToFirst()) {
            existe = cursor.getInt(0) > 0;
            cursor.close();
        }
        db.close();
        return existe;
    }

    /** Compte les films avec un statut précis (ex: "Vu") */
    public int compterFilmsParStatut(String statut) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // rawQuery permet d'écrire du SQL directement
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + Constants.TABLE_FILMS
                + " WHERE " + Constants.COL_STATUT + " = ?",
                new String[]{ statut }
        );
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0); // la première (et seule) colonne du résultat
            cursor.close();
        }
        db.close();
        return count;
    }

    /** Calcule la moyenne des notes depuis SQLite avec AVG() */
    public float getMoyenneNotes() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT AVG(" + Constants.COL_NOTE + ") FROM " + Constants.TABLE_FILMS,
                null
        );
        float moyenne = 0f;
        if (cursor != null && cursor.moveToFirst()) {
            moyenne = cursor.getFloat(0);
            cursor.close();
        }
        db.close();
        return moyenne;
    }

    // -------------------------------------------------------
    // API RETROFIT - RECOMMANDATION ET POSTER
    // -------------------------------------------------------

    /**
     * Récupère la recommandation depuis l'API OMDb (titre aléatoire connu).
     * En cas d'échec réseau (onFailure), on lit le cache SharedPreferences.
     *
     * Pourquoi Retrofit appelle en asynchrone ?
     * → Les appels réseau peuvent prendre plusieurs secondes.
     *   Si on le faisait sur le thread principal (UI thread), l'application
     *   gèlerait le temps de la requête. Retrofit utilise automatiquement
     *   un thread en arrière-plan et rappelle le callback sur le thread UI.
     */
    public void getRecommandation(final RecoCallback callback) {
        // On choisit un titre de film connu pour la démo
        String[] titresdExemple = {"Inception", "The Dark Knight", "Interstellar", "Parasite"};
        String titreAleatoire   = titresdExemple[(int)(Math.random() * titresdExemple.length)];

        RetrofitClient.getOmdbApi()
            .getFilmParTitre(titreAleatoire, Constants.OMDB_API_KEY)
            .enqueue(new Callback<OmdbResponse>() {

                @Override
                public void onResponse(Call<OmdbResponse> call, Response<OmdbResponse> response) {
                    // onResponse est appelé quand on a reçu une réponse HTTP
                    // (même une erreur 404 ou 500 passe par ici)
                    if (response.isSuccessful() && response.body() != null
                            && "True".equals(response.body().getResponse())) {
                        // Succès : on sauvegarde en cache pour le mode hors-ligne
                        sauvegarderCacheReco(response.body());
                        callback.onSuccess(response.body(), false);
                    } else {
                        // Réponse HTTP reçue mais film non trouvé → on essaie le cache
                        lireDepuisCache(callback);
                    }
                }

                @Override
                public void onFailure(Call<OmdbResponse> call, Throwable t) {
                    // onFailure est appelé uniquement en cas d'absence de réseau
                    // ou de timeout → on utilise le cache hors-ligne
                    lireDepuisCache(callback);
                }
            });
    }

    /**
     * Recherche des films OMDb pour l'écran d'ajout.
     * L'Activity ne parle jamais directement à Retrofit : elle passe par le repository.
     */
    public void rechercherFilmsPourAjout(String requete, final SearchMoviesCallback callback) {
        String titreNettoye = nettoyerTitrePourRecherche(requete);

        if (titreNettoye.isEmpty()) {
            callback.onEchec("Saisissez un titre avant de lancer la recherche");
            return;
        }

        RetrofitClient.getOmdbApi()
                .rechercherFilms(titreNettoye, Constants.OMDB_API_KEY)
                .enqueue(new Callback<OmdbSearchResponse>() {
                    @Override
                    public void onResponse(Call<OmdbSearchResponse> call, Response<OmdbSearchResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && "True".equals(response.body().getResponse())
                                && response.body().getSearch() != null
                                && !response.body().getSearch().isEmpty()) {
                            callback.onSuccess(response.body().getSearch());
                        } else if (response.body() != null && response.body().getError() != null) {
                            callback.onEchec(response.body().getError());
                        } else {
                            callback.onEchec("Aucun film trouvé");
                        }
                    }

                    @Override
                    public void onFailure(Call<OmdbSearchResponse> call, Throwable t) {
                        callback.onEchec("Pas de connexion réseau");
                    }
                });
    }

    /**
     * Charge le détail complet d'un film OMDb à partir de son imdbID.
     * Utilisé après la sélection d'un résultat de recherche dans AddEditMovieActivity.
     */
    public void getFilmParImdbId(String imdbId, final OmdbCallback callback) {
        if (imdbId == null || imdbId.trim().isEmpty()) {
            callback.onEchec("Identifiant IMDb invalide");
            return;
        }
        chargerFilmCompletParImdbId(imdbId, callback);
    }

    /**
     * Récupère poster + synopsis d'un film par son titre (pour MovieDetailActivity).
     * Même logique Retrofit mais sans cache, résultat via OmdbCallback.
     */
    public void getPosterPourFilm(String titre, final OmdbCallback callback) {
        chercherFilmAvecFallback(titre, callback);
    }

    private void chercherFilmAvecFallback(String titre, final OmdbCallback callback) {
        RetrofitClient.getOmdbApi()
            .getFilmParTitre(titre, Constants.OMDB_API_KEY)
            .enqueue(new Callback<OmdbResponse>() {
                @Override
                public void onResponse(Call<OmdbResponse> call, Response<OmdbResponse> response) {
                    if (response.isSuccessful() && response.body() != null
                            && "True".equals(response.body().getResponse())) {
                        callback.onSuccess(response.body());
                    } else {
                        rechercherParTitreApproximatif(titre, callback);
                    }
                }

                @Override
                public void onFailure(Call<OmdbResponse> call, Throwable t) {
                    callback.onEchec("Pas de connexion réseau");
                }
            });
    }

    private void rechercherParTitreApproximatif(String titre, final OmdbCallback callback) {
        String titreNettoye = nettoyerTitrePourRecherche(titre);

        RetrofitClient.getOmdbApi()
            .rechercherFilms(titreNettoye, Constants.OMDB_API_KEY)
            .enqueue(new Callback<OmdbSearchResponse>() {
                @Override
                public void onResponse(Call<OmdbSearchResponse> call, Response<OmdbSearchResponse> response) {
                    if (response.isSuccessful() && response.body() != null
                            && "True".equals(response.body().getResponse())
                            && response.body().getSearch() != null
                            && !response.body().getSearch().isEmpty()) {

                        OmdbSearchItem premierResultat = response.body().getSearch().get(0);
                        chargerFilmCompletParImdbId(premierResultat.getImdbId(), callback);
                    } else {
                        callback.onEchec("Film non trouvé sur OMDb");
                    }
                }

                @Override
                public void onFailure(Call<OmdbSearchResponse> call, Throwable t) {
                    callback.onEchec("Pas de connexion réseau");
                }
            });
    }

    private void chargerFilmCompletParImdbId(String imdbId, final OmdbCallback callback) {
        RetrofitClient.getOmdbApi()
            .getFilmParImdbId(imdbId, "full", Constants.OMDB_API_KEY)
            .enqueue(new Callback<OmdbResponse>() {
                @Override
                public void onResponse(Call<OmdbResponse> call, Response<OmdbResponse> response) {
                    if (response.isSuccessful() && response.body() != null
                            && "True".equals(response.body().getResponse())) {
                        callback.onSuccess(response.body());
                    } else {
                        callback.onEchec("Film non trouvé sur OMDb");
                    }
                }

                @Override
                public void onFailure(Call<OmdbResponse> call, Throwable t) {
                    callback.onEchec("Pas de connexion réseau");
                }
            });
    }

    private String nettoyerTitrePourRecherche(String titre) {
        if (titre == null) {
            return "";
        }

        String resultat = titre.trim();
        resultat = resultat.replaceAll("\\(.*?\\)", " ");
        resultat = resultat.replaceAll("\\[.*?\\]", " ");
        resultat = resultat.replaceAll(":.*$", "");
        resultat = resultat.replaceAll("-.*$", "");
        resultat = resultat.replaceAll("\\s+", " ").trim();

        return resultat.isEmpty() ? titre.trim() : resultat;
    }

    // -------------------------------------------------------
    // CACHE HORS-LIGNE (SharedPreferences)
    // -------------------------------------------------------

    /** Sauvegarde la recommandation reçue dans SharedPreferences */
    private void sauvegarderCacheReco(OmdbResponse reco) {
        prefs.edit()
             .putString(Constants.SP_RECO_TITRE,  reco.getTitle())
             .putString(Constants.SP_RECO_ANNEE,  reco.getYear())
             .putString(Constants.SP_RECO_PLOT,   reco.getPlot())
             .putString(Constants.SP_RECO_POSTER, reco.getPoster())
             .apply(); // apply() est asynchrone (non bloquant), commit() est synchrone
    }

    /**
     * Relit la recommandation depuis le cache SharedPreferences.
     * Appelée quand il n'y a pas de réseau.
     */
    private void lireDepuisCache(RecoCallback callback) {
        String titre = prefs.getString(Constants.SP_RECO_TITRE, null);
        if (titre == null) {
            // Pas de cache disponible non plus
            callback.onEchec("Aucune recommandation disponible");
            return;
        }
        // On reconstruit un objet OmdbResponse "factice" avec les données en cache
        // (OmdbResponse n'a pas de setters, on crée une sous-classe anonyme simple ici)
        OmdbResponse cache = new OmdbResponse() {
            // On surcharge les getters avec les valeurs du cache
        };
        // Comme OmdbResponse n'a pas de setters, on crée une version locale simple :
        CacheOmdbResponse cacheReponse = new CacheOmdbResponse(
                prefs.getString(Constants.SP_RECO_TITRE,  ""),
                prefs.getString(Constants.SP_RECO_ANNEE,  ""),
                prefs.getString(Constants.SP_RECO_PLOT,   ""),
                prefs.getString(Constants.SP_RECO_POSTER, "")
        );
        callback.onSuccess(cacheReponse, true); // true = mode hors-ligne
    }

    // -------------------------------------------------------
    // UTILITAIRE INTERNE
    // -------------------------------------------------------

    /** Convertit une ligne du Cursor en objet Movie */
    private Movie cursorVersFilm(Cursor cursor) {
        int    id     = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.COL_ID));
        String titre  = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_TITRE));
        String genre  = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_GENRE));
        String statut = cursor.getString(cursor.getColumnIndexOrThrow(Constants.COL_STATUT));
        int    note   = cursor.getInt(cursor.getColumnIndexOrThrow(Constants.COL_NOTE));
        return new Movie(id, titre, genre, statut, note);
    }

    /**
     * Classe interne simple pour représenter un résultat du cache.
     * On l'utilise à la place de OmdbResponse (qui dépend de Gson)
     * pour le mode hors-ligne.
     */
    public static class CacheOmdbResponse extends OmdbResponse {
        private final String titre;
        private final String annee;
        private final String plot;
        private final String poster;

        public CacheOmdbResponse(String titre, String annee, String plot, String poster) {
            this.titre  = titre;
            this.annee  = annee;
            this.plot   = plot;
            this.poster = poster;
        }

        @Override public String getTitle()    { return titre;  }
        @Override public String getYear()     { return annee;  }
        @Override public String getPlot()     { return plot;   }
        @Override public String getPoster()   { return poster; }
        @Override public String getResponse() { return "True"; }
    }
}
