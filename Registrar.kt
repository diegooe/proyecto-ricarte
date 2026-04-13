package com.example.payremider

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class Registrar : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tilNombre: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilContra: TextInputLayout
    private lateinit var tilConfirmar: TextInputLayout
    private lateinit var etNuevoNombre: TextInputEditText
    private lateinit var etNuevoEmail: TextInputEditText
    private lateinit var etNuevaContra: TextInputEditText
    private lateinit var etConfirmarContra: TextInputEditText
    private lateinit var cbTerminos: CheckBox
    private lateinit var btnFinalizarRegistro: MaterialButton
    private lateinit var tvGoLogin: TextView
    private lateinit var strengthBars: List<View>
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar)
        prefs = getSharedPreferences("PayReminderPrefs", MODE_PRIVATE)
        initViews()
        animarEntrada()
        setupListeners()
        setupPasswordStrength()
    }

    private fun initViews() {
        btnBack              = findViewById(R.id.btnBack)
        tilNombre            = findViewById(R.id.tilNombre)
        tilEmail             = findViewById(R.id.tilEmail)
        tilContra            = findViewById(R.id.tilContra)
        tilConfirmar         = findViewById(R.id.tilConfirmar)
        etNuevoNombre        = findViewById(R.id.etNuevoNombre)
        etNuevoEmail         = findViewById(R.id.etNuevoEmail)
        etNuevaContra        = findViewById(R.id.etNuevaContra)
        etConfirmarContra    = findViewById(R.id.etConfirmarContra)
        cbTerminos           = findViewById(R.id.cbTerminos)
        btnFinalizarRegistro = findViewById(R.id.btnFinalizarRegistro)
        tvGoLogin            = findViewById(R.id.tvGoLogin)
        strengthBars = listOf(
            findViewById(R.id.strengthBar1),
            findViewById(R.id.strengthBar2),
            findViewById(R.id.strengthBar3),
            findViewById(R.id.strengthBar4)
        )
    }

    private fun animarEntrada() {
        val vistas = listOf(etNuevoNombre, etNuevoEmail, etNuevaContra, etConfirmarContra, btnFinalizarRegistro, tvGoLogin)
        vistas.forEachIndexed { i, vista ->
            AnimationUtils.loadAnimation(this, R.anim.fade_in_up).also {
                it.startOffset = (i * 80).toLong()
                vista.startAnimation(it)
            }
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        tvGoLogin.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        btnFinalizarRegistro.setOnClickListener {
            if (validarFormulario()) registrarUsuario()
        }
    }

    private fun setupPasswordStrength() {
        etNuevaContra.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                actualizarBarras(calcularFuerza(s.toString()))
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun calcularFuerza(password: String): Int {
        if (password.isEmpty()) return 0
        var puntos = 0
        if (password.length >= 8) puntos++
        if (password.any { it.isUpperCase() }) puntos++
        if (password.any { it.isDigit() }) puntos++
        if (password.any { "!@#\$%^&*()_+-=".contains(it) }) puntos++
        return puntos
    }

    private fun actualizarBarras(fuerza: Int) {
        val colores = listOf(
            Color.parseColor("#F44336"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#FFC107"),
            Color.parseColor("#4CAF50")
        )
        val vacio = Color.parseColor("#E0E0E0")
        strengthBars.forEachIndexed { i, barra ->
            barra.setBackgroundColor(if (i < fuerza) colores[fuerza - 1] else vacio)
        }
    }

    private fun validarFormulario(): Boolean {
        var esValido = true
        val nombre   = etNuevoNombre.text.toString().trim()
        val email    = etNuevoEmail.text.toString().trim()
        val contra   = etNuevaContra.text.toString().trim()
        val confirma = etConfirmarContra.text.toString().trim()

        tilNombre.error   = null
        tilEmail.error    = null
        tilContra.error   = null
        tilConfirmar.error = null

        if (TextUtils.isEmpty(nombre)) {
            tilNombre.error = "Ingresa tu nombre completo"; esValido = false
        } else if (nombre.length < 3) {
            tilNombre.error = "Nombre demasiado corto"; esValido = false
        }
        if (TextUtils.isEmpty(email)) {
            tilEmail.error = "Ingresa tu correo"; esValido = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Correo no válido"; esValido = false
        }
        if (TextUtils.isEmpty(contra)) {
            tilContra.error = "Ingresa una contraseña"; esValido = false
        } else if (contra.length < 8) {
            tilContra.error = "Mínimo 8 caracteres"; esValido = false
        }
        if (TextUtils.isEmpty(confirma)) {
            tilConfirmar.error = "Confirma tu contraseña"; esValido = false
        } else if (contra != confirma) {
            tilConfirmar.error = "Las contraseñas no coinciden"; esValido = false
        }
        if (!cbTerminos.isChecked) {
            Snackbar.make(btnFinalizarRegistro,
                "Acepta los Términos y Condiciones", Snackbar.LENGTH_SHORT).show()
            esValido = false
        }
        return esValido
    }

    private fun registrarUsuario() {
        val nombre = etNuevoNombre.text.toString().trim()
        val email  = etNuevoEmail.text.toString().trim()
        val contra = etNuevaContra.text.toString().trim()

        if (email == prefs.getString("userEmail", "")) {
            tilEmail.error = "Este correo ya está registrado"
            return
        }

        prefs.edit()
            .putString("userName", nombre)
            .putString("userEmail", email)
            .putString("userPassword", contra)
            .apply()

        Snackbar.make(btnFinalizarRegistro,
            "¡Cuenta creada! Ahora inicia sesión", Snackbar.LENGTH_SHORT).show()

        // ✅ VA A MainActivity (pantalla de login)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finishAffinity()
    }
}