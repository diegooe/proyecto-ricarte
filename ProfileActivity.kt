package com.example.payremider

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ProfileActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnVolver: Button
    private lateinit var ivAvatar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        prefs = getSharedPreferences("PayReminderPrefs", Context.MODE_PRIVATE)

        initViews()
        cargarDatos()
        setupListeners()
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnVolver = findViewById(R.id.btnVolver)
        ivAvatar = findViewById(R.id.ivAvatar)
    }

    private fun cargarDatos() {
        val userName = prefs.getString("userName", "Usuario")
        val userEmail = prefs.getString("userEmail", "correo@ejemplo.com")

        tvUserName.text = userName
        tvUserEmail.text = userEmail
    }

    private fun setupListeners() {
        btnEditProfile.setOnClickListener {
            Toast.makeText(this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
        }

        btnVolver.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        ivAvatar.setOnClickListener {
            Toast.makeText(this, "Cambiar foto - Próximamente", Toast.LENGTH_SHORT).show()
        }
    }
}