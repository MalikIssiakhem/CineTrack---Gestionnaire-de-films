package com.groupe.cinetrack.network;

import com.google.gson.annotations.SerializedName;

public class OmdbSearchItem {
    @SerializedName("Title")
    private String title;

    @SerializedName("Year")
    private String year;

    @SerializedName("imdbID")
    private String imdbId;

    @SerializedName("Poster")
    private String poster;

    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getImdbId() { return imdbId; }
    public String getPoster() { return poster; }
}
