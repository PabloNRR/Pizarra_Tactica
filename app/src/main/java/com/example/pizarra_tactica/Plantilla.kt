package com.example.pizarra_tactica

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import java.io.File

class Plantilla : AppCompatActivity() {

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val btnEscudo: ImageButton = findViewById(R.id.btnescudo)
            Glide.with(this).load(it).into(btnEscudo)
            // Guardar la URI seleccionada en el tag del botón para usarla más tarde
            btnEscudo.tag = it.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plantilla)

        val id = intent.getStringExtra("id") ?: return

        // Obtener el nombre y el URI del escudo desde BDEQUIPOS.txt
        val archivoEquipos = File(filesDir, "BDEQUIPOS.txt")
        var nombre: String? = null
        var imageUri: String? = null

        if (archivoEquipos.exists()) {
            val lineas = archivoEquipos.readLines()
            for (linea in lineas) {
                val partes = linea.split("-")
                if (partes.size >= 3 && partes[0] == id) {
                    nombre = partes[1]  // Nombre del equipo
                    imageUri = partes[2]  // URI del escudo
                    break
                }
            }
        }

        // Configurar los valores iniciales en la interfaz
        val nombreEditText: EditText = findViewById(R.id.Text)
        val btnEscudo: ImageButton = findViewById(R.id.btnescudo)

        nombreEditText.setText(nombre)
        imageUri?.let {
            Glide.with(this).load(it).into(btnEscudo)
            btnEscudo.tag = it // Guardar el URI en el tag del botón
        }

        val btnCampo: ImageButton = findViewById(R.id.btncampo)
        val btnHome: ImageButton = findViewById(R.id.btnhome)
        val btnEliminar: ImageButton = findViewById(R.id.btntrash)
        // Llamar a la función para actualizar los TextViews
        actualizarListaJugadores(id)

        btnCampo.setOnClickListener {
            modificarEquipo()
            val intent = Intent(this, CampoActivity::class.java)
            intent.putExtra("id", id) // Pasar el ID al CampoActivity
            startActivity(intent)
        }

        btnHome.setOnClickListener {
            modificarEquipo()
            val intent = Intent(this, ElegirEquipo::class.java)
            startActivity(intent)
        }

        btnEscudo.setOnClickListener {
            getImage.launch("image/*")
        }

        btnEliminar.setOnClickListener {
            val idEquipo = id // Capturamos el ID del equipo
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Confirmación de eliminación")
            builder.setMessage("Está a punto de eliminar el equipo completamente, ¿quiere continuar?")
            builder.setPositiveButton("Sí") { _, _ ->
                eliminarEquipo(idEquipo)
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // Cierra el cuadro de diálogo sin hacer nada
            }
            builder.create().show()
        }
    }

    private fun actualizarListaJugadores(id: String) {
        val archivo = File(filesDir, "BDPLANTILLAS.txt")
        if (!archivo.exists()) return

        val lineas = archivo.readLines()

        lineas.forEachIndexed { index, linea ->
            val partes = linea.split("-")
            if (partes.size < 5) return@forEachIndexed

            val idequipo = partes[0]
            val dorsal = partes[1]
            val nombre = partes[2]
            val posicion = partes[3]

            if (idequipo != id) return@forEachIndexed

            val textoFormateado = String.format(
                "       %2s      |  %-51s%-3s",
                dorsal,
                nombre.take(51),
                posicion.take(3)
            )

            val textView = when (index % 24) {
                0 -> findViewById<TextView>(R.id.Jug1)
                1 -> findViewById(R.id.Jug2)
                2 -> findViewById(R.id.Jug3)
                3 -> findViewById(R.id.Jug4)
                4 -> findViewById(R.id.Jug5)
                5 -> findViewById(R.id.Jug6)
                6 -> findViewById(R.id.Jug7)
                7 -> findViewById(R.id.Jug8)
                8 -> findViewById(R.id.Jug9)
                9 -> findViewById(R.id.Jug10)
                10 -> findViewById(R.id.Jug11)
                11 -> findViewById(R.id.Jug12)
                12 -> findViewById(R.id.Jug13)
                13 -> findViewById(R.id.Jug14)
                14 -> findViewById(R.id.Jug15)
                15 -> findViewById(R.id.Jug16)
                16 -> findViewById(R.id.Jug17)
                17 -> findViewById(R.id.Jug18)
                18 -> findViewById(R.id.Jug19)
                19 -> findViewById(R.id.Jug20)
                20 -> findViewById(R.id.Jug21)
                21 -> findViewById(R.id.Jug22)
                22 -> findViewById(R.id.Jug23)
                23 -> findViewById(R.id.Jug24)
                else -> null
            }

            textView?.text = textoFormateado

            textView?.setOnClickListener {
                modificarEquipo()
                val intent = Intent(this, Jugador::class.java).apply {
                    putExtra("id", id)
                    putExtra("dorsal", dorsal)
                }
                startActivity(intent)
            }

            // Depuración
            println("Index: $index, ID Equipo: $idequipo, Dorsal: $dorsal, Nombre: $nombre, Posición: $posicion")
        }
    }




    private fun eliminarEquipo(id: String) {
        val archivoEquipos = File(filesDir, "BDEQUIPOS.txt")
        val archivoPlantillas = File(filesDir, "BDPLANTILLAS.txt")
        val textoIngresado = "Añadir Equipo"
        val imageUri = Uri.parse("android.resource://${packageName}/drawable/addescudo")

        if (archivoEquipos.exists()) {
            val lineas = archivoEquipos.readLines().toMutableList()
            for (i in lineas.indices) {
                val partes = lineas[i].split("-")
                if (partes[0] == id) {
                    // Reemplazar el nombre y el URI del escudo
                    lineas[i] = "$id-$textoIngresado-$imageUri"
                    break
                }
            }
            archivoEquipos.writeText(lineas.joinToString("\n"))
        }

        if (archivoPlantillas.exists()) {
            val lineas = archivoPlantillas.readLines().toMutableList()
            for (i in lineas.indices) {
                val partes = lineas[i].split("-")
                if (partes[0] == id) {
                    lineas[i] = "$id-0-Nombre del jugador-DC-Nota"
                }
            }
            archivoPlantillas.writeText(lineas.joinToString("\n"))
        }

        // Redirigir al usuario a la actividad ElegirEquipo
        val intent = Intent(this, ElegirEquipo::class.java)
        startActivity(intent)
        finish() // Finaliza la actividad actual para que no esté en la pila
    }


    private fun modificarEquipo() {
        // Obtener el ID del equipo, nombre del equipo, y el nuevo escudo desde la interfaz
        val id = intent.getStringExtra("id") ?: return
        val nuevoNombre = findViewById<EditText>(R.id.Text).text.toString()
        val nuevoEscudoUri = findViewById<ImageButton>(R.id.btnescudo).tag?.toString() ?: ""

        // Referencia al archivo en el almacenamiento interno
        val archivoEquipos = File(filesDir, "BDEQUIPOS.txt")

        // Verificar si el archivo existe
        if (!archivoEquipos.exists()) {
            return // Si no existe, no se hace nada
        }

        // Leer todas las líneas del archivo
        val lineas = archivoEquipos.readLines().toMutableList()

        // Modificar la línea que coincide con el ID
        for (i in lineas.indices) {
            val partes = lineas[i].split("-")
            if (partes[0] == id) {
                // Reemplazar el nombre y el URI del escudo
                lineas[i] = "$id-$nuevoNombre-$nuevoEscudoUri"
                break
            }
        }

        // Escribir las líneas actualizadas nuevamente al archivo
        archivoEquipos.writeText(lineas.joinToString("\n"))
    }
}