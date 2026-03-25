package com.groupe.cinetrack.model;

import java.io.Serializable;

/**
 * Modèle de données représentant un film.
 *
 * On implémente Serializable plutôt que Parcelable :
 * - Serializable : interface Java standard, aucune méthode à écrire → plus simple à coder et expliquer
 * - Parcelable  : interface Android plus rapide mais demande d'implémenter plusieurs méthodes manuellement
 * Pour une soutenance de niveau 2ème année, Serializable est un choix justifiable et défendable.
 */
public class Movie implements Serializable {

    private int    id;
    private String titre;
    private String genre;
    private String statut;  // "Vu" ou "A voir" (voir Constants)
    private int    note;    // 0 à 5

    // Constructeur vide (utile pour créer un objet avant de le remplir)
    public Movie() {}

    // Constructeur complet (utile pour reconstruction depuis SQLite)
    public Movie(int id, String titre, String genre, String statut, int note) {
        this.id     = id;
        this.titre  = titre;
        this.genre  = genre;
        this.statut = statut;
        this.note   = note;
    }

    // --- Getters ---
    public int    getId()     { return id;     }
    public String getTitre()  { return titre;  }
    public String getGenre()  { return genre;  }
    public String getStatut() { return statut; }
    public int    getNote()   { return note;   }

    // --- Setters ---
    public void setId(int id)         { this.id     = id;     }
    public void setTitre(String t)    { this.titre  = t;      }
    public void setGenre(String g)    { this.genre  = g;      }
    public void setStatut(String s)   { this.statut = s;      }
    public void setNote(int n)        { this.note   = n;      }
}
