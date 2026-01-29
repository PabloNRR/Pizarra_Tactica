package com.example.pizarra_tactica

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class ElegirEquipo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elegirequipo)
        cargarEquiposDesdeArchivo()

    }

    override fun onResume() {
        super.onResume()
        // Sacamos la lógica de carga aquí para que se refresque al volver
        cargarEquiposDesdeArchivo()
    }

    private fun cargarEquiposDesdeArchivo() {
        val file = File(filesDir, "BDEQUIPOS.txt")
        // Define un array con los identificadores de los botones de imagen
        val imageButtonIds = arrayOf(
            R.id.Jug1,
            R.id.Jug2,
            R.id.Jug3,
            R.id.Jug4,
            R.id.Jug5,
            R.id.Jug6,
            R.id.Jug7,
            R.id.Jug8,
            R.id.Jug9,
            R.id.Jug10,
            R.id.Jug11,
            R.id.Jug12,
            R.id.Jug13,
            R.id.Jug14,
            R.id.Jug15,
            R.id.Jug16,
            R.id.Jug17,
            R.id.Jug18,
            R.id.Jug19,
            R.id.Jug20,
            // Añade más identificadores según sea necesario
        )

        if (file.exists()) {
            val equipos = leerEquipos(file)

            equipos.forEachIndexed { index, equipo ->
                // Usa el índice para obtener el identificador del botón de imagen
                val imageButtonId = imageButtonIds[index]
                val imageButton = findViewById<ImageButton>(imageButtonId)

                // Obtenemos el archivo real para ver cuándo se modificó por última vez
                val archivoFoto = File(filesDir, "escudo_${equipo.id}.jpg")

                // Carga la imagen en el ImageButton usando Glide
                Glide.with(this).load(Uri.parse(equipo.imageUri))
                    .signature(com.bumptech.glide.signature.ObjectKey(archivoFoto.lastModified()))
                    .skipMemoryCache(true)
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                    .into(imageButton)

                // Configura el click listener para cada botón
                imageButton.setOnClickListener {
                    val intent = Intent(this, Plantilla::class.java)
                    intent.putExtra("id", equipo.id)
                    intent.putExtra("nombre", equipo.nombre)
                    intent.putExtra("imageUri", equipo.imageUri)
                    startActivity(intent)
                }
            }
        }
    }

    private fun leerEquipos(file: File): List<Equipo> {
        val equipos = mutableListOf<Equipo>()
        BufferedReader(InputStreamReader(file.inputStream())).use { reader ->
            var line = reader.readLine()
            while (line != null) {
                val partes = line.split("-")
                if (partes.size == 3) {
                    equipos.add(Equipo(partes[0], partes[1], partes[2]))
                }
                line = reader.readLine()
            }
        }
        return equipos
    }

    data class Equipo(val id: String, val nombre: String, val imageUri: String)
}