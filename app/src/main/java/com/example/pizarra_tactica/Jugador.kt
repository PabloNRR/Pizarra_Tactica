package com.example.pizarra_tactica

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class Jugador : AppCompatActivity() {

    private val posiciones = arrayOf("EI", "DC", "ED", "MCO", "MI", "MC", "MD", "LI", "DFC", "LD", "POR")
    private lateinit var idEquipo: String
    private var dorsalOriginal: Int = 0

    // Declaramos las vistas como variables de clase para acceder a ellas fácilmente
    private lateinit var nombreText: EditText
    private lateinit var dorsalText: EditText
    private lateinit var notaText: EditText
    private lateinit var btnSeleccionarPosicion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jugador)

        idEquipo = intent.getStringExtra("id") ?: return
        dorsalOriginal = intent.getStringExtra("dorsal")?.toIntOrNull() ?: 0

        nombreText = findViewById(R.id.nombretext)
        dorsalText = findViewById(R.id.dorsaltext)
        notaText = findViewById(R.id.notatext)
        btnSeleccionarPosicion = findViewById(R.id.btnSeleccionar)

        val btnGuardarJugador = findViewById<Button>(R.id.btnguardarjugador)
        val btnCancelar = findViewById<Button>(R.id.btncancelar)
        val btnEliminarJugador = findViewById<Button>(R.id.btneliminarjugador)

        // Cargar datos
        cargarDatosJugador()

        // Botón Guardar manual
        btnGuardarJugador.setOnClickListener {
            ejecutarGuardadoYSalir()
        }

        // Botón Cancelar (Aquí NO guardamos, volvemos sin más)
        btnCancelar.setOnClickListener {
            volverAPlantilla()
        }

        btnEliminarJugador.setOnClickListener {
            val jugadorReset = JugadorRemote(idEquipo, 0, "Nombre del jugador", "DC", "Nota")
            guardarEnNube(jugadorReset)
        }

        btnSeleccionarPosicion.setOnClickListener { mostrarDialogoPosiciones() }
    }

    // --- NUEVO: Controlar el botón atrás físico o gesto del sistema ---
    override fun onBackPressed() {
        // Al darle atrás, guardamos automáticamente antes de cerrar
        ejecutarGuardadoYSalir()
    }

    private fun ejecutarGuardadoYSalir() {
        val nuevoDorsal = dorsalText.text.toString().toIntOrNull() ?: 0

        if (nuevoDorsal !in 0..99) { // Permitimos 0 por si es reset
            mostrarErrorDorsal()
        } else {
            val jugadorEditado = JugadorRemote(
                equipoId = idEquipo,
                dorsal = nuevoDorsal,
                nombre = nombreText.text.toString(),
                posicion = btnSeleccionarPosicion.text.toString(),
                nota = notaText.text.toString()
            )
            guardarEnNube(jugadorEditado)
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
                    // Solo salimos si el servidor confirma el guardado
                    volverAPlantilla()
                } else {
                    Toast.makeText(this@Jugador, "Error en el servidor", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Jugador, "Fallo al guardar: revisa internet", Toast.LENGTH_SHORT).show()
                // Si falla el internet, salimos de todos modos para no bloquear al usuario
                volverAPlantilla()
            }
        }
    }

    private fun volverAPlantilla() {
        val intent = Intent(this, Plantilla::class.java).apply {
            putExtra("id", idEquipo)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Limpia el historial para no acumular pantallas
        }
        startActivity(intent)
        finish()
    }

    private fun mostrarDialogoPosiciones() {
        AlertDialog.Builder(this)
            .setTitle("Selecciona una posición")
            .setItems(posiciones) { _, which ->
                btnSeleccionarPosicion.text = posiciones[which]
            }
            .show()
    }

    private fun mostrarErrorDorsal() {
        AlertDialog.Builder(this)
            .setMessage("El dorsal debe ser un número del 1 al 99")
            .setPositiveButton("Aceptar", null)
            .show()
    }
}