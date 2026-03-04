package com.brm.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place // Usamos Place que es estándar
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest

class Restaurant_Details : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val restaurantId = intent.getStringExtra("RESTAURANT_ID") ?: ""

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DetailsScreen(restaurantId)
                }
            }
        }
    }
}

@Composable
fun DetailsScreen(restaurantId: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val placesClient = remember { Places.createClient(context) }

    var restaurantName by remember { mutableStateOf("Cargando...") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf<String?>(null) }
    var website by remember { mutableStateOf<String?>(null) }
    var rating by remember { mutableStateOf(0.0) }
    var userRatings by remember { mutableStateOf(0) }
    var priceLevel by remember { mutableStateOf(0) }

    var imageBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var cargandoImagen by remember { mutableStateOf(true) }

    LaunchedEffect(restaurantId) {
        if (restaurantId.isNotEmpty()) {
            val fields = listOf(
                Place.Field.DISPLAY_NAME,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.INTERNATIONAL_PHONE_NUMBER,
                Place.Field.WEBSITE_URI,
                Place.Field.RATING,
                Place.Field.USER_RATING_COUNT,
                Place.Field.PRICE_LEVEL,
                Place.Field.PHOTO_METADATAS
            )

            val request = FetchPlaceRequest.newInstance(restaurantId, fields)

            placesClient.fetchPlace(request)
                .addOnSuccessListener { response ->
                    val p = response.place
                    restaurantName = p.displayName ?: "Restaurante"
                    address = p.formattedAddress ?: ""
                    phone = p.internationalPhoneNumber
                    website = p.websiteUri?.toString()
                    rating = p.rating ?: 0.0
                    userRatings = p.userRatingCount ?: 0
                    priceLevel = p.priceLevel ?: 0

                    val photoMetadata = p.photoMetadatas?.firstOrNull()
                    if (photoMetadata != null) {
                        val photoRequest = FetchPhotoRequest.builder(photoMetadata)
                            .setMaxWidth(1000)
                            .setMaxHeight(600)
                            .build()

                        placesClient.fetchPhoto(photoRequest)
                            .addOnSuccessListener { photoResponse ->
                                imageBitmap = photoResponse.bitmap.asImageBitmap()
                                cargandoImagen = false
                            }
                            .addOnFailureListener { cargandoImagen = false }
                    } else {
                        cargandoImagen = false
                    }
                }
                .addOnFailureListener {
                    restaurantName = "Error de carga"
                    cargandoImagen = false
                }
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            if (cargandoImagen) {
                CircularProgressIndicator()
            } else if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // CAMBIO AQUÍ: Usamos Icons.Default.Place en lugar de Restaurant
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "Sin fotografía disponible",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = restaurantName,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.weight(1f)
                )
                if (priceLevel > 0) {
                    Text(
                        text = "$".repeat(priceLevel),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = " $rating ",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "($userRatings reseñas)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Text(
                text = "📍 $address",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val uri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.LocationOn, null)
                Spacer(Modifier.width(8.dp))
                Text("CÓMO LLEGAR")
            }

            if (!phone.isNullOrEmpty()) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Call, null)
                    Spacer(Modifier.width(8.dp))
                    Text("LLAMAR: $phone")
                }
            }

            if (!website.isNullOrEmpty()) {
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(website))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) {
                    Icon(Icons.Default.Info, null)
                    Spacer(Modifier.width(8.dp))
                    Text("VISITAR SITIO WEB")
                }
            }
        }
    }
}