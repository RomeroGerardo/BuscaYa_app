package com.example.buscaya.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.buscaya.data.model.Farmacia

/**
 * Entidad de Room que representa una fila en la tabla "farmacias".
 *
 * Mantenemos esta entidad separada del DTO de la API (Farmacia) para seguir
 * el principio de responsabilidad única (SRP) y una arquitectura limpia:
 * - El DTO maneja el contrato y serialización con el servidor (JSON).
 * - La Entidad maneja la estructura relacional local (SQLite con Room).
 */
@Entity(tableName = "farmacias")
data class FarmaciaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val apiId: Int? = null,
    val nombre: String,
    val direccion: String,
    val telefono: String,
    val horario: String,
    val latitude: Double?,
    val longitude: Double?,
    val fechaRaw: String?,
    val fechaIso: String?,
    val diaSemana: String?,
    val imagen: String?,
    val fechaActualizacion: Long = System.currentTimeMillis()
)

// ============================================================================
// FUNCIONES DE MAPEO (DTO <-> ENTITY <-> UI MODEL)
// ============================================================================

/**
 * Convierte un objeto DTO que vino de la red (Farmacia) a una entidad de Room (FarmaciaEntity).
 */
fun Farmacia.toEntity(fechaActualizacion: Long = System.currentTimeMillis()): FarmaciaEntity {
    return FarmaciaEntity(
        id = 0, // 0 para permitir que Room genere automáticamente una clave única al insertar
        apiId = this.id,
        nombre = this.nombre ?: "",
        direccion = this.direccion ?: "",
        telefono = this.telefono ?: "",
        horario = this.horario ?: "",
        latitude = this.latitude,
        longitude = this.longitude,
        fechaRaw = this.fechaRaw,
        fechaIso = this.fechaIso,
        diaSemana = this.diaSemana,
        imagen = this.imagen,
        fechaActualizacion = fechaActualizacion
    )
}

/**
 * Convierte una lista de DTOs en una lista de entidades para Room.
 */
fun List<Farmacia>.toEntityList(fechaActualizacion: Long = System.currentTimeMillis()): List<FarmaciaEntity> {
    return this.map { it.toEntity(fechaActualizacion) }
}

/**
 * Convierte una entidad de Room al modelo que utiliza la interfaz de usuario (Farmacia).
 * Mantiene desacoplada la capa de presentación de la base de datos.
 */
fun FarmaciaEntity.toUiModel(): Farmacia {
    return Farmacia(
        id = this.apiId ?: this.id.toInt(),
        nombre = this.nombre,
        direccion = this.direccion,
        telefono = this.telefono,
        horario = this.horario,
        latitude = this.latitude,
        longitude = this.longitude,
        fechaRaw = this.fechaRaw,
        fechaIso = this.fechaIso,
        diaSemana = this.diaSemana,
        imagen = this.imagen
    )
}

/**
 * Convierte una lista de entidades de Room en una lista de modelos para la UI.
 */
fun List<FarmaciaEntity>.toUiModelList(): List<Farmacia> {
    return this.map { it.toUiModel() }
}
