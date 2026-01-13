package com.example.pizarra_tactica

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class Jugador : AppCompatActivity() {

    private val posiciones = arrayOf("EI", "DC", "ED", "MCO", "MI", "MC", "MD", "LI", "DFC", "LD", "POR")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jugador)

        val id = intent.getStringExtra("id")
        val dorsal = intent.getStringExtra("dorsal")

        val nombreText = findViewById<EditText>(R.id.nombretext)
        val dorsalText = findViewById<EditText>(R.id.dorsaltext)
        val notaText = findViewById<EditText>(R.id.notatext)
        val btnSeleccionarPosicion = findViewById<Button>(R.id.btnSeleccionar)
        val btnGuardarJugador = findViewById<Button>(R.id.btnguardarjugador)
        val btnCancelar = findViewById<Button>(R.id.btncancelar)
        val btnEliminarJugador = findViewById<Button>(R.id.btneliminarjugador)

        // Obtener datos del jugador y establecerlos en la interfaz
        val jugador = obtenerDatosJugador(this, id!!, dorsal!!)
        jugador?.let {
            nombreText.setText(it.nombre)
            dorsalText.setText(dorsal)
            notaText.setText(it.nota)
            btnSeleccionarPosicion.text = it.posicion
        }

        btnGuardarJugador.setOnClickListener {
            modificarJugador(this, id, dorsal, nombreText.text.toString(), dorsalText.text.toString(), btnSeleccionarPosicion.text.toString(), notaText.text.toString())
        }

        btnCancelar.setOnClickListener {
            val intent = Intent(this, Plantilla::class.java).apply {
                putExtra("id", id)         // Pasar el ID del equipo
            }
            startActivity(intent)
        }

        btnEliminarJugador.setOnClickListener {
            eliminarJugador(this, id, dorsal)
            val intent = Intent(this, Plantilla::class.java).apply {
                putExtra("id", id)         // Pasar el ID del equipo
            }
            startActivity(intent)
        }

        btnSeleccionarPosicion.setOnClickListener {
            mostrarDialogoPosiciones()
        }
    }

    data class JugadorData(val nombre: String, val posicion: String, val nota: String)

    private fun obtenerDatosJugador(context: Context, id: String, dorsal: String): JugadorData? {
        val archivo = File(context.filesDir, "BDPLANTILLAS.txt")
        if (!archivo.exists()) return null

        val lineas = archivo.readLines()
        for (linea in lineas) {
            val partes = linea.split("-")
            // Comprobamos que el id de equipo y dorsal coinciden
            if (partes.size >= 5 && partes[0] == id && partes[1] == dorsal) {
                val nombre = partes[2]
                val posicion = partes[3]
                val nota = partes[4]
                return JugadorData(nombre, posicion, nota)
            }
        }
        return null
    }


    private fun mostrarDialogoPosiciones() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona una posición")
        builder.setItems(posiciones) { _, which ->
            val seleccion = posiciones[which]
            val btnSeleccionarPosicion: Button = findViewById(R.id.btnSeleccionar)
            btnSeleccionarPosicion.text = seleccion
        }
        builder.show()
    }

    private fun modificarJugador(context: Context, id: String, dorsal: String, nuevoNombre: String, nuevoDorsal: String, nuevaPosicion: String, nuevaNota: String) {
        if (nuevoDorsal.toIntOrNull() !in 1..99) {
            AlertDialog.Builder(this)
                .setMessage("El dorsal debe ser un número del 1 al 99, cámbielo para poder guardar")
                .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                .show()
            return
        }

        val archivo = File(context.filesDir, "BDPLANTILLAS.txt")
        if (archivo.exists()) {
            val lineas = archivo.readLines().toMutableList()
            val indice = lineas.indexOfFirst { it.startsWith("$id-$dorsal-") }

            if (indice != -1) {
                lineas[indice] = "$id-$nuevoDorsal-$nuevoNombre-$nuevaPosicion-$nuevaNota"
                archivo.writeText(lineas.joinToString("\n"))
                Toast.makeText(context, "Jugador modificado correctamente", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Plantilla::class.java).apply {
                    putExtra("id", id)         // Pasar el ID del equipo
                }
                startActivity(intent)
            } else {
                Toast.makeText(context, "No se encontró el jugador", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Archivo no encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarJugador(context: Context, id: String, dorsal: String) {
        val archivo = File(context.filesDir, "BDPLANTILLAS.txt")
        if (archivo.exists()) {
            val lineas = archivo.readLines().toMutableList()
            val indice = lineas.indexOfFirst { it.startsWith("$id-$dorsal-") }

            if (indice != -1) {
                lineas[indice] = "$id-0-Nombre del jugador-DC-Nota"
                archivo.writeText(lineas.joinToString("\n"))
                Toast.makeText(context, "Jugador eliminado correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "No se encontró el jugador", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Archivo no encontrado", Toast.LENGTH_SHORT).show()
        }
    }
}