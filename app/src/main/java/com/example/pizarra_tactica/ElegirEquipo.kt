package com.example.pizarra_tactica

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.util.Log // Importante para depurar

class ElegirEquipo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elegirequipo)
        consultarEquiposALaNube()
    }

    override fun onResume() {
        super.onResume()
        consultarEquiposALaNube()
    }

    private fun consultarEquiposALaNube() {
        val imageButtonIds = arrayOf(
            R.id.Jug1, R.id.Jug2, R.id.Jug3, R.id.Jug4, R.id.Jug5,
            R.id.Jug6, R.id.Jug7, R.id.Jug8, R.id.Jug9, R.id.Jug10,
            R.id.Jug11, R.id.Jug12, R.id.Jug13, R.id.Jug14, R.id.Jug15,
            R.id.Jug16, R.id.Jug17, R.id.Jug18, R.id.Jug19, R.id.Jug20
        )

        lifecycleScope.launch {
            try {
                // Retrofit ya devuelve List<EquipoRemote> automáticamente
                val equiposRemote = RetrofitClient.instance.obtenerEquipos()

                equiposRemote.forEachIndexed { index, equipo ->
                    if (index < imageButtonIds.size) {
                        val imageButton = findViewById<ImageButton>(imageButtonIds[index])

                        // Usamos .imageUri (que coincide con tu data class EquipoRemote)
                        Glide.with(this@ElegirEquipo)
                            .load(equipo.imageUri) // Glide acepta String, no hace falta Uri.parse siempre
                            .placeholder(R.drawable.addescudo) // Imagen por defecto mientras carga
                            .into(imageButton)

                        imageButton.setOnClickListener {
                            val intent = Intent(this@ElegirEquipo, Plantilla::class.java)
                            intent.putExtra("id", equipo.id)
                            intent.putExtra("nombre", equipo.nombre)
                            intent.putExtra("imageUri", equipo.imageUri)
                            startActivity(intent)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error conectando a la VM: ${e.message}")
            }
        }
    }
}