package com.groupe.cinetrack.network;

import com.groupe.cinetrack.Constants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton Retrofit.
 * On utilise le pattern Singleton pour ne créer l'instance Retrofit
 * qu'une seule fois, ce qui est plus efficace (la création est coûteuse).
 * Toutes les classes qui en ont besoin utilisent RetrofitClient.getInstance().
 */
public class RetrofitClient {

    private static Retrofit instance;

    // Constructeur privé → on ne peut pas créer d'instance depuis l'extérieur
    private RetrofitClient() {}

    public static Retrofit getInstance() {
        if (instance == null) {
            // On construit Retrofit avec la base URL et le convertisseur Gson
            instance = new Retrofit.Builder()
                    .baseUrl(Constants.OMDB_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }

    // Raccourci pour obtenir directement l'API OMDb
    public static OmdbApi getOmdbApi() {
        return getInstance().create(OmdbApi.class);
    }
}
