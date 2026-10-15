package com.example.buscaya.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.buscaya.data.local.dao.FarmaciaDao
import com.example.buscaya.data.local.entity.FarmaciaEntity

/**
 * Base de datos principal de la aplicación mediante Room (SQLite abstraction).
 *
 * Implementa el patrón Singleton mediante [getInstance] para asegurar
 * que sólo exista una única instancia de la base de datos abierta a la vez
 * en todo el ciclo de vida de la aplicación.
 */
@Database(
    entities = [FarmaciaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun farmaciaDao(): FarmaciaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "buscaya_database"
                )
                    // NOTA DIDÁCTICA / IMPORTANTE:
                    // fallbackToDestructiveMigration descarta todas las tablas y recrea
                    // la base de datos desde cero si cambia la versión sin una migración definida.
                    // Esto se utiliza ÚNICAMENTE para entornos de desarrollo y pruebas.
                    // En entornos de producción reales, deben implementarse migraciones explícitas
                    // (Migration) para no eliminar los datos existentes de los usuarios.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
