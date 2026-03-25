package com.groupe.cinetrack.network;

import com.google.gson.annotations.SerializedName;

/**
 * Classe qui représente la réponse JSON de l'API OMDb.
 * Gson mappe automatiquement les champs JSON vers les champs Java
 * grâce à l'annotation @SerializedName (quand le nom diffère).
 *
 * Exemple de réponse OMDb :
 * { "Title":"Inception", "Year":"2010", "Poster":"https://...", "Plot":"...", "Response":"True" }
 */
public class OmdbResponse {

    @SerializedName("Title")
    private String title;

    @SerializedName("Year")
    private String year;

    @SerializedName("Poster")
    private String poster;   // URL de l'affiche ou "N/A" si absente

    @SerializedName("Plot")
    private String plot;     // Synopsis

    @SerializedName("Genre")
    private String genre;    // Genres renvoyes par OMDb

    @SerializedName("Response")
    private String response; // "True" si le film a été trouvé, "False" sinon

    // --- Getters ---
    public String getTitle()    { return title;    }
    public String getYear()     { return year;     }
    public String getPoster()   { return poster;   }
    public String getPlot()     { return plot;     }
    public String getGenre()    { return genre;    }
    public String getResponse() { return response; }
}
