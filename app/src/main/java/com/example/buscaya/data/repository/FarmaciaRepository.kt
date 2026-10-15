package com.example.buscaya.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.buscaya.data.api.RetrofitInstance
import com.example.buscaya.data.local.AppDatabase
import com.example.buscaya.data.local.dao.FarmaciaDao
import com.example.buscaya.data.local.entity.toEntityList
import com.example.buscaya.data.local.entity.toUiModelList
import com.example.buscaya.data.model.Farmacia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Repositorio de Farmacias bajo el patrón Single Source of Truth (Única fuente de verdad).
 *
 * Principio arquitectónico:
 * - Room (Base de Datos Local) es la única fuente de datos para la UI.
 * - La UI observa el [Flow] emitido directamente desde Room.
 * - La red (API Retrofit) sólo actualiza la base de datos local mediante transacciones atómicas.
 */
class FarmaciaRepository(
    private val database: AppDatabase,
    private val farmaciaDao: FarmaciaDao = database.farmaciaDao()
) {

    /**
     * Flow reactivo que expone las farmacias almacenadas en Room.
     * Convierte las entidades de Room a modelos de UI (Farmacia) mediante [toUiModelList].
     */
    val farmacias: Flow<List<Farmacia>> = farmaciaDao.obtenerTodos().map { entities ->
        entities.toUiModelList()
    }

    /**
     * Sincroniza los datos con la API remota.
     *
     * Reglas de negocio:
     * - Si la base local está vacía (contar() == 0) o si [forzar] es true, consulta a la API de Retrofit.
     * - Si la API responde con éxito, dentro de una transacción atómica ([database.withTransaction]),
     *   borra los registros anteriores y guarda los nuevos.
     * - Si la llamada falla (por falta de conexión a internet o error de servidor), NO borra ningún
     *   registro existente y propaga la excepción en un [Result.failure] para que la UI pueda
     *   mostrar la advertencia de "Sin conexión" manteniendo los datos guardados visibles.
     */
    suspend fun refrescar(forzar: Boolean = false): Result<Unit> {
        return try {
            val totalLocales = farmaciaDao.contar()
            Log.d("FarmaciaRepo", "Total en base local: $totalLocales. forzar=$forzar")

            if (forzar || totalLocales == 0) {
                Log.d("FarmaciaRepo", "Consultando API externa...")
                val respuestaApi = RetrofitInstance.api.getFarmaciasEnTurno()
                Log.d("FarmaciaRepo", "API respondió con ${respuestaApi.size} farmacias")

                val entidades = respuestaApi.toEntityList()

                // Transacción atómica en Room: borra y reinserta para mantener consistencia
                database.withTransaction {
                    farmaciaDao.borrarTodos()
                    farmaciaDao.insertarTodos(entidades)
                }
                Log.d("FarmaciaRepo", "Datos actualizados en Room correctamente.")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FarmaciaRepo", "Error al refrescar desde la API: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene la lista actual de farmacias almacenadas de manera síncrona/suspendida.
     */
    suspend fun obtenerGuardadas(): List<Farmacia> {
        return farmacias.first()
    }

    /**
     * Cuenta la cantidad de registros actualmente guardados en Room.
     */
    suspend fun contar(): Int = farmaciaDao.contar()

    /**
     * Búsqueda por nombre dentro de la base de datos local usando LIKE parametrizado.
     */
    fun buscarPorNombre(query: String): Flow<List<Farmacia>> {
        return farmaciaDao.buscarPorNombre(query).map { it.toUiModelList() }
    }
}
