package com.example.buscaya.data.api

import com.example.buscaya.data.model.Farmacia
import com.example.buscaya.data.model.SearchResult
import retrofit2.http.GET
import retrofit2.http.Query

interface FarmaciaApiService {
    
    @GET("api/farmacias_turno_android")
    suspend fun getFarmaciasEnTurno(): List<Farmacia>
    
    @GET("api/hybrid-search-android")
    suspend fun hybridSearch(
        @Query("q") query: String,
        @Query("lat") lat: String = "-34.1595",
        @Query("lon") lon: String = "-64.2670",
        @Query("radius") radius: String = "5000"
    ): List<SearchResult>
}
