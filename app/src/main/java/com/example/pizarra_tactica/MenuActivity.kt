package com.example.pizarra_tactica

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MenuActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FROM_LOGIN = "EXTRA_FROM_LOGIN"
    }

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Solo forzamos logout si la app se ha abierto "en frío"
        // (NO si venimos del login/register)
        val fromLogin = intent.getBooleanExtra(EXTRA_FROM_LOGIN, false)
        if (!fromLogin) {
            auth.signOut()
            getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()
        }

        // crear ficheros si no existen
        crearFicheroBDEquipos(this)
        crearFicheroBDTacticas(this)
        crearFicheroBDPlantillas(this)

        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        mainLayout.setOnClickListener {
            if (auth.currentUser == null) {
                startActivity(Intent(this, Login::class.java))
            } else {
                startActivity(Intent(this, ElegirEquipo::class.java))
            }
        }

        // prueba Retrofit (si lo quieres mantener)
        lifecycleScope.launch {
            try {
                val respuesta = RetrofitClient.instance.obtenerEquipos()
                println("CONEXIÓN EXITOSA: Se han traído ${respuesta.size} equipos")
            } catch (e: Exception) {
                println("ERROR DE CONEXIÓN: ${e.message}")
            }
        }
    }

    private fun crearFicheroBDEquipos(context: Context) {
        val nombreFichero = "BDEQUIPOS.txt"
        val ruta = context.filesDir
        val fichero = File(ruta, nombreFichero)

        if (!fichero.exists()) {
            try {
                val fos = FileOutputStream(fichero)

                val textoIngresado = "Añadir Equipo"
                val imageUri = Uri.parse("android.resource://${context.packageName}/drawable/addescudo")

                for (i in 0..19) {
                    val id = "C$i"
                    val linea = "$id-$textoIngresado-${imageUri}\n"
                    fos.write(linea.toByteArray())
                }

                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun crearFicheroBDTacticas(context: Context) {
        val nombreFichero = "BDTACTICAS.txt"
        val ruta = context.filesDir
        val fichero = File(ruta, nombreFichero)

        if (!fichero.exists()) {
            try {
                val fos = FileOutputStream(fichero)
                fos.write("".toByteArray())
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun crearFicheroBDPlantillas(context: Context) {
        val nombreFichero = "BDPLANTILLAS.txt"
        val ruta = context.filesDir
        val fichero = File(ruta, nombreFichero)

        if (!fichero.exists()) {
            try {
                val fos = FileOutputStream(fichero)
                val writer = fos.bufferedWriter()

                for (i in 0..19) {
                    for (j in 0..23) {
                        writer.write("C$i-0-Nombre del jugador-DC-Nota\n")
                    }
                }

                writer.close()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
