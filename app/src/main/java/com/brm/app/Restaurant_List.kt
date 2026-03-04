package com.brm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.SearchByTextRequest
import java.util.Locale
import androidx.compose.ui.graphics.asImageBitmap
import android.content.Intent
import androidx.compose.foundation.clickable

data class RestaurantData(
    val id: String,
    val name: String,
    val rating: Double,
    val userRatings: Int,
    val priceLevel: Int,
    val address: String,
    val foodType: String,
    val photoMetadata: com.google.android.libraries.places.api.model.PhotoMetadata?
)

class Restaurant_List : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ciudad = intent.getStringExtra("LOCATION_QUERY") ?: ""
        val rangoKm = intent.getFloatExtra("SEARCH_RANGE", 5f)
        val tipoComida = intent.getStringExtra("FOOD_TYPE") ?: "Cualquiera"
        val nivelPrecio = intent.getIntExtra("PRICE_LEVEL", 0)
        val ratingMinimo = intent.getFloatExtra("MIN_RATING", 0f)
        val esCercaDeMi = intent.getBooleanExtra("NEAR_ME", false)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    ResultsScreen(ciudad, rangoKm, tipoComida, nivelPrecio, ratingMinimo, esCercaDeMi)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(ciudad: String, rango: Float, comida: String, precio: Int, minRating: Float, gps: Boolean) {
    var restaurantes by remember { mutableStateOf<List<RestaurantData>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        val placesClient = Places.createClient(context)
        val term = if (comida == "Cualquiera") "restaurantes" else "restaurantes de $comida"
        val query = if (gps) "$term cerca de mí" else "$term en $ciudad"

        // Agregamos PHOTO_METADATA, TYPES y FORMATTED_ADDRESS
        val fields = listOf(
            Place.Field.ID,
            Place.Field.DISPLAY_NAME,
            Place.Field.RATING,
            Place.Field.USER_RATING_COUNT,
            Place.Field.PRICE_LEVEL,
            Place.Field.FORMATTED_ADDRESS,
            Place.Field.TYPES,
            Place.Field.PHOTO_METADATAS
        )

        val requestBuilder = SearchByTextRequest.builder(query, fields).setMaxResultCount(15)
        if (precio > 0) requestBuilder.setPriceLevels(listOf(precio))

        placesClient.searchByText(requestBuilder.build())
            .addOnSuccessListener { response ->
                restaurantes = response.places
                    .filter { (it.rating ?: 0.0).toFloat() >= minRating }
                    .sortedByDescending { it.rating ?: 0.0 }
                    .map {
                        RestaurantData(
                            id = it.id ?: "",
                            name = it.displayName ?: "Desconocido",
                            rating = it.rating ?: 0.0,
                            userRatings = it.userRatingCount ?: 0,
                            priceLevel = it.priceLevel ?: 0,
                            address = it.formattedAddress ?: "",
                            // Tomamos el primer tipo de comida que no sea "restaurant"
                            foodType = it.placeTypes?.firstOrNull { t -> 
                                t != "restaurant" && t != "food" && t != "point_of_interest" 
                            }?.replace("_", " ")?.lowercase(Locale.getDefault())?.replaceFirstChar { char -> char.uppercase() } ?: "Restaurante",
                            photoMetadata = it.photoMetadatas?.firstOrNull()
                        )
                    }
                cargando = false
            }
            .addOnFailureListener { cargando = false }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Descubre") }) }) { padding ->
        if (cargando) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(restaurantes) { res -> RestaurantCard(res) }
            }
        }
    }
}

@Composable
fun RestaurantCard(res: RestaurantData) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val placesClient = remember { Places.createClient(context) }

    var imageBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var falloCarga by remember { mutableStateOf(false) }

    LaunchedEffect(res.photoMetadata) {
        if (res.photoMetadata != null) {
            val photoRequest = FetchPhotoRequest.builder(res.photoMetadata)
                .setMaxWidth(500)
                .setMaxHeight(300)
                .build()

            placesClient.fetchPhoto(photoRequest)
                .addOnSuccessListener { response ->
                    imageBitmap = response.bitmap.asImageBitmap()
                }
                .addOnFailureListener { falloCarga = true }
        } else {
            falloCarga = true
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, Restaurant_Details::class.java).apply {
                    putExtra("RESTAURANT_ID", res.id)
                }
                context.startActivity(intent)
            }, // Coma necesaria aquí para separar el modifier de los otros parámetros
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // 1. ÁREA DE IMAGEN
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color.LightGray)
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap!!,
                        contentDescription = "Imagen de ${res.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (falloCarga) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Imagen no disponible", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            // 2. ÁREA DE TEXTO
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = res.name,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (res.priceLevel > 0) {
                        Text(
                            text = "$".repeat(res.priceLevel),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Text(
                    text = res.foodType,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(text = " ${res.rating} ", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "(${res.userRatings} reseñas)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                val ubicacionCorta = res.address.split(",").take(2).joinToString(",")
                Text(
                    text = "📍 $ubicacionCorta",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1
                )
            }
        }
    }
}