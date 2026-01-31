package com.example.pizarra_tactica

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback // Necesario para el nuevo botón atrás
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class Jugador : AppCompatActivity() {

    private val posiciones = arrayOf("EI", "DC", "ED", "MCO", "MI", "MC", "MD", "LI", "DFC", "LD", "POR")

    // 1. DECLARACIÓN DE VARIABLES DE CLAVE (Nivel Clase)
    private lateinit var idEquipo: String
    private var dorsalOriginal: Int = -1
    private var esNuevo: Boolean = false

    private lateinit var nombreText: EditText
    private lateinit var dorsalText: EditText
    private lateinit var notaText: EditText
    private lateinit var btnSeleccionarPosicion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jugador)

        // 2. RECOGIDA DE DATOS (Asegúrate de no poner "val" delante de idEquipo aquí)
        idEquipo = intent.getStringExtra("id") ?: ""
        esNuevo = intent.getBooleanExtra("ES_NUEVO", false)

        nombreText = findViewById(R.id.nombretext)
        dorsalText = findViewById(R.id.dorsaltext)
        notaText = findViewById(R.id.notatext)
        btnSeleccionarPosicion = findViewById(R.id.btnSeleccionar)

        val btnGuardarJugador = findViewById<Button>(R.id.btnguardarjugador)
        val btnCancelar = findViewById<Button>(R.id.btncancelar)
        val btnEliminarJugador = findViewById<Button>(R.id.btneliminarjugador)

        // 3. LÓGICA DE INICIO
        if (!esNuevo) {
            // Si venimos de la lista, el dorsal llega como String del Intent
            dorsalOriginal = intent.getStringExtra("dorsal")?.toIntOrNull() ?: -1
            cargarDatosJugador()
        } else {
            // Si es un jugador totalmente nuevo
            btnEliminarJugador.visibility = View.GONE
            nombreText.setText("")
            nombreText.hint = "Introduce nombre"
            btnSeleccionarPosicion.text = "Seleccionar"
        }

        // 4. EL NUEVO "ON BACK PRESSED" (Soluciona tu error en rojo)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                ejecutarGuardadoYSalir()
            }
        })

        btnGuardarJugador.setOnClickListener { ejecutarGuardadoYSalir() }
        btnCancelar.setOnClickListener { volverAPlantilla() }
        btnEliminarJugador.setOnClickListener { eliminarJugador() }
        btnSeleccionarPosicion.setOnClickListener { mostrarDialogoPosiciones() }
    }

    private fun ejecutarGuardadoYSalir() {
        val nombre = nombreText.text.toString().trim()
        val posicion = btnSeleccionarPosicion.text.toString()
        val nuevoDorsal = dorsalText.text.toString().toIntOrNull() ?: 0

        // VALIDACIONES OBLIGATORIAS
        if (nombre.isEmpty() || nombre == "---" || nombre == "Nombre del jugador") {
            mostrarError("Debes introducir un nombre real.")
            return
        }

        if (posicion == "Seleccionar" || posicion.isEmpty()) {
            mostrarError("Debes elegir una posición.")
            return
        }

        if (nuevoDorsal !in 1..99) {
            mostrarError("El dorsal debe ser entre 1 y 99.")
            return
        }

        lifecycleScope.launch {
            try {
                // Comprobamos duplicados en la nube usando idEquipo (que ahora es de clase)
                val jugadores = RetrofitClient.instance.obtenerJugadores(idEquipo)
                val yaExiste = jugadores.any { it.dorsal == nuevoDorsal && it.dorsal != dorsalOriginal }

                if (yaExiste) {
                    mostrarError("El dorsal $nuevoDorsal ya está ocupado.")
                } else {
                    val jugadorEditado = JugadorRemote(
                        equipo_id = idEquipo, // <--- Verifica que en tu JugadorRemote se llame equipo_id
                        dorsal = nuevoDorsal,
                        nombre = nombre,
                        posicion = posicion,
                        nota = notaText.text.toString(),
                        old_dorsal = if (esNuevo) null else dorsalOriginal
                    )
                    guardarEnNube(jugadorEditado)
                }
            } catch (e: Exception) {
                Toast.makeText(this@Jugador, "Error al validar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cargarDatosJugador() {
        lifecycleScope.launch {
            try {
                val jugadores = RetrofitClient.instance.obtenerJugadores(idEquipo)
                val j = jugadores.find { it.dorsal == dorsalOriginal }
                j?.let {
                    nombreText.setText(it.nombre)
                    dorsalText.setText(it.dorsal.toString())
                    notaText.setText(it.nota)
                    btnSeleccionarPosicion.text = it.posicion
                }
            } catch (e: Exception) {
                Toast.makeText(this@Jugador, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarEnNube(jugador: JugadorRemote) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.guardarJugador(jugador)
                if (response.isSuccessful) {
                    volverAPlantilla()
                } else {
                    Toast.makeText(this@Jugador, "Error en el servidor", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Jugador, "Fallo de red", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun volverAPlantilla() {
        val intent = Intent(this, Plantilla::class.java).apply {
            putExtra("id", idEquipo)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        finish()
    }

    private fun eliminarJugador() {
        // ... Lógica para borrar fila en la nube ...
    }

    private fun mostrarDialogoPosiciones() {
        AlertDialog.Builder(this)
            .setTitle("Posición")
            .setItems(posiciones) { _, which -> btnSeleccionarPosicion.text = posiciones[which] }
            .show()
    }

    private fun mostrarError(mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle("Atención")
            .setMessage(mensaje)
            .setPositiveButton("Entendido", null)
            .show()
    }
}