package com.example.pizarra_tactica

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        // 1. GESTIÓN DE SESIÓN (MEJORADA)
        // Solo cerramos sesión si queremos forzar un logout.
        // Si ya hay un usuario, lo dejamos pasar.
        if (auth.currentUser == null) {
            // Si no hay usuario, aseguramos que los SharedPreferences estén limpios
            getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()
        }

        // 2. NAVEGACIÓN
        val mainLayout: ConstraintLayout = findViewById(R.id.main)
        mainLayout.setOnClickListener {
            if (auth.currentUser == null) {
                startActivity(Intent(this, Login::class.java))
            } else {
                startActivity(Intent(this, ElegirEquipo::class.java))
            }
        }

        // 3. VERIFICACIÓN DE SALUD
        verificarConexionNube()
    }

    private fun verificarConexionNube() {
        // Obtenemos el UID actual si existe, o usamos uno de "test" para el ping
        val uidParaCheck = auth.currentUser?.uid ?: "ping_check"

        lifecycleScope.launch {
            try {
                // AHORA PASAMOS EL UID (Soluciona el error en rojo)
                val respuesta = RetrofitClient.instance.obtenerEquipos(uidParaCheck)
                Log.d("API_CHECK", "Nube lista: Conexión establecida")
            } catch (e: Exception) {
                Log.e("API_CHECK", "Error de conexión a la nube: ${e.message}")
            }
        }
    }
}