package com.example.pizarra_tactica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EstrategiasActivity : AppCompatActivity() {

    private lateinit var idEquipo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estrategias)

        idEquipo = intent.getStringExtra("id") ?: return

        val contenedor: LinearLayout = findViewById(R.id.contenedorEstrategias)
        val btnNuevaEstrategia: Button = findViewById(R.id.btn_nueva_estrategia)

        // Aquí más adelante cargaremos las estrategias de la base de datos
        // Por ahora, un ejemplo de cómo se vería una estrategia añadida:
        añadirEstrategiaALista("Córner Cerrado Izquierda", contenedor)
    }

    private fun añadirEstrategiaALista(nombre: String, contenedor: LinearLayout) {
        val tv = TextView(this).apply {
            text = nombre
            textSize = 24f
            setPadding(0, 30, 0, 30) // Más espacio arriba y abajo
            setTextColor(resources.getColor(R.color.white))

            // ESTO LO CENTRA
            gravity = android.view.Gravity.CENTER

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            setOnClickListener {
                // Aquí irá la lógica para abrir la pizarra
            }
        }
        contenedor.addView(tv)
    }
}