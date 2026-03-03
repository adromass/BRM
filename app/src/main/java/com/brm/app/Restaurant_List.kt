package com.brm.app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
// --- ESTE IMPORT ES EL CLAVE PARA NAME/ADDRESS ---
import com.google.android.libraries.places.api.model.Place.Field
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
// --- IMPORTS PARA FOTOS ---
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.net.FetchPhotoRequest
// Asegúrate de tener este import para tu API Key segura
import com.brm.app.BuildConfig

class Restaurant_List : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RestaurantAdapter
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_list)

        // 1. Inicializar API de Places de forma SEGURA
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }
        placesClient = Places.createClient(this)

        // 2. Configurar RecyclerView
        recyclerView = findViewById(R.id.rvRestaurants)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 3. Obtener la consulta de búsqueda
        val query = intent.getStringExtra("LOCATION_QUERY")
        if (query != null) {
            searchRestaurantsByText(query)
        } else {
            Toast.makeText(this, "No se encontró ubicación", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchRestaurantsByText(query: String) {
        // 4. Definir qué campos queremos obtener (incluyendo metadatos de foto)
        val placeFields = listOf(
            Field.ID,
            Field.DISPLAY_NAME,
            Field.FORMATTED_ADDRESS,
            Field.RATING,
            Field.PHOTO_METADATAS // --- IMPORTANTE PARA FOTOS ---
        )

        // 5. Crear la solicitud de búsqueda por texto
        val searchByTextRequest = SearchByTextRequest.builder("restaurantes en $query", placeFields)
            .setMaxResultCount(10)
            .build()

        // 6. Ejecutar la búsqueda
        placesClient.searchByText(searchByTextRequest)
            .addOnSuccessListener { response ->
                val restaurantList = mutableListOf<Restaurant>()

                for (place in response.places) {
                    val restaurant = Restaurant(
                        id = place.id ?: "",
                        name = place.displayName ?: "Sin nombre",
                        imageUrl = null,
                        address = place.formattedAddress ?: "Sin dirección",
                        rating = place.rating?.toFloat() ?: 0f
                        // bitmap inicial es null
                    )
                    restaurantList.add(restaurant)

                    // --- OBTENER FOTO SI EXISTE ---
                    val photoMetadata = place.photoMetadatas?.firstOrNull()
                    if (photoMetadata != null) {
                        fetchRestaurantPhoto(photoMetadata, restaurant)
                    }
                }

                // 7. Actualizar el adaptador (inicialmente sin fotos)
                adapter = RestaurantAdapter(restaurantList)
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.e("PlacesAPI", "Error en búsqueda: ${exception.message}")
                Toast.makeText(this, "Error buscando restaurantes", Toast.LENGTH_SHORT).show()
            }
    }

    // --- NUEVA FUNCIÓN PARA OBTENER LA FOTO ---
    private fun fetchRestaurantPhoto(photoMetadata: PhotoMetadata, restaurant: Restaurant) {
        val fetchPhotoRequest = FetchPhotoRequest.builder(photoMetadata)
            .setMaxWidth(500) // Redimensionar para ahorrar memoria
            .setMaxHeight(300)
            .build()

        placesClient.fetchPhoto(fetchPhotoRequest)
            .addOnSuccessListener { fetchPhotoResponse ->
                val bitmap = fetchPhotoResponse.bitmap
                // --- AQUÍ TIENES EL BITMAP DE LA FOTO ---

                // --- CONEXIÓN CON EL ADAPTADOR ---
                runOnUiThread { // Asegurar que sea en el hilo principal
                    adapter.updateRestaurantPhoto(restaurant.id, bitmap)
                }
                Log.d("PlacesAPI", "Foto cargada para: ${restaurant.name}")
            }
            .addOnFailureListener { exception ->
                Log.e("PlacesAPI", "Error cargando foto: ${exception.message}")
            }
    }
}