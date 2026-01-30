package com.example.pizarra_tactica

import android.content.Intent
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pizarra_tactica.databinding.ActivityEnteroBinding
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.io.File
import android.widget.Toast

class CampoEntero : AppCompatActivity() {

    lateinit var jugada: ImageButton
    lateinit var jugador: ImageButton
    lateinit var balon: ImageButton
    lateinit var brush: ImageButton
    lateinit var banquillo: ImageView
    lateinit var editar: ImageButton
    lateinit var play: ImageButton
    lateinit var pausar: ImageButton
    lateinit var palante: ImageButton
    lateinit var patra: ImageButton
    lateinit var guardar: ImageButton
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
        setContentView(R.layout.activity_entero)
        binding = ActivityEnteroBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val id = intent.getStringExtra("id") ?: return

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mainLayout = findViewById<ConstraintLayout>(R.id.main)
        dibujoView = findViewById(R.id.dibujoView)
        campoView = findViewById(R.id.campo)
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
        shakeToUndo = ShakeToUndoManager(this) {
            dibujoView.undoLast()
            Toast.makeText(this, "Undo", Toast.LENGTH_SHORT).show()
        }



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


        //PRUEBAS----------------------------------
        val invisible = INVISIBLE
        val visible = VISIBLE

        actualizarListaJugadores(id)

        jugada.setOnClickListener {
            jugada.visibility = invisible
            jugador.visibility = invisible
            balon.visibility = invisible
            banquillo.visibility = invisible
            editar.visibility = visible
            play.visibility = visible
            pausar.visibility = visible
            palante.visibility = visible
            patra.visibility = visible
            guardar.visibility = visible

        }
        guardar.setOnClickListener {
            jugada.visibility = visible
            jugador.visibility = visible
            balon.visibility = visible
            banquillo.visibility = visible
            editar.visibility = invisible
            play.visibility = invisible
            pausar.visibility = invisible
            palante.visibility = invisible
            patra.visibility = invisible
            guardar.visibility = invisible

        }

        //--------------------------------------------------------------

        brush.setOnClickListener {
            dibujoView.setDrawMode(Dibujo.DrawMode.FREE)
            dibujoView.setDrawingEnabled(true)
        }

        goma.setOnClickListener {
            dibujoView.setDrawMode(Dibujo.DrawMode.ERASE)
            dibujoView.setDrawingEnabled(true)
        }

        lineas.setOnClickListener {
            dibujoView.setDrawMode(Dibujo.DrawMode.ARROW)
            dibujoView.setDrawingEnabled(true)
        }

        Menu.setOnClickListener {
            mostraropciones(id)
            dibujoView.setDrawingEnabled(false)
        }
        jugada.setOnClickListener {
            dibujoView.setDrawingEnabled(false)
        }
        jugador.setOnClickListener {
            addPlayerToLayout(true, "")
            dibujoView.setDrawingEnabled(false)
        }
        editar.setOnClickListener {
            dibujoView.setDrawingEnabled(false)
        }
        play.setOnClickListener {
            dibujoView.setDrawingEnabled(false)
        }
        pausar.setOnClickListener {
            dibujoView.setDrawingEnabled(false)
        }
        palante.setOnClickListener {
            dibujoView.setDrawingEnabled(false)
        }
        patra.setOnClickListener {
            dibujoView.setDrawingEnabled(false)
        }
        guardar.setOnClickListener {
            dibujoView.setDrawingEnabled(false)
        }

        for (i in 0 until mainLayout.childCount) {
            val child = mainLayout.getChildAt(i)
            // Haz que solo las vistas de tipo ImageView sean movibles
            if (isMovableView(child)==true) {
                setDraggable(child)
            }
        }
    }

    private fun mostraropciones(id: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("MENU")
        builder.setItems(opciones) { dialog, which ->
            val seleccion = opciones[which]
            when (seleccion) {
                "Elegir equipo" -> {
                    val intent = Intent(this, ElegirEquipo::class.java)
                    startActivity(intent)
                }
                "Elegir campo" -> {
                    val intent = Intent(this, CampoActivity::class.java)
                    intent.putExtra("id", id) // Pasar el ID al CampoEntero
                    startActivity(intent)
                }
                "Guardar pizarra" -> {
                    val intent = Intent(this, MenuActivity::class.java)
                    startActivity(intent)
                }
                "Salir sin guardar" -> {
                    val intent = Intent(this, MenuActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        builder.show()
    }

    //------------------------------------------------------------------

    private fun isMovableView(view: View): Boolean {
        return (view is ImageView && view.id != R.id.campo && view.id != R.id.banquillo && view.id != R.id.jugada && view.id != R.id.Menu && view.id != R.id.jugador && view.id != R.id.lineas && view.id != R.id.goma && view.id != R.id.editar && view.id != R.id.play && view.id != R.id.pausar && view.id != R.id.palante && view.id != R.id.patra && view.id != R.id.guardar && view.id != R.id.brush)// Excluye el fondo del campo y el banquillo
    }

    private fun setDraggable(view: View) {
        view.setOnTouchListener { v, event ->
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

    private fun actualizarListaJugadores(id: String) {
        // Obtener el archivo en el directorio interno
        val archivo = File(filesDir, "BDPLANTILLAS.txt")
        if (!archivo.exists()) return // Si el archivo no existe, salir

        // Leer el contenido del archivo
        val lineas = archivo.readLines()

        // Procesar cada línea y actualizar los TextViews
        lineas.forEachIndexed { index, linea ->
            val partes = linea.split("-")
            if (partes.size < 5) return@forEachIndexed // Ignorar líneas mal formadas

            val idequipo = partes[0]
            val dorsal = partes[1]

            // Asegurarse de que el ID del equipo coincida con el actual
            if (idequipo != id) return@forEachIndexed

            // Formatear el texto para el TextView correspondiente
            val textoFormateado = String.format(
                "%2s  ",
                dorsal,
            )

            // Acceder al TextView directamente usando su ID
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

            // Configurar el OnClickListener para cada TextView
            textView?.setOnClickListener {
                val dorsal = textView.text.toString() // Obtener el texto del dorsal
                addPlayerToLayout(false, dorsal) //jugador azul por false
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