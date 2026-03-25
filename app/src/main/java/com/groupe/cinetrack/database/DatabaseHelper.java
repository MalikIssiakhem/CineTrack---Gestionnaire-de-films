package com.groupe.cinetrack.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.groupe.cinetrack.Constants;

/**
 * DatabaseHelper : gère la création et la mise à jour de la base SQLite.
 * On hérite de SQLiteOpenHelper qui s'occupe d'ouvrir/créer la base.
 *
 * Pourquoi SQLite pour les films ?
 * → Les films sont des données structurées (titre, genre, note...) qu'on
 *   doit filtrer et trier → SQLite avec ses requêtes est idéal.
 *   SharedPreferences ne convient que pour des paires clé/valeur simples.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Requête SQL de création de la table films
    private static final String CREATE_TABLE_FILMS =
            "CREATE TABLE " + Constants.TABLE_FILMS + " ("
            + Constants.COL_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Constants.COL_TITRE  + " TEXT NOT NULL, "
            + Constants.COL_GENRE  + " TEXT, "
            + Constants.COL_STATUT + " TEXT NOT NULL DEFAULT '" + Constants.STATUT_A_VOIR + "', "
            + Constants.COL_NOTE   + " INTEGER DEFAULT 0"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, Constants.DB_NOM, null, Constants.DB_VERSION);
    }

    /**
     * onCreate est appelée UNE SEULE FOIS, lors de la toute première
     * ouverture de la base (quand le fichier n/existe pas encore).
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_FILMS);
    }

    /**
     * onUpgrade est appelée quand DB_VERSION augmente.
     * Ici on supprime et recrée la table (simple mais perd les données).
     * En production on ferait des ALTER TABLE pour migrer sans perte.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_FILMS);
        onCreate(db);
    }
}
