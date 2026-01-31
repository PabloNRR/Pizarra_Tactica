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
    }

    private fun toggleVisibility(vararg views: View, makeVisible: Boolean) {
        val visibility = if (makeVisible) VISIBLE else INVISIBLE
        views.forEach { it.visibility = visibility }
    }

    private fun isMovableView(view: View): Boolean {
        return (view is ImageView && view.id != R.id.mediocampo && view.id != R.id.banquillo && view.id != R.id.jugada && view.id != R.id.Menu && view.id != R.id.jugador && view.id != R.id.lineas && view.id != R.id.goma && view.id != R.id.editar && view.id != R.id.play && view.id != R.id.pausar && view.id != R.id.palante && view.id != R.id.patra && view.id != R.id.guardar && view.id != R.id.brush)
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
        builder.setTitle("MENU")
        builder.setItems(opciones) { dialog, which ->
            when (opciones[which]) {
                "Elegir equipo" -> startActivity(Intent(this, ElegirEquipo::class.java))
                "Elegir campo" -> startActivity(Intent(this, CampoActivity::class.java).putExtra("id", id))
                "Guardar pizarra" -> startActivity(Intent(this, CampoActivity::class.java))
                "Salir sin guardar" -> startActivity(Intent(this, MenuActivity::class.java))
            }
        }
        builder.show()
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
