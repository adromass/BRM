package com.brm.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current

    // Estados
    var ciudadTexto by remember { mutableStateOf("") }
    var rangoKm by remember { mutableFloatStateOf(10f) }
    var nivelPrecio by remember { mutableIntStateOf(0) }
    var tipoComida by remember { mutableStateOf("Cualquiera") }
    var expanded by remember { mutableStateOf(false) }

    val categoriasComida = listOf(
        "Cualquiera", "Italiana", "Mexicana", "Japonesa", "Hamburguesas",
        "Steakhouse", "Mariscos", "Vegetariana", "China", "Cafetería", "Pizzería"
    )

    val startAutocomplete = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            ciudadTexto = place.displayName ?: ""
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            val intent = Intent(context, Restaurant_List::class.java).apply {
                putExtra("NEAR_ME", true)
                putExtra("SEARCH_RANGE", rangoKm)
                putExtra("FOOD_TYPE", tipoComida)
                putExtra("PRICE_LEVEL", nivelPrecio)
                // Eliminado putExtra de Rating
            }
            context.startActivity(intent)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bajamos el contenido para no chocar con la status bar
        Spacer(modifier = Modifier.height(64.dp))

        Text(
            "BRM App",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Buscador de ciudades
        OutlinedTextField(
            value = ciudadTexto,
            onValueChange = { },
            label = { Text("¿En qué ciudad buscas?") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val fields = listOf(Place.Field.ID, Place.Field.DISPLAY_NAME)
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .setTypesFilter(listOf("(regions)"))
                        .build(context)
                    startAutocomplete.launch(intent)
                },
            enabled = false,
            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        // Selector de tipo de comida
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = tipoComida,
                onValueChange = {},
                readOnly = true,
                label = { Text("¿Qué comeremos?") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                categoriasComida.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = { tipoComida = opcion; expanded = false }
                    )
                }
            }
        }

        // Card de Ajustes (Rango y Precio)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {

                // RANGO: 5 a 20 km (pasos de 5)
                Column {
                    Text("Rango de búsqueda: ${rangoKm.toInt()} km", style = MaterialTheme.typography.labelLarge)
                    Slider(
                        value = rangoKm,
                        onValueChange = { rangoKm = it },
                        valueRange = 5f..20f,
                        steps = 2 // 5, 10, 15, 20
                    )
                }

                // PRESUPUESTO
                Column {
                    Text("Presupuesto sugerido", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..4).forEach { nivel ->
                            FilterChip(
                                modifier = Modifier.padding(horizontal = 4.dp), // Espacio entre chips
                                selected = nivelPrecio == nivel,
                                onClick = { nivelPrecio = if (nivelPrecio == nivel) 0 else nivel },
                                label = { Text("$".repeat(nivel)) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BOTONES HORIZONTALES (A la par)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f).height(56.dp),
                onClick = {
                    val intent = Intent(context, Restaurant_List::class.java).apply {
                        putExtra("NEAR_ME", false)
                        putExtra("LOCATION_QUERY", ciudadTexto)
                        putExtra("SEARCH_RANGE", rangoKm)
                        putExtra("FOOD_TYPE", tipoComida)
                        putExtra("PRICE_LEVEL", nivelPrecio)
                    }
                    context.startActivity(intent)
                },
                enabled = ciudadTexto.isNotBlank(),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Ciudad", fontWeight = FontWeight.Bold)
            }

            Button(
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                onClick = { permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)) },
                shape = MaterialTheme.shapes.large
            ) {
                Text("Cerca de mí", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}