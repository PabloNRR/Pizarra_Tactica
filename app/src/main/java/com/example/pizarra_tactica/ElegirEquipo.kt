package com.example.pizarra_tactica

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.util.Log

class ElegirEquipo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elegirequipo)

        // Botón estático para crear un equipo nuevo
        findViewById<ImageButton>(R.id.btn_añadir_nuevo).setOnClickListener {
            val intent = Intent(this, Plantilla::class.java).apply {
                // Enviamos "NUEVO" para que la Plantilla sepa que está empezando de cero
                putExtra("id", "NUEVO")
                putExtra("nombre", "Nuevo Equipo")
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        consultarEquiposALaNube()
    }

    private fun consultarEquiposALaNube() {
        val contenedor = findViewById<LinearLayout>(R.id.contenedorEquipos)

        lifecycleScope.launch {
            try {
                val equiposRemote = RetrofitClient.instance.obtenerEquipos()

                // Limpiamos los equipos antiguos (pero mantenemos el botón añadir en el XML)
                // O eliminamos todo y lo re-añadimos para evitar duplicados:
                actualizarInterfaz(contenedor, equiposRemote)

            } catch (e: Exception) {
                Log.e("API_ERROR", "Error: ${e.message}")
            }
        }
    }

    private fun actualizarInterfaz(contenedor: LinearLayout, equipos: List<EquipoRemote>) {
        if (contenedor.childCount > 1) {
            contenedor.removeViews(1, contenedor.childCount - 1)
        }

        equipos.forEach { equipo ->
            if (equipo.nombre != "Añadir Equipo" && equipo.nombre != "Nuevo Equipo") {
                val nuevoBoton = ImageButton(this).apply {
                    layoutParams = LinearLayout.LayoutParams(700, 700).apply {
                        setMargins(40, 0, 40, 0)
                    }
                    scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                    background = null
                }

                // --- CAMBIO AQUÍ: Forzamos la actualización ---
                Glide.with(this)
                    .load(equipo.imageUri)
                    .placeholder(R.drawable.addescudo)
                    // Esto obliga a Glide a refrescar la imagen si el archivo cambió
                    .signature(com.bumptech.glide.signature.ObjectKey(System.currentTimeMillis().toString()))
                    .into(nuevoBoton)

                nuevoBoton.setOnClickListener {
                    val intent = Intent(this, Plantilla::class.java).apply {
                        putExtra("id", equipo.id)
                        putExtra("nombre", equipo.nombre)
                        putExtra("imageUri", equipo.imageUri)
                    }
                    startActivity(intent)
                }
                contenedor.addView(nuevoBoton)
            }
        }
    }
}