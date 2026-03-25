package com.groupe.cinetrack.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OmdbSearchResponse {
    @SerializedName("Search")
    private List<OmdbSearchItem> search;

    @SerializedName("Response")
    private String response;

    @SerializedName("Error")
    private String error;

    public List<OmdbSearchItem> getSearch() { return search; }
    public String getResponse() { return response; }
    public String getError() { return error; }
}
