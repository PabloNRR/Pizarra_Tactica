package com.example.pizarra_tactica

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.io.File
import android.provider.MediaStore

class Plantilla : AppCompatActivity() {

    private var fotoUri: Uri? = null
    private lateinit var idEquipo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plantilla)

        idEquipo = intent.getStringExtra("id") ?: return

        val nombreEditText: EditText = findViewById(R.id.Text)
        val btnEscudo: ImageButton = findViewById(R.id.btnescudo)
        val btnCampo: ImageButton = findViewById(R.id.btncampo)
        val btnHome: ImageButton = findViewById(R.id.btnhome)
        val btnEliminar: ImageButton = findViewById(R.id.btntrash)

        // 1. Ponemos el nombre que viene del Intent DE INMEDIATO
        val nombreIntent = intent.getStringExtra("----")
        nombreEditText.setText(nombreIntent)

        // 1. Configuramos el aspecto de los TextViews de inmediato (con texto vacío o "...")
        prepararInterfazJugadores()

        // 1. CARGAR DATOS DESDE LA API (En lugar de leer BDEQUIPOS.txt)
        cargarDatosDesdeNube(idEquipo, nombreEditText, btnEscudo)

        btnHome.setOnClickListener {
            // Al pulsar Home, primero guarda y luego navega
            modificarEquipoEnNube {
                val intent = Intent(this, ElegirEquipo::class.java)
                startActivity(intent)
                finish() // Importante cerrar la actual
            }
        }

        btnCampo.setOnClickListener {
            modificarEquipoEnNube {
                val intent = Intent(this, CampoActivity::class.java)
                intent.putExtra("id", idEquipo)
                startActivity(intent)
            }
        }

        btnEscudo.setOnClickListener { abrirOpcionesImagen() }

        btnEliminar.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Confirmación de eliminación")
                .setMessage("¿Desea resetear este equipo y sus jugadores?")
                .setPositiveButton("Sí") { _, _ -> eliminarEquipoEnNube(idEquipo) }
                .setNegativeButton("No", null)
                .show()
        }
    }

    override fun onBackPressed() {
        // Cuando el usuario intenta salir con el gesto de atrás:
        modificarEquipoEnNube {
            super.onBackPressed() // Esto cierra la actividad después de guardar
        }
    }

    private fun prepararInterfazJugadores() {
        val textViews = arrayOf(
            R.id.Jug1, R.id.Jug2, R.id.Jug3, R.id.Jug4, R.id.Jug5, R.id.Jug6,
            R.id.Jug7, R.id.Jug8, R.id.Jug9, R.id.Jug10, R.id.Jug11, R.id.Jug12,
            R.id.Jug13, R.id.Jug14, R.id.Jug15, R.id.Jug16, R.id.Jug17, R.id.Jug18,
            R.id.Jug19, R.id.Jug20, R.id.Jug21, R.id.Jug22, R.id.Jug23, R.id.Jug24
        )

        for (id in textViews) {
            val tv = findViewById<TextView>(id)
            tv.typeface = android.graphics.Typeface.MONOSPACE
            tv.text = " -- | ---------           --" // Texto de espera con el mismo ancho
        }
    }

    private fun cargarDatosDesdeNube(id: String, etNombre: EditText, btnEscudo: ImageButton) {
        lifecycleScope.launch {
            try {
                // 1. Obtenemos la lista de la API
                val lista = RetrofitClient.instance.obtenerEquipos()

                // 2. Buscamos nuestro equipo
                val equipo = lista.find { it.id == id }

                equipo?.let {
                    etNombre.setText(it.nombre)
                    // Importante: usamos it.imageUri de tu clase EquipoRemote
                    Glide.with(this@Plantilla)
                        .load(it.imageUri)
                        .placeholder(R.drawable.addescudo) // Imagen mientras carga
                        .into(btnEscudo)
                    btnEscudo.tag = it.imageUri
                }

                // 3. Cargamos los jugadores usando el ID del equipo
                val jugadores = RetrofitClient.instance.obtenerJugadores(id)
                pintarJugadoresEnInterfaz(jugadores)

            } catch (e: Exception) {
                Toast.makeText(this@Plantilla, "Error al sincronizar con la nube", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pintarJugadoresEnInterfaz(jugadores: List<JugadorRemote>) {
        val textViews = arrayOf(
            R.id.Jug1, R.id.Jug2, R.id.Jug3, R.id.Jug4, R.id.Jug5, R.id.Jug6,
            R.id.Jug7, R.id.Jug8, R.id.Jug9, R.id.Jug10, R.id.Jug11, R.id.Jug12,
            R.id.Jug13, R.id.Jug14, R.id.Jug15, R.id.Jug16, R.id.Jug17, R.id.Jug18,
            R.id.Jug19, R.id.Jug20, R.id.Jug21, R.id.Jug22, R.id.Jug23, R.id.Jug24
        )

        jugadores.forEachIndexed { index, j ->
            if (index < textViews.size) {
                val tv = findViewById<TextView>(textViews[index])
                val texto = String.format(" %2d | %-20s  %3s", j.dorsal, j.nombre.take(20), j.posicion.take(3))
                tv.typeface = android.graphics.Typeface.MONOSPACE
                tv.text = texto
                tv.setOnClickListener {
                    modificarEquipoEnNube {
                        val intent = Intent(this, Jugador::class.java).apply {
                            putExtra("id", idEquipo)
                            putExtra("dorsal", j.dorsal.toString()) // Convertimos Int a String para el Intent
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun modificarEquipoEnNube(onComplete: () -> Unit) {
        val nombre = findViewById<EditText>(R.id.Text).text.toString()
        val uri = findViewById<ImageButton>(R.id.btnescudo).tag?.toString() ?: ""

        val equipoActualizado = EquipoRemote(idEquipo, nombre, uri)

        lifecycleScope.launch {
            try {
                // Llamamos a la API y esperamos la respuesta del servidor
                val respuesta = RetrofitClient.instance.guardarEquipo(equipoActualizado)

                if (respuesta.isSuccessful) {
                    // SOLO cuando el servidor confirma que guardó, ejecutamos la navegación
                    onComplete()
                } else {
                    Toast.makeText(this@Plantilla, "Error al guardar en la nube", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@Plantilla, "Sin conexión: no se guardaron los cambios", Toast.LENGTH_SHORT).show()
                // Opcional: onComplete() si quieres que salga de todos modos aunque falle
            }
        }
    }

    private fun eliminarEquipoEnNube(id: String) {
        lifecycleScope.launch {
            try {
                // El reset en la nube lo hacemos enviando los valores por defecto
                val equipoReset = EquipoRemote(id, "Añadir Equipo", "android.resource://$packageName/drawable/addescudo")
                RetrofitClient.instance.guardarEquipo(equipoReset)

                // Aquí podrías añadir una ruta en la API para borrar jugadores,
                // o simplemente resetear el equipo como estás haciendo.

                startActivity(Intent(this@Plantilla, ElegirEquipo::class.java))
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@Plantilla, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- LÓGICA DE IMAGEN (Se mantiene similar pero actualiza el .tag para el POST) ---

    private val launcherSelector = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val dataGaleria = result.data?.data
            val uriFinal = if (dataGaleria != null) copiarImagenAGuardar(dataGaleria, idEquipo) else fotoUri

            uriFinal?.let { uri ->
                val btnEscudo: ImageButton = findViewById(R.id.btnescudo)
                Glide.with(this).load(uri).into(btnEscudo)
                btnEscudo.tag = uri.toString()
                // Guardamos automáticamente en la nube al cambiar la imagen
                modificarEquipoEnNube {}
            }
        }
    }

    private fun abrirOpcionesImagen() {
        val intentGaleria = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        fotoUri = crearUriTemporal()
        val intentCamara = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { putExtra(MediaStore.EXTRA_OUTPUT, fotoUri) }
        val chooser = Intent.createChooser(intentGaleria, "Selecciona el escudo").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(intentCamara))
        }
        launcherSelector.launch(chooser)
    }

    private fun crearUriTemporal(): Uri {
        val archivo = File(filesDir, "escudo_$idEquipo.jpg")
        return androidx.core.content.FileProvider.getUriForFile(this, "${packageName}.fileprovider", archivo)
    }

    private fun copiarImagenAGuardar(uriOrigen: Uri, equipoId: String): Uri? {
        return try {
            contentResolver.openInputStream(uriOrigen)?.use { input ->
                File(filesDir, "escudo_$equipoId.jpg").outputStream().use { output -> input.copyTo(output) }
            }
            crearUriTemporal()
        } catch (e: Exception) { null }
    }
}