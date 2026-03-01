package com.brm.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.brm.app.Search

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- TODO ESTO DEBE IR AQUÍ DENTRO ---
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        btnSearch.setOnClickListener {
            // Esto abre la nueva pantalla
            val intent = Intent(this, Search::class.java)
            startActivity(intent)
        }
        // -------------------------------------
    }
}