package com.brm.app

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.SearchByTextRequest

enum class SortOption(val label: String) {
    RELEVANCIA("Sugeridos"), RATING("Mejor Rating"), POPULARIDAD("Más Populares"), PRECIO_BAJO("Más Baratos")
}

data class RestaurantData(
    val id: String, val name: String, val rating: Double, val userRatings: Int,
    val priceLevel: Int, val foodType: String, val photoMetadata: com.google.android.libraries.places.api.model.PhotoMetadata?,
    val distanceText: String = "", val distanceInMeters: Float = 0f
)

class Restaurant_List : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ciudad = intent.getStringExtra("LOCATION_QUERY") ?: ""
        val lat = intent.getDoubleExtra("LATITUDE", 0.0)
        val lon = intent.getDoubleExtra("LONGITUDE", 0.0)
        val rangoKm = intent.getFloatExtra("SEARCH_RANGE", 10f)
        val tipoComida = intent.getStringExtra("FOOD_TYPE") ?: "Cualquiera"
        val nivelPrecio = intent.getIntExtra("PRICE_LEVEL", 0)
        val esCercaDeMi = intent.getBooleanExtra("NEAR_ME", false)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ResultsScreen(ciudad, lat, lon, rangoKm, tipoComida, nivelPrecio, esCercaDeMi)
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(ciudad: String, cityLat: Double, cityLon: Double, rango: Float, comida: String, precioFiltrado: Int, gpsSolicitado: Boolean) {
    var listaBase by remember { mutableStateOf<List<RestaurantData>>(emptyList()) }
    var listaOrdenada by remember { mutableStateOf<List<RestaurantData>>(emptyList()) }
    var currentSort by remember { mutableStateOf(SortOption.RELEVANCIA) }
    var cargando by remember { mutableStateOf(true) }

    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        val client = Places.createClient(context)
        if (gpsSolicitado) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { loc ->
                if (loc != null) {
                    buscarPlaces(client, loc.latitude, loc.longitude, true, ciudad, comida, rango, precioFiltrado) {
                        listaBase = it
                        cargando = false
                    }
                } else { cargando = false }
            }.addOnFailureListener { cargando = false }
        } else {
            buscarPlaces(client, cityLat, cityLon, false, ciudad, comida, rango, precioFiltrado) {
                listaBase = it
                cargando = false
            }
        }
    }

    LaunchedEffect(currentSort, listaBase) {
        listaOrdenada = when (currentSort) {
            SortOption.RELEVANCIA -> if (gpsSolicitado || (cityLat != 0.0)) listaBase.sortedBy { it.distanceInMeters } else listaBase
            SortOption.RATING -> listaBase.sortedByDescending { it.rating }
            SortOption.POPULARIDAD -> listaBase.sortedByDescending { it.userRatings }
            SortOption.PRECIO_BAJO -> listaBase.sortedBy { if (it.priceLevel == 0) 5 else it.priceLevel }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Resultados", fontWeight = FontWeight.Bold) },
                actions = {
                    if (!cargando && listaBase.isNotEmpty()) {
                        IconButton(onClick = { showSheet = true }) { Icon(Icons.Default.List, "Ordenar") }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (listaOrdenada.isEmpty()) {
                        item {
                            Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No se encontraron resultados en esta zona.", color = Color.Gray)
                            }
                        }
                    }
                    items(listaOrdenada) { res -> RestaurantCard(res) }
                }
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(bottom = 32.dp, start = 16.dp, end = 16.dp)) {
                    Text("Ordenar por", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                    SortOption.entries.forEach { option ->
                        if (option == SortOption.PRECIO_BAJO && precioFiltrado > 0) return@forEach
                        ListItem(
                            headlineContent = { Text(if (option == SortOption.RELEVANCIA && (gpsSolicitado || cityLat != 0.0)) "Más cercanos" else option.label) },
                            leadingContent = { RadioButton(selected = currentSort == option, onClick = { currentSort = option; showSheet = false }) },
                            modifier = Modifier.clickable { currentSort = option; showSheet = false }
                        )
                    }
                }
            }
        }
    }
}

private fun buscarPlaces(client: com.google.android.libraries.places.api.net.PlacesClient, lat: Double, lon: Double, usaGps: Boolean, ciudad: String, comida: String, rango: Float, precio: Int, onResult: (List<RestaurantData>) -> Unit) {
    val query = if (comida == "Cualquiera") "restaurant" else "restaurant $comida"
    val fields = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.RATING, Place.Field.USER_RATING_COUNT, Place.Field.PRICE_LEVEL, Place.Field.TYPES, Place.Field.PHOTO_METADATAS, Place.Field.LOCATION)
    val radioMetros = (rango * 1000).toDouble()
    val req = SearchByTextRequest.builder(query, fields).setMaxResultCount(20)

    if (lat != 0.0 && lon != 0.0) {
        val bounds = CircularBounds.newInstance(LatLng(lat, lon), radioMetros)
        req.setLocationBias(bounds)
    }

    if (precio > 0) req.setPriceLevels(listOf(precio))

    client.searchByText(req.build()).addOnSuccessListener { resp ->
        val listaNegra = listOf("point_of_interest", "establishment", "food", "store", "restaurant")
        val finalResults = mutableListOf<RestaurantData>()

        resp.places.forEach { p ->
            var dMeters = 0f
            var dStr = ""

            if (p.location != null && lat != 0.0) {
                val start = Location("u").apply { latitude = lat; longitude = lon }
                val end = Location("r").apply { latitude = p.location!!.latitude; longitude = p.location!!.longitude }
                dMeters = start.distanceTo(end)
                dStr = String.format("%.1f km", dMeters / 1000f)
            }

            if (lat == 0.0 || dMeters <= radioMetros) {
                val etiquetaLimpia = p.placeTypes?.firstOrNull { t -> !listaNegra.contains(t.lowercase()) }?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() }
                val tipoAMostrar = if (comida == "Cualquiera" || etiquetaLimpia == null) "Gastronomía" else etiquetaLimpia

                finalResults.add(RestaurantData(p.id!!, p.displayName!!, p.rating ?: 0.0, p.userRatingCount ?: 0, p.priceLevel ?: 0, tipoAMostrar, p.photoMetadatas?.firstOrNull(), dStr, dMeters))
            }
        }
        onResult(finalResults)
    }.addOnFailureListener { onResult(emptyList()) }
}

@Composable
fun RestaurantCard(res: RestaurantData) {
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }
    var imageBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(res.photoMetadata) {
        res.photoMetadata?.let {
            placesClient.fetchPhoto(FetchPhotoRequest.builder(it).setMaxWidth(600).build()).addOnSuccessListener { resp ->
                imageBitmap = resp.bitmap.asImageBitmap()
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, Restaurant_Details::class.java).apply {
                    putExtra("PLACE_ID", res.id)
                }
                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(160.dp).background(Color.LightGray)) {
                imageBitmap?.let {
                    Image(bitmap = it, contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(res.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                    if (res.priceLevel > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer, 
                            shape = CircleShape,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("$".repeat(res.priceLevel), color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (res.distanceText.isNotEmpty()) {
                        Text(res.distanceText, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(res.foodType, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                    Text(" ${res.rating} (${res.userRatings})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
