package com.example.pizarra_tactica

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class EstrategiasActivity : AppCompatActivity() {

    private lateinit var idEquipo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estrategias)

        // 1. Recuperamos el ID del equipo que viene de CampoActivity
        idEquipo = intent.getStringExtra("id") ?: return

        val contenedor: LinearLayout = findViewById(R.id.contenedorEstrategias)

        // 2. Cargamos las estrategias reales desde el servidor
        cargarEstrategias(contenedor)
    }

    private fun cargarEstrategias(contenedor: LinearLayout) {
        // Usamos una corrutina para no congelar la pantalla mientras descarga
        lifecycleScope.launch {
            try {
                // Llamada a tu servidor Python
                val listaEstrategias = RetrofitClient.instance.obtenerEstrategias(idEquipo)

                contenedor.removeAllViews() // Limpiamos la lista antes de llenarla

                if (listaEstrategias.isEmpty()) {
                    Toast.makeText(this@EstrategiasActivity, "No hay estrategias guardadas", Toast.LENGTH_SHORT).show()
                }

                // 3. Por cada estrategia guardada, creamos un elemento en la lista
                listaEstrategias.forEach { estrategia ->
                    crearItemEstrategia(estrategia, contenedor)
                }

            } catch (e: Exception) {
                Toast.makeText(this@EstrategiasActivity, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun crearItemEstrategia(estrategia: EstrategiaRemote, contenedor: LinearLayout) {
        val tv = TextView(this).apply {
            text = estrategia.nombre.uppercase() // El nombre que le diste al guardar
            textSize = 22f
            setPadding(20, 45, 20, 45)
            setTextColor(android.graphics.Color.WHITE)
            gravity = android.view.Gravity.CENTER

            // Estilo visual: una línea separadora opcional
            setBackgroundResource(android.R.drawable.list_selector_background)

            // 4. AL PULSAR: Volvemos al campo con los datos de los jugadores
            setOnClickListener {
                val intent = Intent(this@EstrategiasActivity, MedioCampo::class.java)
                intent.putExtra("id", idEquipo)
                intent.putExtra("DATOS_ESTRATEGIA", estrategia.datos_jugadores) // Pasamos las coordenadas
                startActivity(intent)
            }
        }
        contenedor.addView(tv)
    }
}