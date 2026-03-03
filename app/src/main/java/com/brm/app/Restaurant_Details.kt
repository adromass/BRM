package com.brm.app

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Restaurant_Details : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_details)

        // 1. Referenciamos los componentes de tu XML
        val ivRestaurant = findViewById<ImageView>(R.id.ivRestaurantDetail)
        val tvName = findViewById<TextView>(R.id.tvRestaurantNameDetail)
        val btnCall = findViewById<Button>(R.id.btnCall)

        // --- AQUÍ ESTÁ LA MAGIA ---
        // 2. Recibimos el dato enviado por el Adapter
        val restaurantName = intent.getStringExtra("RESTAURANT_NAME") ?: "Restaurante"

        // 3. Mostramos el nombre real
        tvName.text = restaurantName
        // --------------------------

        btnCall.setOnClickListener {
            Toast.makeText(this, "Llamando a $restaurantName...", Toast.LENGTH_SHORT).show()
        }
    }
}