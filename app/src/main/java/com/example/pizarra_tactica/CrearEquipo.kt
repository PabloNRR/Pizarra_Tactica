package com.example.pizarra_tactica

import android.content.Context
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random

class CrearEquipo : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crearequipo)

        val editText = findViewById<EditText>(R.id.editText)
        val añadir = findViewById<Button>(R.id.btnañadirescudo)
        imageView = findViewById(R.id.escudo)
        val cancelar = findViewById<Button>(R.id.btncancelarequipo)
        val plantilla = findViewById<Button>(R.id.btnplantilla)

        cancelar.setOnClickListener {
            val intent: Intent = Intent(this, ElegirEquipo::class.java)
            startActivity(intent)
        }
        añadir.setOnClickListener {
            openFileChooser()
        }
        plantilla.setOnClickListener {
            val nuevoId = generarIdUnico(this)
            val textoIngresado = editText.text.toString()
            guardarEnBDEquipos(this, nuevoId, textoIngresado, imageUri)
            val intent: Intent = Intent(this, Plantilla::class.java)
            startActivity(intent)
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imageView.setImageURI(imageUri)
        }
    }

    fun generarIdUnico(context: Context): String {
        val nombreFichero = "BDEQUIPOS.txt"
        val ruta = context.filesDir
        val fichero = File(ruta, nombreFichero)
        val idsExistentes = mutableSetOf<String>()

        // Lee los IDs existentes del fichero
        if (fichero.exists()) {
            try {
                fichero.forEachLine { linea ->
                    idsExistentes.add(linea.trim())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        // Genera un ID único
        var nuevoId: String
        do {
            nuevoId = "C" + (1..6).map { Random.nextInt(0, 10) }.joinToString("")
        } while (idsExistentes.contains(nuevoId))

        return nuevoId
    }

    private fun guardarEnBDEquipos(context: Context, nuevoId: String, textoIngresado: String, imageUri: Uri?) {
        val nombreFichero = "BDEQUIPOS.txt"
        val ruta = context.filesDir
        val fichero = File(ruta, nombreFichero)

        val cadena = "$nuevoId-$textoIngresado-${imageUri.toString()}\n"

        try {
            val fos = FileOutputStream(fichero, true)
            fos.write(cadena.toByteArray())
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}