package com.example.buscaya.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.buscaya.data.local.entity.FarmaciaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para la entidad FarmaciaEntity.
 *
 * Principio arquitectónico:
 * - Consultas de lectura reactivas devuelven [Flow] (Room emite automáticamente
 *   cuando la tabla SQLite sufre inserciones, modificaciones o eliminaciones).
 * - Operaciones de escritura (insertar, actualizar, eliminar, borrar) son [suspend],
 *   garantizando que se ejecuten fuera del Main Thread (hilo principal).
 */
@Dao
interface FarmaciaDao {

    /**
     * Retorna un Flow reactivo con todas las farmacias ordenadas por ID.
     */
    @Query("SELECT * FROM farmacias ORDER BY id ASC")
    fun obtenerTodos(): Flow<List<FarmaciaEntity>>

    /**
     * Inserta una lista de entidades en la base de datos.
     * En caso de conflicto de clave primaria, reemplaza el registro (OnConflictStrategy.REPLACE).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(farmacias: List<FarmaciaEntity>)

    /**
     * Actualiza una farmacia existente en la base de datos local.
     */
    @Update
    suspend fun actualizar(farmacia: FarmaciaEntity)

    /**
     * Elimina una farmacia de la base de datos local.
     */
    @Delete
    suspend fun eliminar(farmacia: FarmaciaEntity)

    /**
     * Búsqueda por coincidencia de nombre usando LIKE parametrizado.
     *
     * IMPORTANTE PARA SEGURIDAD / DIDÁCTICA:
     * El comodín '%' se concatena dentro de la consulta SQL con el operador '||' de SQLite
     * junto al parámetro nombrado :query.
     * NUNCA concatenar texto plano en Kotlin (ej: "LIKE '%" + text + "%'")
     * porque generaría vulnerabilidad a inyección SQL (SQL Injection).
     */
    @Query("SELECT * FROM farmacias WHERE :query = '' OR nombre LIKE '%' || :query || '%' ORDER BY id ASC")
    fun buscarPorNombre(query: String): Flow<List<FarmaciaEntity>>

    /**
     * Cuenta el total de registros en la tabla.
     * Sirve para verificar si la base local ya cuenta con datos guardados.
     */
    @Query("SELECT COUNT(*) FROM farmacias")
    suspend fun contar(): Int

    /**
     * Borra todas las filas de la tabla de farmacias.
     */
    @Query("DELETE FROM farmacias")
    suspend fun borrarTodos()
}
