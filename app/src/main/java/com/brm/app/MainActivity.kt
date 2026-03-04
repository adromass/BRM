package com.brm.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

class MainActivity : ComponentActivity() {

    private var cityText by mutableStateOf("")

    private val startAutocomplete = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            if (intent != null) {
                val place = Autocomplete.getPlaceFromIntent(intent)
                cityText = place.displayName ?: ""
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainContent(
                        currentCity = cityText,
                        onCityClick = { launchAutocomplete() },
                        onSearchClick = { ciudad, rango, comida, precio, rating, nearMe ->
                            val intent = Intent(this, Restaurant_List::class.java).apply {
                                putExtra("LOCATION_QUERY", ciudad)
                                putExtra("SEARCH_RANGE", rango)
                                putExtra("FOOD_TYPE", comida)
                                putExtra("PRICE_LEVEL", precio)
                                putExtra("MIN_RATING", rating)
                                putExtra("NEAR_ME", nearMe)
                            }
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    private fun launchAutocomplete() {
        val fields = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setTypesFilter(listOf("(regions)"))
            .build(this)
        startAutocomplete.launch(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    currentCity: String,
    onCityClick: () -> Unit,
    onSearchClick: (String, Float, String, Int, Float, Boolean) -> Unit
) {
    var rangeValue by remember { mutableFloatStateOf(5f) }
    var selectedFood by remember { mutableStateOf("Cualquiera") }
    var selectedPrice by remember { mutableIntStateOf(0) }
    var selectedRating by remember { mutableFloatStateOf(0f) }
    var expanded by remember { mutableStateOf(false) }

    val foodOptions = listOf("Cualquiera", "Pizza", "Sushi", "Mexicana", "Hamburguesas", "Italiana", "Café")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Explorar Restaurantes", style = MaterialTheme.typography.headlineMedium)

        // 1. Ciudad
        OutlinedTextField(
            value = currentCity,
            onValueChange = {},
            label = { Text("¿En qué ciudad buscas?") },
            modifier = Modifier.fillMaxWidth().clickable { onCityClick() },
            enabled = false,
            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // 2. Comida
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedFood,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de comida") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                foodOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { selectedFood = option; expanded = false }
                    )
                }
            }
        }

        // 3. Rating
        Column {
            Text("Rating mínimo: ${if(selectedRating == 0f) "Cualquiera" else "$selectedRating+ ⭐"}", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3.0f, 4.0f, 4.5f).forEach { rating ->
                    FilterChip(
                        selected = selectedRating == rating,
                        onClick = { selectedRating = if (selectedRating == rating) 0f else rating },
                        label = { Text("$rating ⭐") }
                    )
                }
            }
        }

        // 4. Precio
        Column {
            Text("Presupuesto: ${if(selectedPrice == 0) "Cualquiera" else "$".repeat(selectedPrice)}", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2, 3).forEach { level ->
                    FilterChip(
                        selected = selectedPrice == level,
                        onClick = { selectedPrice = if (selectedPrice == level) 0 else level },
                        label = { Text("$".repeat(level)) }
                    )
                }
            }
        }

        // 5. Radio
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Distancia: ${rangeValue.toInt()} km")
                Slider(value = rangeValue, onValueChange = { rangeValue = it }, valueRange = 1f..15f)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { onSearchClick(currentCity, rangeValue, selectedFood, selectedPrice, selectedRating, false) },
            modifier = Modifier.fillMaxWidth(),
            enabled = currentCity.isNotEmpty()
        ) {
            Text("Buscar en Ciudad")
        }

        OutlinedButton(
            onClick = { onSearchClick("", rangeValue, selectedFood, selectedPrice, selectedRating, true) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📍 Buscar Cerca de Mí")
        }
    }
}