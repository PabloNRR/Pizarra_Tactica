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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Crear el fichero BDEQUIPOS si no existe
        crearFicheroBDEquipos(this)
        crearFicheroBDTacticas(this)
        crearFicheroBDPlantillas(this)

        // Agregar OnClickListener al ConstraintLayout principal
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        mainLayout.setOnClickListener {
            val intent = Intent(this, ElegirEquipo::class.java)
            startActivity(intent)
        }

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

                // Generar 20 líneas con IDs incrementales
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
