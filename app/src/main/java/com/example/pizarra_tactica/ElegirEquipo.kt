package com.example.pizarra_tactica

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.util.Log

class ElegirEquipo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_elegirequipo)

        // Botón estático para crear un equipo nuevo
        findViewById<ImageButton>(R.id.btn_añadir_nuevo).setOnClickListener {
            val intent = Intent(this, Plantilla::class.java).apply {
                // Enviamos "NUEVO" para que la Plantilla sepa que está empezando de cero
                putExtra("id", "NUEVO")
                putExtra("nombre", "Nuevo Equipo")
            }
            startActivity(intent)
        }

        // Botón para cerrar sesión
        findViewById<ImageButton>(R.id.btn_logout).setOnClickListener {
            // 1. Cerramos la sesión en Firebase
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

            // 2. Limpiamos la "memoria" local de la tablet
            getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()

            // 3. Volvemos a la pantalla de Login
            val intent = Intent(this, Login::class.java)
            // Estas flags sirven para que no se pueda volver atrás con el botón del móvil
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        findViewById<ImageButton>(R.id.btn_profile).setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }

    }

    override fun onResume() {
        super.onResume()
        consultarEquiposALaNube()
    }

    private fun consultarEquiposALaNube() {
        val contenedor = findViewById<LinearLayout>(R.id.contenedorEquipos)
        // 1. Obtenemos el usuario que inició sesión
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val uid = user?.uid // Este es el ID único de ese correo

        if (uid == null) {
            // Si no hay nadie logueado, lo mandamos de vuelta al Login
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                // 2. Pasamos el UID a la consulta GET
                val equiposRemote = RetrofitClient.instance.obtenerEquipos(uid)
                actualizarInterfaz(contenedor, equiposRemote)
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error: ${e.message}")
            }
        }
    }

    private fun actualizarInterfaz(contenedor: LinearLayout, equipos: List<EquipoRemote>) {
        if (contenedor.childCount > 1) {
            contenedor.removeViews(1, contenedor.childCount - 1)
        }

        equipos.forEach { equipo ->
            if (equipo.nombre != "Añadir Equipo" && equipo.nombre != "Nuevo Equipo") {
                val nuevoBoton = ImageButton(this).apply {
                    layoutParams = LinearLayout.LayoutParams(700, 700).apply {
                        setMargins(40, 0, 40, 0)
                    }
                    scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                    background = null
                }

                // --- CAMBIO AQUÍ: Forzamos la actualización ---
                Glide.with(this)
                    .load(equipo.imageUri)
                    .placeholder(R.drawable.addescudo)
                    // Esto obliga a Glide a refrescar la imagen si el archivo cambió
                    .signature(com.bumptech.glide.signature.ObjectKey(System.currentTimeMillis().toString()))
                    .into(nuevoBoton)

                nuevoBoton.setOnClickListener {
                    val intent = Intent(this, Plantilla::class.java).apply {
                        putExtra("id", equipo.id)
                        putExtra("nombre", equipo.nombre)
                        putExtra("imageUri", equipo.imageUri)
                    }
                    startActivity(intent)
                }
                contenedor.addView(nuevoBoton)
            }
        }
    }
}