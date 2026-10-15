package com.example.buscaya.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buscaya.data.model.Farmacia
import com.example.buscaya.data.model.SearchResult
import com.example.buscaya.ui.components.BuscaYaLogo
import com.example.buscaya.ui.theme.BuscaYaDivider
import com.example.buscaya.ui.theme.BuscaYaGray
import com.example.buscaya.ui.theme.BuscaYaWhite
import com.example.buscaya.ui.theme.BuscaYaYellow
import com.example.buscaya.ui.theme.PharmacyBackground
import com.example.buscaya.ui.theme.PharmacyIconBlue
import com.example.buscaya.ui.theme.PharmacyIconGreen
import com.example.buscaya.ui.theme.PharmacyIconTeal
import com.example.buscaya.ui.theme.PharmacyPrimary
import com.example.buscaya.ui.theme.PharmacyPrimaryDark
import com.example.buscaya.ui.theme.PharmacyTextOnPrimary
import com.example.buscaya.ui.theme.PharmacyTextPrimary
import com.example.buscaya.ui.viewmodel.FarmaciaViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.buscaya.ui.viewmodel.FarmaciaUiState

@Composable
fun FarmaciaScreen(
    viewModel: FarmaciaViewModel = viewModel(
        factory = FarmaciaViewModel.provideFactory(LocalContext.current.applicationContext)
    )
) {
    // Recolectamos el estado de forma segura con el ciclo de vida (collectAsStateWithLifecycle)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val filtroFarmacia by viewModel.filtroFarmacia.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFarmacia by remember { mutableStateOf<Farmacia?>(null) }
    var selectedSearchResult by remember { mutableStateOf<SearchResult?>(null) }

    // Obtenemos la lista de farmacias según el estado actual emitido por Room
    val farmacias = when (val state = uiState) {
        is FarmaciaUiState.Exito -> state.farmacias
        is FarmaciaUiState.SinConexion -> state.farmaciasGuardadas
        is FarmaciaUiState.Error -> state.farmaciasGuardadas
        is FarmaciaUiState.Cargando -> emptyList()
    }

    // Las farmacias siempre se muestran completas, sin filtrar
    val filteredFarmacias = farmacias

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BuscaYaYellow)
    ) {
        // Encabezado con Logo y Buscador
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BuscaYaLogo()

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Villa del Rosario",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = PharmacyTextOnPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                },
                placeholder = {
                    Text("Buscar farmacia por nombre...", color = BuscaYaGray)
                },
                singleLine = true,
                shape = RoundedCornerShape(50),
                textStyle = androidx.compose.ui.text.TextStyle(color = PharmacyTextPrimary, fontSize = 16.sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BuscaYaWhite,
                    unfocusedContainerColor = BuscaYaWhite,
                    disabledContainerColor = BuscaYaWhite,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = PharmacyPrimaryDark,
                    focusedTextColor = PharmacyTextPrimary,
                    unfocusedTextColor = PharmacyTextPrimary
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { viewModel.buscarLocales(searchQuery) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.buscarLocales(searchQuery) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PharmacyPrimaryDark,
                    contentColor = BuscaYaWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Buscar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // Resultados de búsqueda
        if (searchResults.isNotEmpty() || isSearching) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(BuscaYaWhite)
            ) {
                Text(
                    text = "Resultados de Búsqueda",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmacyPrimaryDark,
                    modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
                )

                if (isSearching) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PharmacyPrimaryDark)
                    }
                } else if (searchResults.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        itemsIndexed(searchResults) { index, result ->
                            SearchResultItem(
                                result = result,
                                showDivider = index < searchResults.lastIndex,
                                onClick = { selectedSearchResult = result }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron resultados para '$searchQuery'",
                            color = BuscaYaGray,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Panel de farmacias - siempre visible
         Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(PharmacyBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp, start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Farmacias de Turno",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmacyPrimaryDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "(desde: 08:00 am hasta: 08:00 am del día siguiente)",
                    fontSize = 13.sp,
                    color = BuscaYaGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Botón "Actualizar" que fuerza el refresco desde la API y Room
                Button(
                    onClick = { viewModel.refrescar(forzar = true) },
                    enabled = !isRefreshing,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PharmacyPrimary,
                        contentColor = BuscaYaWhite
                    ),
                    modifier = Modifier.height(38.dp)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = BuscaYaWhite,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Actualizando...", fontSize = 13.sp, color = BuscaYaWhite)
                    } else {
                        Text("🔄 Actualizar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BuscaYaWhite)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Campo de búsqueda en tiempo real directo a SQLite/Room con LIKE (arriba del listado)
                TextField(
                    value = filtroFarmacia,
                    onValueChange = { nuevoTexto ->
                        viewModel.actualizarFiltroFarmacia(nuevoTexto)
                    },
                    placeholder = {
                        Text("Buscar farmacia por nombre...", color = BuscaYaGray, fontSize = 14.sp)
                    },
                    leadingIcon = {
                        Text("🔍", modifier = Modifier.padding(start = 12.dp))
                    },
                    trailingIcon = {
                        if (filtroFarmacia.isNotEmpty()) {
                            IconButton(onClick = { viewModel.actualizarFiltroFarmacia("") }) {
                                Text("✕", color = BuscaYaGray, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    textStyle = androidx.compose.ui.text.TextStyle(color = PharmacyTextPrimary, fontSize = 14.sp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BuscaYaWhite,
                        unfocusedContainerColor = BuscaYaWhite,
                        disabledContainerColor = BuscaYaWhite,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = PharmacyPrimaryDark,
                        focusedTextColor = PharmacyTextPrimary,
                        unfocusedTextColor = PharmacyTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Aviso si la app está operando offline mostrando datos de Room
            if (uiState is FarmaciaUiState.SinConexion) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3CD)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️ Sin conexión, mostrando datos guardados",
                            color = Color(0xFF856404),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    uiState is FarmaciaUiState.Cargando && filteredFarmacias.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = PharmacyPrimary
                        )
                    }
                    uiState is FarmaciaUiState.Error && filteredFarmacias.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Ocurrió un error al cargar los datos",
                                color = PharmacyPrimaryDark,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (uiState as FarmaciaUiState.Error).mensaje,
                                color = BuscaYaGray,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.refrescar(forzar = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyPrimary)
                            ) {
                                Text("Reintentar", color = BuscaYaWhite)
                            }
                        }
                    }
                    filteredFarmacias.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (filtroFarmacia.isNotBlank()) "No se encontraron farmacias con '$filtroFarmacia'" else "No hay farmacias de turno guardadas",
                                color = BuscaYaGray,
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp
                            )
                        }
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            itemsIndexed(filteredFarmacias) { index, farmacia ->
                                FarmaciaListItem(
                                    farmacia = farmacia,
                                    showDivider = index < filteredFarmacias.lastIndex,
                                    onClick = { selectedFarmacia = farmacia }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Modal de detalles de farmacia
    selectedFarmacia?.let { farmacia ->
        FarmaciaDetailModal(
            farmacia = farmacia,
            onDismiss = { selectedFarmacia = null }
        )
    }
    
    // Modal de detalles de resultado de búsqueda
    selectedSearchResult?.let { result ->
        SearchResultDetailModal(
            result = result,
            onDismiss = { selectedSearchResult = null }
        )
    }
}
@Composable
fun FarmaciaListItem(
    farmacia: Farmacia,
    showDivider: Boolean = true,
    onClick: () -> Unit = {}
) {
    // Obtener campos de forma segura manejando nulos de la API
    val name = farmacia.nombre ?: "Farmacia"
    val rawAddress = farmacia.direccion ?: ""
    
    // Separar dirección y teléfono de forma limpia y segura
    val parts = rawAddress.split("–", "-", "—")
    val address = parts.firstOrNull()?.trim() ?: rawAddress
    val phone = if (parts.size > 1) parts[1].trim() else ""

    // Generar iniciales del nombre de forma segura
    val words = name.trim().split("\\s+".toRegex())
    val iconText = if (words.size >= 2) {
        "${words[0].firstOrNull() ?: ""}${words[1].firstOrNull() ?: ""}"
    } else if (words.isNotEmpty() && words[0].isNotEmpty()) {
        words[0].take(2)
    } else {
        "FA"
    }.uppercase()

    // Color del icono basado en el hashCode del nombre para evitar depender del id que puede ser nulo o causar unboxing crashes
    val iconColor = when (Math.abs(name.hashCode()) % 3) {
        0 -> PharmacyIconGreen
        1 -> PharmacyIconTeal
        else -> PharmacyIconBlue
    }

    // Obtener fecha desde el API o fallback a la de hoy
    val diaSemana = farmacia.diaSemana ?: "Turno"
    
    // Intentar extraer el número de día de "fecha_raw" (e.g. "Sábado 13 de Junio 2026" -> "13")
    val dayNumber = remember(farmacia.fechaRaw) {
        val raw = farmacia.fechaRaw ?: ""
        val match = "\\d+".toRegex().find(raw)
        match?.value ?: Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString()
    }
    
    // Intentar extraer las 3 primeras letras del mes de "fecha_raw" (e.g. "Sábado 13 de Junio 2026" -> "Jun")
    val month = remember(farmacia.fechaRaw) {
        val raw = farmacia.fechaRaw ?: ""
        val partsRaw = raw.split(" ")
        if (partsRaw.size >= 4) {
            partsRaw[3].take(3).replaceFirstChar { it.uppercase() }
        } else {
            SimpleDateFormat("MMM", Locale("es", "ES")).format(Calendar.getInstance().time)
                .replaceFirstChar { it.uppercase() }.removeSuffix(".")
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen o Círculo con iniciales
            if (!farmacia.imagen.isNullOrBlank()) {
                AsyncImage(
                    model = farmacia.imagen,
                    contentDescription = "Logo $name",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconText,
                        color = BuscaYaWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Calendario / Fecha
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(76.dp)
            ) {
                Text(
                    text = diaSemana,
                    fontSize = 11.sp,
                    color = BuscaYaGray,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = dayNumber,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = PharmacyPrimary,
                    lineHeight = 34.sp
                )
                Text(
                    text = month,
                    fontSize = 11.sp,
                    color = BuscaYaGray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Detalles de la farmacia
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PharmacyTextPrimary,
                    lineHeight = 20.sp
                )
                Text(
                    text = address,
                    fontSize = 13.sp,
                    color = BuscaYaGray,
                    lineHeight = 18.sp
                )
                if (phone.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tel: $phone",
                        fontSize = 12.sp,
                        color = BuscaYaGray,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Horario: Turno 24 hs",
                    fontSize = 11.sp,
                    color = PharmacyPrimaryDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                thickness = 1.dp,
                color = BuscaYaDivider
            )
        }
    }
}

@Composable
fun FarmaciaDetailModal(
    farmacia: Farmacia,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(indication = null, interactionSource = null) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable(
                        enabled = false,
                        onClick = {},
                        indication = null,
                        interactionSource = null
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header Azul
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PharmacyPrimary)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Detalles de Farmacia",
                            color = BuscaYaWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = BuscaYaWhite
                            ),
                            modifier = Modifier.size(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("✕", fontSize = 20.sp)
                        }
                    }

                    // Contenido
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Imagen de la farmacia
                            if (!farmacia.imagen.isNullOrBlank()) {
                                AsyncImage(
                                    model = farmacia.imagen,
                                    contentDescription = "Logo ${farmacia.nombre}",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            
                            // Nombre grande
                            Text(
                                text = farmacia.nombre?.uppercase() ?: "FARMACIA",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyPrimary,
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = "FARMACIA",
                                fontSize = 12.sp,
                                color = PharmacyPrimaryDark,
                                textAlign = TextAlign.Center,
                                letterSpacing = 1.5.sp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Card con info
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = PharmacyBackground
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    // Turno
                                    if (!farmacia.diaSemana.isNullOrBlank()) {
                                        Text(
                                            text = "TURNO: ${farmacia.diaSemana}",
                                            fontSize = 11.sp,
                                            color = Color.Red,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                    
                                    // Nombre
                                    Text(
                                        text = farmacia.nombre ?: "Farmacia",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyPrimary,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    
                                    // Dirección (completa con teléfono)
                                    if (!farmacia.direccion.isNullOrBlank()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "📍",
                                                fontSize = 18.sp,
                                                modifier = Modifier.padding(end = 10.dp, top = 2.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = farmacia.direccion ?: "",
                                                    fontSize = 13.sp,
                                                    color = PharmacyTextPrimary,
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Fecha (si disponible)
                                    if (!farmacia.fechaRaw.isNullOrBlank()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "📅",
                                                fontSize = 18.sp,
                                                modifier = Modifier.padding(end = 10.dp, top = 2.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = farmacia.fechaRaw ?: "",
                                                    fontSize = 13.sp,
                                                    color = PharmacyTextPrimary,
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    result: SearchResult,
    showDivider: Boolean = true,
    onClick: () -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!result.imagen.isNullOrBlank()) {
                AsyncImage(
                    model = result.imagen,
                    contentDescription = result.nombre,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PharmacyBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏢", fontSize = 28.sp)
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = result.nombre ?: "Local",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PharmacyTextPrimary,
                    lineHeight = 20.sp
                )
                Text(
                    text = result.direccion ?: "Sin dirección",
                    fontSize = 13.sp,
                    color = BuscaYaGray,
                    lineHeight = 18.sp
                )
                if (!result.tipo.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tipo: ${result.tipo}",
                        fontSize = 11.sp,
                        color = PharmacyPrimaryDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                thickness = 1.dp,
                color = BuscaYaDivider
            )
        }
    }
}

@Composable
fun SearchResultDetailModal(
    result: SearchResult,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(indication = null, interactionSource = null) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clickable(
                        enabled = false,
                        onClick = {},
                        indication = null,
                        interactionSource = null
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PharmacyPrimary)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Detalle del Local",
                            color = BuscaYaWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = BuscaYaWhite
                            ),
                            modifier = Modifier.size(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("✕", fontSize = 20.sp)
                        }
                    }

                    // Contenido
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Imagen
                            if (!result.imagen.isNullOrBlank()) {
                                AsyncImage(
                                    model = result.imagen,
                                    contentDescription = "Imagen ${result.nombre}",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            
                            // Nombre
                            Text(
                                text = result.nombre?.uppercase() ?: "LOCAL",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = PharmacyPrimary,
                                textAlign = TextAlign.Center
                            )
                            
                            // Negocio nombre
                            if (!result.negocioNombre.isNullOrBlank()) {
                                Text(
                                    text = result.negocioNombre,
                                    fontSize = 14.sp,
                                    color = PharmacyPrimaryDark,
                                    textAlign = TextAlign.Center
                                )
                            }
                            
                            // Rating
                            if (result.rating != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "⭐ ${result.rating}",
                                    fontSize = 16.sp,
                                    color = BuscaYaGray,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Card con info
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = PharmacyBackground
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    // Descripción
                                    if (!result.descripcion.isNullOrBlank()) {
                                        Text(
                                            text = result.descripcion,
                                            fontSize = 14.sp,
                                            color = PharmacyTextPrimary,
                                            lineHeight = 20.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                    
                                    // Dirección
                                    if (!result.direccion.isNullOrBlank()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "📍",
                                                fontSize = 18.sp,
                                                modifier = Modifier.padding(end = 10.dp, top = 2.dp)
                                            )
                                            Text(
                                                text = result.direccion,
                                                fontSize = 13.sp,
                                                color = PharmacyTextPrimary,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                    
                                    // Teléfono
                                    if (!result.telefono.isNullOrBlank()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "📞",
                                                fontSize = 18.sp,
                                                modifier = Modifier.padding(end = 10.dp, top = 2.dp)
                                            )
                                            Text(
                                                text = result.telefono,
                                                fontSize = 13.sp,
                                                color = PharmacyTextPrimary,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                    
                                    // Email
                                    if (!result.emailContacto.isNullOrBlank()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "✉️",
                                                fontSize = 18.sp,
                                                modifier = Modifier.padding(end = 10.dp, top = 2.dp)
                                            )
                                            Text(
                                                text = result.emailContacto,
                                                fontSize = 13.sp,
                                                color = PharmacyTextPrimary,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                    
                                    // Precio
                                    if (!result.precio.isNullOrBlank()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "💲",
                                                fontSize = 18.sp,
                                                modifier = Modifier.padding(end = 10.dp, top = 2.dp)
                                            )
                                            Text(
                                                text = result.precio,
                                                fontSize = 13.sp,
                                                color = PharmacyTextPrimary,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}
