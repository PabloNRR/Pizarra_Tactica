package com.example.pizarra_tactica

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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

        // 1. GESTIÓN DE SESIÓN
        val fromLogin = intent.getBooleanExtra(EXTRA_FROM_LOGIN, false)
        if (!fromLogin) {
            auth.signOut()
            getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()
        }

        // 2. ELIMINAMOS LA CREACIÓN DE FICHEROS .txt
        // Ya no necesitamos crearFicheroBDEquipos, etc. porque la estructura está en SQL.

        // 3. NAVEGACIÓN
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        mainLayout.setOnClickListener {
            if (auth.currentUser == null) {
                startActivity(Intent(this, Login::class.java))
            } else {
                // Al hacer clic, vamos directamente a elegir equipo, que cargará desde la API
                startActivity(Intent(this, ElegirEquipo::class.java))
            }
        }

        // 4. VERIFICACIÓN DE SALUD DE LA API
        // Esto sirve para confirmar que la tablet tiene internet y la VM está encendida
        verificarConexionNube()
    }

    private fun verificarConexionNube() {
        lifecycleScope.launch {
            try {
                val respuesta = RetrofitClient.instance.obtenerEquipos()
                // Si llegamos aquí, la conexión es correcta
                android.util.Log.d("API_CHECK", "Nube lista: ${respuesta.size} equipos detectados")
            } catch (e: Exception) {
                android.util.Log.e("API_CHECK", "Error de conexión a la nube: ${e.message}")
                // Opcional: Mostrar un Toast si la VM está apagada
                // Toast.makeText(this@MenuActivity, "Sin conexión al servidor táctico", Toast.LENGTH_LONG).show()
            }
        }
    }
}