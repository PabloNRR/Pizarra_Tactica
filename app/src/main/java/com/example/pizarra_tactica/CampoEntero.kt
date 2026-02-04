package com.example.pizarra_tactica

import android.content.Intent
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.pizarra_tactica.databinding.ActivityEnteroBinding
import kotlinx.coroutines.launch

class CampoEntero : AppCompatActivity() {

    private var modoBorradorActivo = false
    lateinit var jugada: ImageButton
    lateinit var jugador: ImageButton
    lateinit var brush: ImageButton
    lateinit var banquillo: ImageView
    lateinit var Menu: ImageButton
    lateinit var lineas: ImageButton
    lateinit var goma: ImageButton
    private lateinit var dibujoView: Dibujo
    private lateinit var campoView: ImageView
    private var dX = 0f
    private var dY = 0f
    private val opciones = arrayOf("Elegir equipo", "Elegir campo", "Guardar pizarra", "Salir sin guardar")
    private lateinit var binding: ActivityEnteroBinding
    private lateinit var shakeToUndo: ShakeToUndoManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEnteroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getStringExtra("id") ?: return

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar vistas
        val mainLayout = findViewById<ConstraintLayout>(R.id.main)
        dibujoView = findViewById(R.id.dibujoView)
        campoView = findViewById(R.id.campo)
        jugada = findViewById(R.id.jugada)
        jugador = findViewById(R.id.jugador)
        brush = findViewById(R.id.brush)
        banquillo = findViewById(R.id.banquillo)
        Menu = findViewById(R.id.Menu)
        lineas = findViewById(R.id.lineas)
        goma = findViewById(R.id.goma)

        shakeToUndo = ShakeToUndoManager(this) {
            dibujoView.undoLast()
            Toast.makeText(this, "Deshecho", Toast.LENGTH_SHORT).show()
        }

        // Configurar límites del dibujo
        campoView.post {
            val adjustedBounds = RectF(
                campoView.left.toFloat() + campoView.paddingLeft,
                campoView.top.toFloat() + campoView.paddingTop,
                campoView.right.toFloat() - campoView.paddingRight,
                campoView.bottom.toFloat() - campoView.paddingBottom
            )
            dibujoView.setDrawingBounds(adjustedBounds)
        }

        actualizarListaJugadores(id)
        actualizarColoresHerramientas(jugada) // Jugada activo por defecto

        // Listeners de herramientas
        Menu.setOnClickListener { mostraropciones(id) }

        jugada.setOnClickListener {
            modoBorradorActivo = false
            dibujoView.setDrawingEnabled(false)
            actualizarColoresHerramientas(jugada)
        }

        brush.setOnClickListener {
            modoBorradorActivo = false
            dibujoView.setDrawMode(Dibujo.DrawMode.FREE)
            dibujoView.setDrawingEnabled(true)
            actualizarColoresHerramientas(brush)
        }

        lineas.setOnClickListener {
            modoBorradorActivo = false
            dibujoView.setDrawMode(Dibujo.DrawMode.ARROW)
            dibujoView.setDrawingEnabled(true)
            actualizarColoresHerramientas(lineas)
        }

        goma.setOnClickListener {
            modoBorradorActivo = true
            dibujoView.setDrawMode(Dibujo.DrawMode.ERASE)
            dibujoView.setDrawingEnabled(true)
            actualizarColoresHerramientas(goma)
        }

        jugador.setOnClickListener {
            addPlayerToLayout(true, "")
            dibujoView.setDrawingEnabled(false)
        }

        // Cargar estrategia si existe
        val datosGuardados = intent.getStringExtra("DATOS_ESTRATEGIA")
        if (datosGuardados != null) {
            mainLayout.post { reconstruirPizarra(datosGuardados) }
        }
    }

    private fun actualizarColoresHerramientas(herramientaActiva: ImageButton) {
        val colorAzul = android.graphics.Color.BLUE
        val colorNegro = android.graphics.Color.BLACK
        val botones = listOf(brush, goma, lineas, jugada)
        botones.forEach { it.setColorFilter(if (it == herramientaActiva) colorAzul else colorNegro, android.graphics.PorterDuff.Mode.SRC_IN) }
    }

    private fun estaJugadorEnCampo(dorsal: String): Boolean {
        if (dorsal.isEmpty()) return false
        val mainLayout = findViewById<ConstraintLayout>(R.id.main)
        for (i in 0 until mainLayout.childCount) {
            val vista = mainLayout.getChildAt(i)
            if (vista is ConstraintLayout && vista.id == View.NO_ID) {
                val tvDorsal = vista.getChildAt(1) as? TextView
                if (tvDorsal?.text.toString().trim() == dorsal.trim()) return true
            }
        }
        return false
    }

    private fun addPlayerToLayout(esrojo: Boolean, dorsal: String) {
        if (estaJugadorEnCampo(dorsal)) {
            Toast.makeText(this, "El jugador $dorsal ya está en el campo", Toast.LENGTH_SHORT).show()
            return
        }

        val playerLayout = ConstraintLayout(this)
        val playerviewparams = ConstraintLayout.LayoutParams(100, 100)
        playerLayout.layoutParams = playerviewparams

        val newPlayer = ImageView(this)
        newPlayer.setImageDrawable(ContextCompat.getDrawable(this, if (esrojo) R.drawable.baseline_circle_24 else R.drawable.baseline_circle_24_blue))
        newPlayer.layoutParams = ConstraintLayout.LayoutParams(100, 100)

        val numberTextView = TextView(this).apply {
            text = dorsal
            textSize = 20f
            setTextColor(ContextCompat.getColor(context, R.color.white))
            setPadding(24, 20, 4, 2)
            layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT).apply {
                leftToLeft = playerLayout.id
                topToTop = playerLayout.id
            }
        }

        playerLayout.addView(newPlayer)
        playerLayout.addView(numberTextView)

        val mainLayout = findViewById<ConstraintLayout>(R.id.main)
        mainLayout.addView(playerLayout)

        // --- CAMBIO PARA CENTRADO EXACTO ---
        // Calculamos el centro restando la mitad del tamaño de la ficha (50px)
        // para que el centro del círculo coincida con el centro de la pantalla.
        playerLayout.x = (mainLayout.width / 2f) - 50f
        playerLayout.y = (mainLayout.height / 2f) - 50f

        setDraggable(playerLayout)
    }

    private fun setDraggable(view: View) {
        view.setOnTouchListener { v, event ->
            if (modoBorradorActivo) {
                if (event.action == MotionEvent.ACTION_DOWN) (v.parent as ConstraintLayout).removeView(v)
                return@setOnTouchListener true
            }
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    v.animate().x(event.rawX + dX).y(event.rawY + dY).setDuration(0).start()
                }
            }
            true
        }
    }

    private fun reconstruirPizarra(datos: String) {
        val mainLayout = findViewById<ConstraintLayout>(R.id.main)
        datos.split(";").forEach { info ->
            val partes = info.split(":")
            if (partes.size == 3) {
                colocarJugadorGuardado(partes[0], partes[1].toFloat() * mainLayout.width, partes[2].toFloat() * mainLayout.height)
            }
        }
    }

    private fun colocarJugadorGuardado(dorsal: String, pX: Float, pY: Float) {
        val playerLayout = ConstraintLayout(this)
        playerLayout.layoutParams = ConstraintLayout.LayoutParams(100, 100)

        val newPlayer = ImageView(this).apply {
            setImageResource(R.drawable.baseline_circle_24_blue)
            layoutParams = ConstraintLayout.LayoutParams(100, 100)
        }

        val tv = TextView(this).apply {
            text = dorsal
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.white))
            gravity = android.view.Gravity.CENTER
            layoutParams = ConstraintLayout.LayoutParams(100, 100)
        }

        playerLayout.addView(newPlayer)
        playerLayout.addView(tv)
        findViewById<ConstraintLayout>(R.id.main).addView(playerLayout)
        playerLayout.x = pX
        playerLayout.y = pY
        setDraggable(playerLayout)
    }

    private fun mostraropciones(id: String) {
        AlertDialog.Builder(this).setTitle("Opciones").setItems(arrayOf("Guardar Pizarra")) { _, _ ->
            solicitarNombreYPizarra(id)
        }.show()
    }

    private fun solicitarNombreYPizarra(idEquipo: String) {
        val input = android.widget.EditText(this)
        AlertDialog.Builder(this).setTitle("Guardar Estrategia").setView(input).setPositiveButton("Guardar") { _, _ ->
            val nombre = input.text.toString()
            if (nombre.isNotEmpty()) capturarYEnviarEstrategia(idEquipo, nombre)
        }.setNegativeButton("Cancelar", null).show()
    }

    private fun capturarYEnviarEstrategia(idEquipo: String, nombre: String) {
        val mainLayout = findViewById<ConstraintLayout>(R.id.main)
        val listaPosiciones = mutableListOf<String>()

        // 1. Recorremos los elementos del campo para capturar las posiciones
        for (i in 0 until mainLayout.childCount) {
            val vista = mainLayout.getChildAt(i)

            if (vista is ConstraintLayout && vista.id == View.NO_ID) {
                val tvDorsal = vista.getChildAt(1) as? TextView
                val dorsal = tvDorsal?.text.toString()

                val posX = vista.x / mainLayout.width
                val posY = vista.y / mainLayout.height
                listaPosiciones.add("$dorsal:$posX:$posY")
            }
        }

        val datosSerializados = listaPosiciones.joinToString(";")

        // 2. Envío a la nube con Corrutinas
        lifecycleScope.launch {
            try {
                // El tercer parámetro cambia a "entero"
                val estrategia = EstrategiaRemote(
                    equipo_id = idEquipo,
                    nombre = nombre,
                    tipo_campo = "entero",
                    datos_jugadores = datosSerializados
                )

                val response = RetrofitClient.instance.guardarEstrategia(estrategia)
                if (response.isSuccessful) {
                    Toast.makeText(this@CampoEntero, "¡Pizarra '$nombre' guardada!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CampoEntero, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarListaJugadores(id: String) {
        val contenedor = findViewById<LinearLayout>(R.id.banquilloContenedor)
        contenedor.removeAllViews()
        lifecycleScope.launch {
            try {
                RetrofitClient.instance.obtenerJugadores(id).forEach { jugador ->
                    val tv = TextView(this@CampoEntero).apply {
                        text = "${jugador.dorsal}  "
                        textSize = 40f
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                        setOnClickListener { addPlayerToLayout(false, jugador.dorsal.toString()) }
                    }
                    contenedor.addView(tv)
                }
            } catch (e: Exception) { /* Error handling */ }
        }
    }

    override fun onResume() { super.onResume(); shakeToUndo.start() }
    override fun onPause() { super.onPause(); shakeToUndo.stop() }
}