package com.example.buscaya.data.api

import com.example.buscaya.data.model.Farmacia
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import android.util.Log
import java.lang.reflect.Type

class FarmaciaListDeserializer : JsonDeserializer<List<*>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<*> {
        return try {
            Log.d("FarmaciaDeserializer", "JSON recibido: $json")
            
            val actualType = if (typeOfT is java.lang.reflect.ParameterizedType) {
                typeOfT.actualTypeArguments[0]
            } else {
                Farmacia::class.java
            }
            
            when {
                json?.isJsonArray == true -> {
                    Log.d("FarmaciaDeserializer", "Es un array directo")
                    json.asJsonArray.mapNotNull { element ->
                        context?.deserialize(element, actualType)
                    }
                }
                json?.isJsonObject == true -> {
                    Log.d("FarmaciaDeserializer", "Es un objeto, buscando farmacias/data")
                    val jsonObject = json.asJsonObject
                    val farmaciasArray = when {
                        jsonObject.has("farmacias") -> {
                            Log.d("FarmaciaDeserializer", "Encontrado campo 'farmacias'")
                            jsonObject.get("farmacias")
                        }
                        jsonObject.has("results") -> {
                            Log.d("FarmaciaDeserializer", "Encontrado campo 'results'")
                            jsonObject.get("results")
                        }
                        jsonObject.has("data") -> {
                            Log.d("FarmaciaDeserializer", "Encontrado campo 'data'")
                            jsonObject.get("data")
                        }
                        else -> {
                            Log.w("FarmaciaDeserializer", "No encontrado 'farmacias', 'results' ni 'data', campos disponibles: ${jsonObject.keySet()}")
                            null
                        }
                    }
                    
                    if (farmaciasArray != null && farmaciasArray.isJsonArray) {
                        farmaciasArray.asJsonArray.mapNotNull { element ->
                            context?.deserialize(element, actualType)
                        }
                    } else {
                        Log.w("FarmaciaDeserializer", "Campo no es un array")
                        emptyList<Any>()
                    }
                }
                else -> {
                    Log.w("FarmaciaDeserializer", "JSON no es array ni objeto: $json")
                    emptyList<Any>()
                }
            }
        } catch (e: Exception) {
            Log.e("FarmaciaDeserializer", "Error deserializando", e)
            emptyList<Any>()
        }
    }
}
