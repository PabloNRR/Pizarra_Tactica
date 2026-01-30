package com.example.pizarra_tactica

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.io.File

class CrearEquipo : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null
    private lateinit var idEquipo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crearequipo)

        // IMPORTANTE: Recibimos el ID (C0, C1...) desde ElegirEquipo
        idEquipo = intent.getStringExtra("id") ?: "C0"

        val editText = findViewById<EditText>(R.id.editText)
        val añadir = findViewById<Button>(R.id.btnañadirescudo)
        imageView = findViewById(R.id.escudo)
        val cancelar = findViewById<Button>(R.id.btncancelarequipo)
        val plantilla = findViewById<Button>(R.id.btnplantilla)

        // Si ya venía un nombre o imagen, los mostramos
        editText.setText(intent.getStringExtra("nombre") ?: "---")
        val uriPrevio = intent.getStringExtra("imageUri")
        if (!uriPrevio.isNullOrEmpty()) {
            Glide.with(this).load(uriPrevio).into(imageView)
            imageUri = Uri.parse(uriPrevio)
        }

        cancelar.setOnClickListener {
            finish() // Volver atrás
        }

        añadir.setOnClickListener {
            openFileChooser()
        }

        plantilla.setOnClickListener {
            val nombreNuevo = editText.text.toString()
            // Si el usuario no puso nombre, dejamos el guion para mantener tu estética
            val nombreFinal = if (nombreNuevo.isBlank()) "---" else nombreNuevo
            val uriFinal = imageUri?.toString() ?: "android.resource://$packageName/drawable/addescudo"

            // 1. CREAMOS EL OBJETO EquipoRemote
            val equipoActualizado = EquipoRemote(
                id = idEquipo,
                nombre = nombreFinal,
                imageUri = uriFinal
            )

            // 2. GUARDAMOS EN LA NUBE
            guardarEnNube(equipoActualizado)
        }
    }

    private fun guardarEnNube(equipo: EquipoRemote) {
        lifecycleScope.launch {
            try {
                // Llamamos al POST de tu API
                val response = RetrofitClient.instance.guardarEquipo(equipo)
                if (response.isSuccessful) {
                    Toast.makeText(this@CrearEquipo, "Equipo guardado en la nube", Toast.LENGTH_SHORT).show()

                    // Vamos a la pantalla de la plantilla de ese equipo
                    val intent = Intent(this@CrearEquipo, Plantilla::class.java).apply {
                        putExtra("id", equipo.id)
                        putExtra("nombre", equipo.nombre)
                        putExtra("imageUri", equipo.imageUri)
                    }
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CrearEquipo, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data?.data != null) {
            val selectedImage = data.data!!
            // Copiamos la imagen a nuestra carpeta interna para que la URI sea permanente
            imageUri = copiarImagenInterna(selectedImage, idEquipo)
            imageView.setImageURI(imageUri)
        }
    }

    private fun copiarImagenInterna(uriOrigen: Uri, equipoId: String): Uri? {
        return try {
            contentResolver.openInputStream(uriOrigen)?.use { input ->
                val archivoDestino = File(filesDir, "escudo_$equipoId.jpg")
                archivoDestino.outputStream().use { output -> input.copyTo(output) }
                // Devolvemos la URI de nuestro FileProvider
                androidx.core.content.FileProvider.getUriForFile(
                    this, "${packageName}.fileprovider", archivoDestino
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}