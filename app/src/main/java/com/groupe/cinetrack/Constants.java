package com.groupe.cinetrack;

/**
 * Classe Constants : centralise toutes les constantes du projet.
 * Avantage : si on veut changer un nom de colonne ou une clé SP,
 * on le change à un seul endroit. Aucune valeur en dur dans les Activities.
 */
public class Constants {

    // -------------------------------------------------------
    // SharedPreferences
    // -------------------------------------------------------
    public static final String SP_NOM = "cinetrack_prefs";

    // Clés profil utilisateur
    public static final String SP_PRENOM        = "prenom";
    public static final String SP_EMAIL         = "email";
    public static final String SP_GENRE_FAV     = "genre_favori";

    // Clés cache recommandation hors-ligne
    public static final String SP_RECO_TITRE    = "reco_titre";
    public static final String SP_RECO_ANNEE    = "reco_annee";
    public static final String SP_RECO_PLOT     = "reco_plot";
    public static final String SP_RECO_POSTER   = "reco_poster";

    // -------------------------------------------------------
    // SQLite
    // -------------------------------------------------------
    public static final String DB_NOM           = "cinetrack.db";
    public static final int    DB_VERSION       = 1;

    public static final String TABLE_FILMS      = "films";
    public static final String COL_ID           = "id";
    public static final String COL_TITRE        = "titre";
    public static final String COL_GENRE        = "genre";
    public static final String COL_STATUT       = "statut";
    public static final String COL_NOTE         = "note";

    // Valeurs possibles pour le statut
    public static final String STATUT_VU        = "Vu";
    public static final String STATUT_A_VOIR    = "A voir";

    // -------------------------------------------------------
    // API OMDb  (https://www.omdbapi.com/)
    // Remplacer "VOTRE_CLE_ICI" par une vraie clé gratuite
    // obtenue sur https://www.omdbapi.com/apikey.aspx
    // La clé est ici dans Constants, jamais dans une Activity.
    // -------------------------------------------------------
    public static final String OMDB_BASE_URL    = "https://www.omdbapi.com/";
    public static final String OMDB_API_KEY     = "21e1fd71";

    // Intent key pour passer un film entre Activities
    public static final String INTENT_FILM      = "film";
    // Intent key pour savoir si on est en mode modification
    public static final String INTENT_MODE_EDIT = "mode_edit";
    public static final String INTENT_RECO_TITRE = "reco_titre";
    public static final String INTENT_RECO_ANNEE = "reco_annee";
    public static final String INTENT_RECO_GENRE = "reco_genre";
    public static final String INTENT_RECO_PLOT = "reco_plot";
    public static final String INTENT_RECO_POSTER = "reco_poster";
    public static final String INTENT_MODE_RECOMMANDATION = "mode_recommandation";
}
