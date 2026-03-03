package com.brm.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Search : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        // Ajuste de insets (esto ya lo tenías)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- TUS IDs CORRECTOS ---
        val etSearchLocation = findViewById<EditText>(R.id.etSearchLocation)
        val btnConfirmSearch = findViewById<Button>(R.id.btnConfirmSearch)

        btnConfirmSearch.setOnClickListener {
            val query = etSearchLocation.text.toString()
            if (query.isNotEmpty()) {
                val intent = Intent(this, Restaurant_List::class.java)
                intent.putExtra("SEARCH_TYPE", "CITY")
                intent.putExtra("LOCATION_QUERY", query)
                startActivity(intent)
            } else {
                etSearchLocation.error = "Escribe una ciudad"
            }
        }
        // -------------------------
    }
}