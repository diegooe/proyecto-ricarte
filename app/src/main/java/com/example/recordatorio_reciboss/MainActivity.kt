package com.example.recordatorio_reciboss

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnAgregar = findViewById<Button>(R.id.btnAgregar)

        btnAgregar.setOnClickListener {
            Toast.makeText(this, "Aquí irá el formulario", Toast.LENGTH_SHORT).show()
        }
    }
}