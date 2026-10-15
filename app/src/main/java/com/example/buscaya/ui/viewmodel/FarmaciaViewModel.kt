package com.example.buscaya.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.buscaya.data.local.AppDatabase
import com.example.buscaya.data.model.Farmacia
import com.example.buscaya.data.model.SearchResult
import com.example.buscaya.data.repository.FarmaciaRepository
import com.example.buscaya.data.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest

/**
 * Estados posibles de la pantalla de Farmacias representados mediante una interfaz sellada.
 *
 * Principio: Single Source of Truth + Manejo explícito de conectividad offline.
 */
sealed interface FarmaciaUiState {
    /**
     * La aplicación está cargando o sincronizando datos por primera vez y aún no hay datos listos.
     */
    object Cargando : FarmaciaUiState

    /**
     * Datos sincronizados con la API remota y persistidos correctamente en la base de datos Room.
     */
    data class Exito(val farmacias: List<Farmacia>) : FarmaciaUiState

    /**
     * No hay conexión a internet o el servidor no respondió, pero se presentan
     * los datos previamente guardados en Room.
     */
    data class SinConexion(val farmaciasGuardadas: List<Farmacia>) : FarmaciaUiState

    /**
     * Ocurrió un error inesperado. Si había datos guardados, se conservan para no dejar vacía la UI.
     */
    data class Error(val mensaje: String, val farmaciasGuardadas: List<Farmacia> = emptyList()) : FarmaciaUiState
}

/**
 * ViewModel para gestionar el estado de Farmacias y Búsqueda de locales.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FarmaciaViewModel(
    private val repository: FarmaciaRepository,
    private val searchRepository: SearchRepository = SearchRepository()
) : ViewModel() {

    // Estado principal de la UI para farmacias (expuesto como StateFlow inmutable)
    private val _uiState = MutableStateFlow<FarmaciaUiState>(FarmaciaUiState.Cargando)
    val uiState: StateFlow<FarmaciaUiState> = _uiState.asStateFlow()

    // Indicador de recarga (Swipe-to-refresh o botón de refrescar)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Filtro de búsqueda en la base de datos local (Room) usando LIKE parametrizado
    private val _filtroFarmacia = MutableStateFlow("")
    val filtroFarmacia: StateFlow<String> = _filtroFarmacia.asStateFlow()

    // Búsqueda de comercios/locales (se mantiene funcionalidad existente)
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    init {
        // Observamos reactivamente la búsqueda en Room:
        // Cada vez que cambia _filtroFarmacia (al escribir el usuario) o cambia la base de datos,
        // Room ejecuta la consulta SQL con LIKE y emite la lista en tiempo real.
        // NUNCA filtramos la lista en memoria.
        viewModelScope.launch {
            _filtroFarmacia
                .flatMapLatest { query ->
                    repository.buscarPorNombre(query.trim())
                }
                .collect { listaFiltrada ->
                    val estadoActual = _uiState.value
                    when (estadoActual) {
                        is FarmaciaUiState.SinConexion -> {
                            _uiState.value = FarmaciaUiState.SinConexion(listaFiltrada)
                        }
                        is FarmaciaUiState.Error -> {
                            _uiState.value = FarmaciaUiState.Error(estadoActual.mensaje, listaFiltrada)
                        }
                        else -> {
                            _uiState.value = FarmaciaUiState.Exito(listaFiltrada)
                        }
                    }
                }
        }

        // Al iniciar, intentamos sincronizar con la API si la BD está vacía
        refrescar(forzar = false)
    }

    /**
     * Actualiza el término de búsqueda de farmacias en tiempo real.
     * Al cambiar este valor, el Flow conectado al DAO ejecuta la query SQL con LIKE.
     */
    fun actualizarFiltroFarmacia(query: String) {
        _filtroFarmacia.value = query
    }

    /**
     * Sincroniza los datos con la API remota.
     * Si [forzar] es true, consulta la API aunque ya existan datos en Room.
     */
    fun refrescar(forzar: Boolean = false) {
        viewModelScope.launch {
            _isRefreshing.value = true
            Log.d("FarmaciaVM", "Iniciando refrescar(forzar=$forzar)...")

            val resultado = repository.refrescar(forzar)
            _isRefreshing.value = false

            resultado.fold(
                onSuccess = {
                    val guardadas = repository.obtenerGuardadas()
                    Log.d("FarmaciaVM", "Refresco exitoso: ${guardadas.size} farmacias.")
                    _uiState.value = FarmaciaUiState.Exito(guardadas)
                },
                onFailure = { error ->
                    val guardadas = repository.obtenerGuardadas()
                    Log.w("FarmaciaVM", "Fallo al refrescar: ${error.message}. Locales en BD: ${guardadas.size}")

                    if (esFallaDeConexion(error)) {
                        _uiState.value = FarmaciaUiState.SinConexion(guardadas)
                    } else {
                        _uiState.value = FarmaciaUiState.Error(
                            mensaje = error.message ?: "Error al sincronizar datos",
                            farmaciasGuardadas = guardadas
                        )
                    }
                }
            )
        }
    }

    /**
     * Determina si la excepción ocurrió por falta de conectividad / red inaccesible.
     */
    private fun esFallaDeConexion(e: Throwable): Boolean {
        return e is IOException ||
                e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                e.message?.contains("No address associated with hostname", ignoreCase = true) == true ||
                e.message?.contains("connect", ignoreCase = true) == true ||
                e.message?.contains("timeout", ignoreCase = true) == true
    }

    /**
     * Búsqueda híbrida de comercios mediante el SearchRepository.
     */
    fun buscarLocales(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _searchError.value = null
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null
            Log.d("FarmaciaVM", "Buscando locales: $query")
            try {
                val results = searchRepository.hybridSearch(query)
                Log.d("FarmaciaVM", "Resultados de búsqueda: ${results.size}")
                _searchResults.value = results
            } catch (e: Exception) {
                Log.e("FarmaciaVM", "Error al buscar comercios: ${e.message}", e)
                _searchError.value = e.message
            } finally {
                _isSearching.value = false
            }
        }
    }

    companion object {
        /**
         * Método helper para construir la fábrica simple sin Hilt.
         */
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            FarmaciaViewModelFactory(context)
    }
}

/**
 * ViewModelProvider.Factory simple para instanciar FarmaciaViewModel con sus dependencias
 * sin necesidad de librerías de inyección como Hilt.
 */
class FarmaciaViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FarmaciaViewModel::class.java)) {
            val database = AppDatabase.getInstance(context)
            val repository = FarmaciaRepository(database)
            val searchRepository = SearchRepository()
            return FarmaciaViewModel(repository, searchRepository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}
