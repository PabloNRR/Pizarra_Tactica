package com.example.pizarra_tactica

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CampoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_campo)

        val id = intent.getStringExtra("id") ?: return

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val campo: CardView =findViewById(R.id.campoo)
        campo.setOnClickListener{
            val intent: Intent = Intent(this,CampoEntero::class.java)
            intent.putExtra("id", id) // Pasar el ID al CampoEntero
            startActivity(intent)
        }
        val mediocampo: CardView =findViewById(R.id.campito)
        mediocampo.setOnClickListener{
            val intent: Intent = Intent(this,MedioCampo::class.java)
            intent.putExtra("id", id) // Pasar el ID al MedioCampo
            startActivity(intent)
        }
    }
}