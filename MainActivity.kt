package com.example.payremider

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnIniciar: MaterialButton
    private lateinit var button2: MaterialButton
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("PayReminderPrefs", MODE_PRIVATE)

        if (prefs.getBoolean("isLoggedIn", false)) {
            irAlHome()
            return
        }

        initViews()
        setupListeners()
    }

    private fun initViews() {
        tilEmail    = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etEmail     = findViewById(R.id.etEmail)
        etPassword  = findViewById(R.id.etPassword)
        btnIniciar  = findViewById(R.id.btnIniciar)
        button2     = findViewById(R.id.button2)
    }

    private fun setupListeners() {
        btnIniciar.setOnClickListener {
            if (validarFormulario()) iniciarSesion()
        }

        button2.setOnClickListener {
            startActivity(Intent(this, Registrar::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun validarFormulario(): Boolean {
        var esValido = true
        val email    = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        tilEmail.error    = null
        tilPassword.error = null

        if (TextUtils.isEmpty(email)) {
            tilEmail.error = "Ingresa tu correo electrónico"
            esValido = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Correo electrónico no válido"
            esValido = false
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.error = "Ingresa tu contraseña"
            esValido = false
        } else if (password.length < 8) {
            tilPassword.error = "La contraseña debe tener al menos 8 caracteres"
            esValido = false
        }

        return esValido
    }

    private fun iniciarSesion() {
        val email    = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        val emailGuardado    = prefs.getString("userEmail", "")
        val passwordGuardada = prefs.getString("userPassword", "")

        if (email == emailGuardado && password == passwordGuardada) {
            prefs.edit().putBoolean("isLoggedIn", true).apply()
            val nombre = prefs.getString("userName", "Usuario")
            Snackbar.make(btnIniciar, "¡Bienvenido de vuelta, $nombre!", Snackbar.LENGTH_SHORT).show()
            irAlHome()
        } else {
            tilPassword.error = "Correo o contraseña incorrectos"
            Snackbar.make(btnIniciar,
                "Credenciales incorrectas. ¿Aún no tienes cuenta?",
                Snackbar.LENGTH_LONG
            ).setAction("Regístrate") {
                startActivity(Intent(this, Registrar::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }.show()
        }
    }

    private fun irAlHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        overridePendingTransition(R.anim.fade_in_up, R.anim.slide_out_left)
        finishAffinity()
    }
}