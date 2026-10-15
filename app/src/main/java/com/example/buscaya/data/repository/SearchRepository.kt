package com.example.buscaya.data.repository

import android.util.Log
import com.example.buscaya.data.api.RetrofitInstance
import com.example.buscaya.data.model.SearchResult

class SearchRepository {
    
    suspend fun hybridSearch(query: String): List<SearchResult> {
        return try {
            Log.d("SearchRepo", "Buscando: $query")
            val response = RetrofitInstance.api.hybridSearch(query)
            Log.d("SearchRepo", "Resultados encontrados: ${response.size}")
            response
        } catch (e: Exception) {
            Log.e("SearchRepo", "Error en búsqueda", e)
            throw Exception("Error al buscar: ${e.message}")
        }
    }
}
