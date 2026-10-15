package com.example.buscaya.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import com.example.buscaya.data.model.Farmacia

object RetrofitInstance {
    
    private const val BASE_URL = "https://buscaya.com.ar/"
    
    val api: FarmaciaApiService by lazy {
        val gson = GsonBuilder()
            .registerTypeAdapter(List::class.java, FarmaciaListDeserializer())
            .create()
        
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(FarmaciaApiService::class.java)
    }
}
