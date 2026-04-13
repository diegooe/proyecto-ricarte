package com.example.payremider

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AddReceiptActivity : AppCompatActivity() {

    private lateinit var etMonto: EditText
    private lateinit var etFecha: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var spinnerCategoria: Spinner
    private lateinit var btnGuardar: MaterialButton
    private lateinit var btnCancelar: Button
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_receipt)

        prefs = getSharedPreferences("PayReminderPrefs", Context.MODE_PRIVATE)
        initViews()
        configurarSpinner()
        setupFechaFormatter() // 🔴 NUEVO: formateador de fecha
        setupListeners()
    }

    private fun initViews() {
        etMonto = findViewById(R.id.etMonto)
        etFecha = findViewById(R.id.etFecha)
        etDescripcion = findViewById(R.id.etDescripcion)
        spinnerCategoria = findViewById(R.id.spinnerCategoria)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)
    }

    private fun configurarSpinner() {
        val categorias = arrayOf("Comida", "Transporte", "Servicios", "Entretenimiento", "Salud", "Otros")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoria.adapter = adapter
    }

    // 🔴 NUEVO: Formateador automático para la fecha
    private fun setupFechaFormatter() {
        etFecha.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val dateFormat = "##/##/####"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                val text = s.toString()
                if (text.isEmpty()) return

                // Eliminar cualquier carácter que no sea número
                val cleanText = text.replace("[^\\d]".toRegex(), "")

                if (cleanText.length > 8) {
                    // No permitir más de 8 dígitos
                    isUpdating = true
                    s?.replace(0, s.length, cleanText.substring(0, 8))
                    isUpdating = false
                    return
                }

                // Aplicar formato DD/MM/YYYY
                val formatted = StringBuilder()
                for (i in cleanText.indices) {
                    when (i) {
                        2, 4 -> formatted.append("/").append(cleanText[i])
                        else -> formatted.append(cleanText[i])
                    }
                }

                isUpdating = true
                s?.replace(0, s.length, formatted.toString())
                isUpdating = false
            }
        })
    }

    private fun setupListeners() {
        btnGuardar.setOnClickListener {
            val montoStr = etMonto.text.toString()
            val fechaStr = etFecha.text.toString()
            val descripcion = etDescripcion.text.toString()
            val categoria = spinnerCategoria.selectedItem.toString()

            if (montoStr.isEmpty() || fechaStr.isEmpty()) {
                Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar formato de fecha (debe tener 10 caracteres con formato DD/MM/YYYY)
            if (fechaStr.length != 10 || !fechaStr.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
                Toast.makeText(this, "Formato de fecha inválido. Usa DD/MM/AAAA", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val monto = montoStr.toDoubleOrNull() ?: 0.0

            // 🔴 Guardar la fecha SIN las barras para almacenamiento
            val fechaSinFormato = fechaStr.replace("/", "")

            // Crear nuevo recibo
            val newReceipt = Receipt(
                amount = monto,
                date = fechaSinFormato, // Guardamos sin formato (ej: 23042006)
                description = descripcion,
                category = categoria
            )

            // Guardar recibo
            guardarRecibo(newReceipt)

            Toast.makeText(this, "Recibo guardado correctamente", Toast.LENGTH_SHORT).show()
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        btnCancelar.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun guardarRecibo(receipt: Receipt) {
        // Obtener lista existente
        val recibosJson = prefs.getString("recibos", "[]")
        val type = object : TypeToken<MutableList<Receipt>>() {}.type
        val recibos: MutableList<Receipt> = gson.fromJson(recibosJson, type)

        // Agregar nuevo recibo
        recibos.add(receipt)

        // Guardar lista actualizada
        val nuevoJson = gson.toJson(recibos)
        prefs.edit().putString("recibos", nuevoJson).apply()

        // Actualizar total pendiente
        val total = recibos.sumOf { it.amount }
        prefs.edit().putFloat("totalPendiente", total.toFloat()).apply()
        prefs.edit().putInt("cantidadPagos", recibos.size).apply()
    }
}