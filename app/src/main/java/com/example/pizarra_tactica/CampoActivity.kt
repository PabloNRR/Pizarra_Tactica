package com.example.pizarra_tactica

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
class CampoActivity : AppCompatActivity() {

    // 1. Declaramos la variable a nivel de clase para que sea accesible en todo el archivo
    private lateinit var idEquipo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_campo)

        // 2. Le asignamos el valor del Intent (usamos idEquipo en lugar de val id)
        idEquipo = intent.getStringExtra("id") ?: return

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- OPCIÓN CAMPO ENTERO ---
        val campo: CardView = findViewById(R.id.campoo)
        campo.setOnClickListener {
            val intent = Intent(this, CampoEntero::class.java)
            intent.putExtra("id", idEquipo) // Usamos la variable de clase
            startActivity(intent)
        }

        // --- OPCIÓN MEDIO CAMPO ---
        val mediocampo: CardView = findViewById(R.id.campito)
        mediocampo.setOnClickListener {
            val intent = Intent(this, MedioCampo::class.java)
            intent.putExtra("id", idEquipo) // Usamos la variable de clase
            startActivity(intent)
        }

        // --- BOTÓN ESTRATEGIAS ---
        val btnEstrategias: Button = findViewById(R.id.btn_estrategias)
        btnEstrategias.setOnClickListener {
            val intent = Intent(this, EstrategiasActivity::class.java)
            intent.putExtra("id", idEquipo) // Usamos la variable de clase
            startActivity(intent)
        }
    }
}