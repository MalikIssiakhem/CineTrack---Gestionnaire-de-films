package com.groupe.cinetrack.utils;

import com.groupe.cinetrack.Constants;
import com.groupe.cinetrack.model.Movie;

import java.util.List;

/**
 * MovieUtils : contient toute la logique métier.
 * On sépare cette logique des Activities pour deux raisons :
 * 1. Facilement testable (pas besoin d'Android pour tester des calculs)
 * 2. Les Activities restent simples : elles délèguent le calcul ici
 */
public class MovieUtils {

    /**
     * Formate un film en chaîne lisible pour la ListView.
     * Exemple : "[VU] Inception - Science-Fiction - ★4/5"
     * Exemple : "Interstellar - Drame - ★3/5"
     */
    public static String formaterFilm(Movie film) {
        String prefixe = Constants.STATUT_VU.equals(film.getStatut()) ? "[VU] " : "";
        String etoiles = formaterNote(film.getNote());
        return prefixe + film.getTitre() + " - " + film.getGenre() + " - " + etoiles;
    }

    /**
     * Convertit une note entière en étoiles textuelles.
     * Ex : 4 → "★4/5"
     */
    public static String formaterNote(int note) {
        return "★" + note + "/5";
    }

    /**
     * Calcule la note moyenne d'une liste de films.
     * Retourne 0 si la liste est vide (évite la division par zéro).
     */
    public static float calculerMoyenne(List<Movie> films) {
        if (films == null || films.isEmpty()) return 0f;
        int total = 0;
        for (Movie film : films) {
            total += film.getNote();
        }
        return (float) total / films.size();
    }

    /**
     * Compte le nombre de films avec un statut donné.
     * Utilisé par le dashboard pour les compteurs "Vus" / "À voir".
     */
    public static int compterParStatut(List<Movie> films, String statut) {
        if (films == null) return 0;
        int count = 0;
        for (Movie film : films) {
            if (statut.equals(film.getStatut())) count++;
        }
        return count;
    }
}
