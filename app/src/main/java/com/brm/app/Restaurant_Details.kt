package com.brm.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest

class Restaurant_Details : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val restaurantId = intent.getStringExtra("PLACE_ID") ?: intent.getStringExtra("RESTAURANT_ID") ?: ""

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    DetailsScreen(restaurantId)
                }
            }
        }
    }
}

@Composable
fun DetailsScreen(restaurantId: String) {
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }

    var restaurantName by remember { mutableStateOf("Cargando...") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf<String?>(null) }
    var website by remember { mutableStateOf<String?>(null) }
    var rating by remember { mutableDoubleStateOf(0.0) }
    var userRatings by remember { mutableIntStateOf(0) }
    var priceLevel by remember { mutableIntStateOf(0) }
    var imageList by remember { mutableStateOf<List<androidx.compose.ui.graphics.ImageBitmap>>(emptyList()) }
    var latLng by remember { mutableStateOf<com.google.android.gms.maps.model.LatLng?>(null) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(restaurantId) {
        if (restaurantId.isNotEmpty()) {
            val fields = listOf(
                Place.Field.DISPLAY_NAME,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.NATIONAL_PHONE_NUMBER,
                Place.Field.INTERNATIONAL_PHONE_NUMBER,
                Place.Field.WEBSITE_URI,
                Place.Field.RATING,
                Place.Field.USER_RATING_COUNT,
                Place.Field.PRICE_LEVEL,
                Place.Field.PHOTO_METADATAS,
                Place.Field.LOCATION 
            )

            val request = FetchPlaceRequest.newInstance(restaurantId, fields)

            placesClient.fetchPlace(request).addOnSuccessListener { response ->
                val p = response.place
                restaurantName = p.displayName ?: "Restaurante"
                address = p.formattedAddress ?: ""
                phone = p.nationalPhoneNumber ?: p.internationalPhoneNumber
                website = p.websiteUri?.toString()
                rating = p.rating ?: 0.0
                userRatings = p.userRatingCount ?: 0
                priceLevel = p.priceLevel ?: 0
                latLng = p.location

                p.photoMetadatas?.take(5)?.forEach { metadata ->
                    val photoRequest = FetchPhotoRequest.builder(metadata).setMaxWidth(1200).build()
                    placesClient.fetchPhoto(photoRequest).addOnSuccessListener { photoResp ->
                        imageList = imageList + photoResp.bitmap.asImageBitmap()
                    }
                }
                cargando = false
            }.addOnFailureListener { cargando = false }
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            if (imageList.isNotEmpty()) {
                LazyRow(modifier = Modifier.fillMaxSize()) {
                    items(imageList) { bitmap ->
                        Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            modifier = Modifier.fillParentMaxWidth().fillMaxHeight(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Box(Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Restaurant, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                }
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = restaurantName, 
                style = MaterialTheme.typography.displaySmall, 
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                Text(text = " $rating ($userRatings reseñas)", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                if (priceLevel > 0) {
                    Text(text = " • " + "$".repeat(priceLevel), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionCircle(Icons.Default.Call, "Llamar", !phone.isNullOrEmpty()) {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                }
                ActionCircle(Icons.Default.Language, "Web", !website.isNullOrEmpty()) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(website)))
                }
                ActionCircle(Icons.Default.Directions, "Ir", true) {
                    val destination = if (latLng != null) "${latLng?.latitude},${latLng?.longitude}" else Uri.encode(address)
                    val uri = Uri.parse("google.navigation:q=$destination")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") })
                }
                ActionCircle(Icons.Default.Share, "Compartir", true) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "¡Mira este restaurante! $restaurantName en $address")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Compartir usando"))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

            Text(text = "Ubicación", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = address, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
            
            Spacer(modifier = Modifier.height(8.dp))

            // Coherencia: 28.dp para contenedores grandes (Mapa)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (latLng != null) {
                    val staticMapUrl = "https://maps.googleapis.com/maps/api/staticmap?" +
                            "center=${latLng?.latitude},${latLng?.longitude}" +
                            "&zoom=15" +
                            "&size=600x400" +
                            "&markers=color:red%7C${latLng?.latitude},${latLng?.longitude}" +
                            "&key=${BuildConfig.MAPS_API_KEY}"
                    
                    AsyncImage(
                        model = staticMapUrl,
                        contentDescription = "Mapa",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ActionCircle(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    val contentColor = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)
    val containerColor = if (enabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.LightGray.copy(alpha = 0.2f)
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape) // Botones redondos (estándar Google)
                .background(containerColor)
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = if (enabled) Color.Unspecified else Color.Gray)
    }
}
