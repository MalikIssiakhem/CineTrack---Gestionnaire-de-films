package com.groupe.cinetrack.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OmdbApi {

    @GET(".")
    Call<OmdbResponse> getFilmParTitre(
        @Query("t") String titre,
        @Query("apikey") String apiKey
    );

    @GET(".")
    Call<OmdbResponse> getFilmParImdbId(
        @Query("i") String imdbId,
        @Query("plot") String plot,
        @Query("apikey") String apiKey
    );

    @GET(".")
    Call<OmdbSearchResponse> rechercherFilms(
        @Query("s") String titre,
        @Query("apikey") String apiKey
    );
}
