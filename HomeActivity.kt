package com.example.payremider

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.NumberFormat
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var tvNombreUsuario: TextView
    private lateinit var tvSaludo: TextView
    private lateinit var tvTotalPendiente: TextView
    private lateinit var tvCantidadPagos: TextView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var ivAvatar: android.widget.ImageView
    private lateinit var layoutVacio: LinearLayout
    private lateinit var rvPagos: RecyclerView
    private lateinit var tvVerTodos: TextView
    private lateinit var prefs: SharedPreferences
    private lateinit var receiptAdapter: ReceiptAdapter
    private val gson = Gson()
    private var recibos: List<Receipt> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        prefs = getSharedPreferences("PayReminderPrefs", Context.MODE_PRIVATE)

        initViews()
        cargarDatosUsuario()
        setupRecyclerView() // 🔴 AHORA CONFIGURA EL ADAPTADOR CON ELIMINAR
        cargarRecibos()
        animarEntrada()
        setupBottomNav()
        setupAvatar()
        setupOnBackPressed()
    }

    private fun initViews() {
        tvNombreUsuario = findViewById(R.id.tvNombreUsuario)
        tvSaludo = findViewById(R.id.tvSaludo)
        tvTotalPendiente = findViewById(R.id.tvTotalPendiente)
        tvCantidadPagos = findViewById(R.id.tvCantidadPagos)
        bottomNav = findViewById(R.id.bottomNav)
        ivAvatar = findViewById(R.id.ivAvatar)
        layoutVacio = findViewById(R.id.layoutVacio)
        rvPagos = findViewById(R.id.rvPagos)
        tvVerTodos = findViewById(R.id.tvVerTodos)
    }

    private fun cargarDatosUsuario() {
        val nombre = prefs.getString("userName", "Usuario") ?: "Usuario"
        tvNombreUsuario.text = nombre

        tvSaludo.text = when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "¡Buenos días!"
            in 12..17 -> "¡Buenas tardes!"
            else -> "¡Buenas noches!"
        }
    }

    // 🔴 NUEVO: setupRecyclerView con el adaptador que tiene eliminar
    private fun setupRecyclerView() {
        receiptAdapter = ReceiptAdapter(
            recibos,
            onItemClick = { receipt ->
                Toast.makeText(this, "Recibo: $${receipt.amount}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { receipt ->
                eliminarRecibo(receipt)
            }
        )
        rvPagos.layoutManager = LinearLayoutManager(this)
        rvPagos.adapter = receiptAdapter
    }

    private fun cargarRecibos() {
        val recibosJson = prefs.getString("recibos", "[]")
        val type = object : TypeToken<List<Receipt>>() {}.type
        recibos = gson.fromJson(recibosJson, type)

        // Actualizar vistas
        val total = recibos.sumOf { it.amount }
        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        tvTotalPendiente.text = format.format(total)
        tvCantidadPagos.text = recibos.size.toString()

        // Mostrar lista vacía o recyclerView
        if (recibos.isEmpty()) {
            layoutVacio.visibility = android.view.View.VISIBLE
            rvPagos.visibility = android.view.View.GONE
        } else {
            layoutVacio.visibility = android.view.View.GONE
            rvPagos.visibility = android.view.View.VISIBLE
            receiptAdapter.updateList(recibos)
        }
    }

    // 🔴 NUEVA FUNCIÓN PARA ELIMINAR RECIBO
    private fun eliminarRecibo(receipt: Receipt) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar recibo")
            .setMessage("¿Estás seguro de eliminar este recibo?")
            .setPositiveButton("Eliminar") { _, _ ->
                // Obtener lista actual
                val recibosJson = prefs.getString("recibos", "[]")
                val type = object : TypeToken<MutableList<Receipt>>() {}.type
                val listaRecibos: MutableList<Receipt> = gson.fromJson(recibosJson, type)

                // Eliminar el recibo
                listaRecibos.removeAll { it.id == receipt.id }

                // Guardar lista actualizada
                val nuevoJson = gson.toJson(listaRecibos)
                prefs.edit().putString("recibos", nuevoJson).apply()

                // Recargar vista
                cargarRecibos()

                Toast.makeText(this, "Recibo eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun animarEntrada() {
        tvTotalPendiente.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.fade_in_up)
        )
        tvCantidadPagos.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.fade_in_up).also { it.startOffset = 100 }
        )
        bottomNav.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.fade_in_up).also { it.startOffset = 200 }
        )
    }

    private fun setupBottomNav() {
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true

                R.id.nav_agregar -> {
                    startActivity(Intent(this, AddReceiptActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }

                R.id.nav_perfil -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }

                R.id.nav_logout -> {
                    cerrarSesion()
                    true
                }

                else -> false
            }
        }
    }

    private fun setupAvatar() {
        ivAvatar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun cerrarSesion() {
        prefs.edit().putBoolean("isLoggedIn", false).apply()
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finishAffinity()
    }

    private fun setupOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(true)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        cargarRecibos()
    }
}