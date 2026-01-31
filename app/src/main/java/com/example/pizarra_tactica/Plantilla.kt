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
import com.google.firebase.auth.FirebaseAuth // <--- IMPORTANTE
import kotlinx.coroutines.launch
import java.io.File
import android.provider.MediaStore

class Plantilla : AppCompatActivity() {

    private var fotoUri: Uri? = null
    private lateinit var idEquipo: String

    // Propiedad para obtener el ID del usuario actual de forma rápida
    private val currentUid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plantilla)

        val idRecibido = intent.getStringExtra("id") ?: return

        if (idRecibido == "NUEVO") {
            idEquipo = "EQ_" + System.currentTimeMillis()
            findViewById<EditText>(R.id.Text).setText("Nuevo Equipo")
        } else {
            idEquipo = idRecibido
            cargarDatosDesdeNube(idEquipo, findViewById(R.id.Text), findViewById(R.id.btnescudo))
        }

        val btnEscudo: ImageButton = findViewById(R.id.btnescudo)
        val btnCampo: ImageButton = findViewById(R.id.btncampo)
        val btnHome: ImageButton = findViewById(R.id.btnhome)
        val btnEliminar: ImageButton = findViewById(R.id.btntrash)

        btnHome.setOnClickListener {
            verificarYGuardar {
                val intent = Intent(this, ElegirEquipo::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }

        btnCampo.setOnClickListener {
            verificarYGuardar {
                val intent = Intent(this, CampoActivity::class.java)
                intent.putExtra("id", idEquipo)
                startActivity(intent)
            }
        }

        btnEscudo.setOnClickListener { abrirOpcionesImagen() }

        btnEliminar.setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("Confirmación")
                .setMessage("¿Desea eliminar este equipo por completo?")
                .setPositiveButton("Sí") { _, _ -> eliminarEquipoEnNube(idEquipo) }
                .setNegativeButton("No", null)
                .show()
        }
    }

    // --- REFINADO DEL SELECTOR DE IMAGEN ---

    private val launcherSelector = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val dataGaleria = result.data?.data
            val uriFinal = if (dataGaleria != null) copiarImagenAGuardar(dataGaleria, idEquipo) else fotoUri

            uriFinal?.let { uri ->
                val btnEscudo: ImageButton = findViewById(R.id.btnescudo)
                Glide.with(this)
                    .load(uri)
                    .signature(com.bumptech.glide.signature.ObjectKey(System.currentTimeMillis().toString()))
                    .into(btnEscudo)

                btnEscudo.tag = uri.toString()

                modificarEquipoEnNube {
                    Toast.makeText(this, "Escudo actualizado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // --- LÓGICA DE PERSISTENCIA ACTUALIZADA ---

    private fun verificarYGuardar(onSuccess: () -> Unit) {
        val nombre = findViewById<EditText>(R.id.Text).text.toString().trim()
        val uriTag = findViewById<ImageButton>(R.id.btnescudo).tag?.toString() ?: ""

        if (nombre.isEmpty() || nombre == "Nuevo Equipo") {
            Toast.makeText(this, "Introduce un nombre válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (uriTag.isEmpty() || uriTag.contains("addescudo")) {
            Toast.makeText(this, "Debes seleccionar un escudo", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // PASO CLAVE: Filtrar por el usuario actual para validar duplicados
                val lista = RetrofitClient.instance.obtenerEquipos(currentUid)
                val duplicado = lista.any { it.nombre.equals(nombre, ignoreCase = true) && it.id != idEquipo }

                if (duplicado) {
                    Toast.makeText(this@Plantilla, "Ya existe un equipo con ese nombre", Toast.LENGTH_SHORT).show()
                } else {
                    modificarEquipoEnNube { onSuccess() }
                }
            } catch (e: Exception) {
                onSuccess()
            }
        }
    }

    override fun onBackPressed() {
        verificarYGuardar { super.onBackPressed() }
    }

    private fun cargarDatosDesdeNube(id: String, etNombre: EditText, btnEscudo: ImageButton) {
        lifecycleScope.launch {
            try {
                // PASO CLAVE: Obtener solo los equipos del usuario logueado
                val lista = RetrofitClient.instance.obtenerEquipos(currentUid)
                val equipo = lista.find { it.id == id }

                equipo?.let {
                    etNombre.setText(it.nombre)
                    Glide.with(this@Plantilla)
                        .load(it.imageUri)
                        .placeholder(R.drawable.addescudo)
                        .signature(com.bumptech.glide.signature.ObjectKey(System.currentTimeMillis().toString()))
                        .into(btnEscudo)
                    btnEscudo.tag = it.imageUri
                }

                val jugadores = RetrofitClient.instance.obtenerJugadores(id)
                pintarJugadoresEnInterfaz(jugadores)

            } catch (e: Exception) {
                Toast.makeText(this@Plantilla, "Error al sincronizar", Toast.LENGTH_SHORT).show()
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
                            putExtra("dorsal", j.dorsal.toString())
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

        // PASO CLAVE: Incluir el currentUid en el objeto
        val equipoActualizado = EquipoRemote(idEquipo, nombre, uri, currentUid)

        lifecycleScope.launch {
            try {
                val respuesta = RetrofitClient.instance.guardarEquipo(equipoActualizado)
                if (respuesta.isSuccessful) onComplete()
            } catch (e: Exception) {
                Toast.makeText(this@Plantilla, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eliminarEquipoEnNube(id: String) {
        lifecycleScope.launch {
            try {
                // PASO CLAVE: Resetear enviando el UID para que el servidor sepa de quién es
                val equipoReset = EquipoRemote(
                    id,
                    "Añadir Equipo",
                    "android.resource://$packageName/drawable/addescudo",
                    currentUid
                )
                RetrofitClient.instance.guardarEquipo(equipoReset)

                startActivity(Intent(this@Plantilla, ElegirEquipo::class.java))
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@Plantilla, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- MÉTODOS DE IMAGEN (Se mantienen igual) ---
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