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

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pizarra_tactica.databinding.ActivityMedioCampoBinding
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.TextView
import android.widget.Toast

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch



class MedioCampo : AppCompatActivity() {

    private var modoBorradorActivo = false // <--- NUEVA VARIABLE
    lateinit var jugada: ImageButton
    lateinit var jugador: ImageButton
    lateinit var balon: ImageButton
    lateinit var banquillo: ImageView
    lateinit var editar: ImageButton
    lateinit var play: ImageButton
    lateinit var pausar: ImageButton
    lateinit var palante: ImageButton
    lateinit var patra: ImageButton
    lateinit var guardar: ImageButton
    lateinit var Menu: ImageButton
    lateinit var brush: ImageButton
    lateinit var goma: ImageButton
    lateinit var lineas: ImageButton
    private val opciones = arrayOf("Elegir equipo", "Elegir campo", "Guardar pizarra", "Salir sin guardar")
    private lateinit var dibujoView: Dibujo
    private lateinit var campoView: ImageView
    private var dX = 0f
    private var dY = 0f
    private lateinit var binding: ActivityMedioCampoBinding
    private lateinit var shakeToUndo: ShakeToUndoManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_medio_campo)

        val id = intent.getStringExtra("id") ?: return
        actualizarListaJugadores(id)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityMedioCampoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mainLayout = findViewById<ConstraintLayout>(R.id.main)
        dibujoView = findViewById(R.id.dibujoView)
        shakeToUndo = ShakeToUndoManager(this) {
            dibujoView.undoLast()
            Toast.makeText(this, "Undo", Toast.LENGTH_SHORT).show()
        }
        campoView = findViewById(R.id.mediocampo)
        jugada = findViewById(R.id.jugada)
        jugador = findViewById(R.id.jugador)
        brush = findViewById(R.id.brush)
        banquillo = findViewById(R.id.banquillo)
        editar = findViewById(R.id.editar)
        play = findViewById(R.id.play)
        pausar = findViewById(R.id.pausar)
        palante = findViewById(R.id.palante)
        patra = findViewById(R.id.patra)
        guardar = findViewById(R.id.guardar)
        Menu = findViewById(R.id.Menu)
        lineas = findViewById(R.id.lineas)
        goma = findViewById(R.id.goma)

        // Get the bounds of the football field image
        campoView.post {
            val bounds = RectF(
                campoView.left.toFloat(),
                campoView.top.toFloat(),
                campoView.right.toFloat(),
                campoView.bottom.toFloat()
            )
            // Adjust the bounds to account for any potential padding or margins
            val paddingLeft = campoView.paddingLeft.toFloat()
            val paddingTop = campoView.paddingTop.toFloat()
            val paddingRight = campoView.paddingRight.toFloat()
            val paddingBottom = campoView.paddingBottom.toFloat()
            val adjustedBounds = RectF(
                bounds.left + paddingLeft,
                bounds.top + paddingTop,
                bounds.right - paddingRight,
                bounds.bottom - paddingBottom
            )
            dibujoView.setDrawingBounds(adjustedBounds)
        }

        actualizarListaJugadores(id)
        actualizarColoresHerramientas(jugada) // Pincel activo por defecto

        // Set onClick listeners for buttons
        Menu.setOnClickListener {
            mostraropciones(id)
            dibujoView.setDrawingEnabled(false)
        }

        jugada.setOnClickListener {
            modoBorradorActivo = false
            dibujoView.setDrawingEnabled(false)
            actualizarColoresHerramientas(jugada) // Ilumina jugada
        }

        brush.setOnClickListener {
            modoBorradorActivo = false
            dibujoView.setDrawMode(Dibujo.DrawMode.FREE)
            dibujoView.setDrawingEnabled(true)
            actualizarColoresHerramientas(brush) // Ilumina pincel
        }

        lineas.setOnClickListener {
            modoBorradorActivo = false
            dibujoView.setDrawMode(Dibujo.DrawMode.ARROW)
            dibujoView.setDrawingEnabled(true)
            actualizarColoresHerramientas(lineas) // Ilumina flechas
        }

        goma.setOnClickListener {
            modoBorradorActivo = true
            dibujoView.setDrawMode(Dibujo.DrawMode.ERASE)
            dibujoView.setDrawingEnabled(true)
            actualizarColoresHerramientas(goma) // Ilumina goma
        }

        lineas.setOnClickListener {
            modoBorradorActivo = false
            dibujoView.setDrawMode(Dibujo.DrawMode.ARROW)
            dibujoView.setDrawingEnabled(true)

            // Iluminamos las líneas y apagamos la goma
            actualizarColoresHerramientas(lineas)
        }

        jugador.setOnClickListener {
            addPlayerToLayout(true, "")
            dibujoView.setDrawingEnabled(false)
        }

        // Make movable views draggable
        for (i in 0 until mainLayout.childCount) {
            val child = mainLayout.getChildAt(i)
            if (isMovableView(child)) {
                setDraggable(child)
            }
        }

        val datosGuardados = intent.getStringExtra("DATOS_ESTRATEGIA")
        if (datosGuardados != null) {
            // Usamos post para asegurar que el campo ya tiene dimensiones
            findViewById<ConstraintLayout>(R.id.main).post {
                reconstruirPizarra(datosGuardados)
            }
        }
    }

    private fun reconstruirPizarra(datos: String) {
        val mainLayout = findViewById<ConstraintLayout>(R.id.main)

        // Separamos cada jugador por el punto y coma
        val jugadores = datos.split(";")

        jugadores.forEach { info ->
            val partes = info.split(":")
            if (partes.size == 3) {
                val dorsal = partes[0]
                // Convertimos el porcentaje de vuelta a píxeles según el tamaño de la pantalla actual
                val pX = partes[1].toFloat() * mainLayout.width
                val pY = partes[2].toFloat() * mainLayout.height

                colocarJugadorGuardado(dorsal, pX, pY)
            }
        }
    }

    private fun colocarJugadorGuardado(dorsal: String, pX: Float, pY: Float) {
        val playerLayout = ConstraintLayout(this)
        // 1. Parámetros del contenedor
        val paramsContenedor = ConstraintLayout.LayoutParams(100, 100)
        playerLayout.layoutParams = paramsContenedor

        val newPlayer = ImageView(this)
        newPlayer.setImageResource(R.drawable.baseline_circle_24_blue)
        // 2. Parámetros de la imagen (nombre distinto)
        val paramsImagen = ConstraintLayout.LayoutParams(100, 100)
        newPlayer.layoutParams = paramsImagen

        val numberTextView = TextView(this).apply {
            text = dorsal
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.white))
            gravity = android.view.Gravity.CENTER
            // 3. Parámetros del texto
            val paramsTexto = ConstraintLayout.LayoutParams(100, 100)
            layoutParams = paramsTexto
        }

        playerLayout.addView(newPlayer)
        playerLayout.addView(numberTextView)

        val mainLayout = findViewById<ConstraintLayout>(R.id.main)
        mainLayout.addView(playerLayout)

        playerLayout.x = pX
        playerLayout.y = pY

        setDraggable(playerLayout)
    }

    private fun toggleVisibility(vararg views: View, makeVisible: Boolean) {
        val visibility = if (makeVisible) VISIBLE else INVISIBLE
        views.forEach { it.visibility = visibility }
    }

    private fun isMovableView(view: View): Boolean {
        return (view is ImageView && view.id != R.id.mediocampo && view.id != R.id.banquillo && view.id != R.id.jugada && view.id != R.id.Menu && view.id != R.id.jugador && view.id != R.id.lineas && view.id != R.id.goma && view.id != R.id.editar && view.id != R.id.play && view.id != R.id.pausar && view.id != R.id.palante && view.id != R.id.patra && view.id != R.id.guardar && view.id != R.id.brush)
    }

    private fun estaJugadorEnCampo(dorsal: String): Boolean {
        // Si el dorsal está vacío (jugador genérico rojo), permitimos poner varios
        if (dorsal.isEmpty()) return false

        val mainLayout = findViewById<ConstraintLayout>(R.id.main)
        for (i in 0 until mainLayout.childCount) {
            val vista = mainLayout.getChildAt(i)

            // Buscamos los layouts de jugadores (son ConstraintLayouts que hemos añadido)
            if (vista is ConstraintLayout && vista.id == View.NO_ID) {
                // El TextView del dorsal es el segundo hijo (índice 1) según tu addPlayerToLayout
                val tvDorsal = vista.getChildAt(1) as? TextView
                if (tvDorsal?.text.toString().trim() == dorsal.trim()) {
                    return true // Encontrado, el jugador ya está en el campo
                }
            }
        }
        return false // No encontrado
    }

    private fun setDraggable(view: View) {
        view.setOnTouchListener { v, event ->
            // SI LA GOMA ESTÁ ACTIVA: Borramos el jugador al tocarlo
            if (modoBorradorActivo) {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val parent = v.parent as ConstraintLayout
                    parent.removeView(v) // Elimina el jugador (el playerLayout)
                }
                return@setOnTouchListener true
            }

            // SI LA GOMA NO ESTÁ ACTIVA: Lógica de arrastre normal
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    v.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                }
            }
            true
        }
    }

    private fun addPlayerToLayout(esrojo: Boolean, dorsal: String) {

        if (estaJugadorEnCampo(dorsal)) {
            Toast.makeText(this, "El jugador $dorsal ya está en el campo", Toast.LENGTH_SHORT).show()
            return // Cortamos la ejecución aquí
        }

        //creamos un constraintlayout que contenga la bola y el dorsal
        val playerLayout = ConstraintLayout(this)

        // 1. Crear un nuevo ImageView dinámicamente
        val newPlayer = ImageView(this)

        // 2. Asignar el Drawable al jugador
        val playerDrawable: Drawable? = if (esrojo){
            ContextCompat.getDrawable(this, R.drawable.baseline_circle_24)  // Usa un drawable existente
        } else{
            ContextCompat.getDrawable(this, R.drawable.baseline_circle_24_blue)  // Usa un drawable existente
        }
        if (playerDrawable != null) {
            newPlayer.setImageDrawable(playerDrawable)
        } else {
            // Manejar el caso en que el drawable no se encuentra
            return
        }

        val numberTextView = TextView(this)
        numberTextView.text = dorsal
        numberTextView.textSize = 20f // Tamaño del texto
        numberTextView.setTextColor(ContextCompat.getColor(this, R.color.white)) // Color blanco
        numberTextView.setPadding(24, 20, 4, 2) // Espaciado interno

        //editamos el layout del jugador
        val playerviewparams = ConstraintLayout.LayoutParams(
            100,
            100
        )
        playerviewparams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
        playerviewparams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID


        //guardamos los datos en el layout
        playerLayout.layoutParams = playerviewparams

        // 3. Establecer los parámetros de tamaño
        val imageViewParams = ConstraintLayout.LayoutParams(
            100, // Ancho del jugador (más grande)
            100  // Alto del jugador (más grande)
        )
        // 4. Establecer la posición inicial (en la mitad izquierda)
        imageViewParams.leftToLeft = playerLayout.id
        imageViewParams.topToTop = playerLayout.id

        // 5. Aplicar los parámetros al jugador
        newPlayer.layoutParams = imageViewParams



        //hacemos lo mismo con el textview
        val textViewParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        textViewParams.leftToRight = playerLayout.id
        textViewParams.topToBottom = playerLayout.id

        numberTextView.layoutParams = textViewParams


        //añadimos todo al constraint layout
        playerLayout.addView(newPlayer)
        playerLayout.addView(numberTextView)

        // 6. Añadir el jugador al layout principal
        val mainLayout = findViewById<ConstraintLayout>(R.id.main)

        // Ajusta los márgenes para posicionarlo
        playerviewparams.leftMargin = (mainLayout.width / 2)
        playerviewparams.topMargin = (mainLayout.height / 4)
        //guardamos los datos en el layout
        playerLayout.layoutParams = playerviewparams

        mainLayout.addView(playerLayout)

        // 7. Hacer al jugador arrastrable
        setDraggable(playerLayout)
    }

    // Añade esta función al final de tu clase MedioCampo.kt
    private fun actualizarColoresHerramientas(herramientaActiva: ImageButton) {
        val colorAzul = android.graphics.Color.BLUE
        val colorNegro = android.graphics.Color.BLACK

        // 1. Añadimos todos los botones que queremos que "brillen"
        val botones = listOf(brush, goma, lineas, jugada)

        botones.forEach { boton ->
            if (boton == herramientaActiva) {
                // Aplicamos el tinte azul al icono
                boton.setColorFilter(colorAzul, android.graphics.PorterDuff.Mode.SRC_IN)
            } else {
                // Limpiamos el tinte (vuelve a negro o color original)
                boton.setColorFilter(colorNegro, android.graphics.PorterDuff.Mode.SRC_IN)
            }
        }
    }

    private fun mostraropciones(id: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Opciones de Pizarra")
        val opcionesMenu = arrayOf("Guardar Pizarra") // Solo esta opción

        builder.setItems(opcionesMenu) { _, _ ->
            solicitarNombreYPizarra(id)
        }
        builder.show()
    }

    private fun solicitarNombreYPizarra(idEquipo: String) {
        val input = android.widget.EditText(this)
        input.hint = "Nombre de la jugada (ej: Corner 1)"

        AlertDialog.Builder(this)
            .setTitle("Guardar Estrategia")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nombreJugada = input.text.toString()
                if (nombreJugada.isNotEmpty()) {
                    capturarYEnviarEstrategia(idEquipo, nombreJugada)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun capturarYEnviarEstrategia(idEquipo: String, nombre: String) {
        val mainLayout = findViewById<ConstraintLayout>(R.id.main)
        val listaPosiciones = mutableListOf<String>()

        // 1. Recorremos los elementos del campo para capturar las posiciones
        for (i in 0 until mainLayout.childCount) {
            val vista = mainLayout.getChildAt(i)

            // Filtramos solo los Layouts de los jugadores (sin ID y de tipo ConstraintLayout)
            if (vista is ConstraintLayout && vista.id == View.NO_ID) {
                val tvDorsal = vista.getChildAt(1) as? TextView
                val dorsal = tvDorsal?.text.toString()

                // Cálculo de posición relativa (0.0 a 1.0) para que sea responsive
                val posX = vista.x / mainLayout.width
                val posY = vista.y / mainLayout.height
                listaPosiciones.add("$dorsal:$posX:$posY")
            }
        }

        val datosSerializados = listaPosiciones.joinToString(";")

        // 2. Envío a la nube con Corrutinas
        lifecycleScope.launch {
            try {
                // Creamos el objeto con los 4 parámetros de tu data class
                val estrategia = EstrategiaRemote(
                    equipo_id = idEquipo,
                    nombre = nombre,
                    tipo_campo = "medio",
                    datos_jugadores = datosSerializados
                )

                val response = RetrofitClient.instance.guardarEstrategia(estrategia)
                if (response.isSuccessful) {
                    Toast.makeText(this@MedioCampo, "¡Pizarra '$nombre' guardada!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MedioCampo, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actualizarListaJugadores(id: String) {
        val contenedor = findViewById<LinearLayout>(R.id.banquilloContenedor)
        contenedor.removeAllViews()

        // 1. LANZAMOS LA CORRUTINA (Esto quita el error en rojo)
        lifecycleScope.launch {
            try {
                // 2. Ahora sí podemos llamar a la función suspendida
                val listaJugadores = RetrofitClient.instance.obtenerJugadores(id)

                // 3. Pintamos los dorsales en el banquillo
                listaJugadores.forEach { jugador ->
                    val tv = TextView(this@MedioCampo).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(20, 0, 20, 0)
                        }
                        text = jugador.dorsal.toString()
                        textSize = 40f
                        setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.white))

                        setOnClickListener {
                            addPlayerToLayout(false, jugador.dorsal.toString())
                        }
                    }
                    contenedor.addView(tv)
                }
            } catch (e: Exception) {
                // Siempre es bueno avisar si algo falla
                android.widget.Toast.makeText(this@MedioCampo, "Error al cargar banquillo", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        shakeToUndo.start()
    }

    override fun onPause() {
        super.onPause()
        shakeToUndo.stop()
    }

}
