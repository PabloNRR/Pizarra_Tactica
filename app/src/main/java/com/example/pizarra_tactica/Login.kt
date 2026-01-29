package com.example.pizarra_tactica

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pizarra_tactica.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener { doLogin() }

        binding.btnGoRegister.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }
    }

    private fun doLogin() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val pass = binding.etPassword.text?.toString().orEmpty()

        binding.tvError.visibility = View.GONE

        if (email.isBlank() || pass.isBlank()) {
            showError("Rellena email y contraseña.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Email no válido (ej: pablo@gmail.com).")
            return
        }

        setLoading(true)

        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                auth.currentUser?.getIdToken(true)
                    ?.addOnSuccessListener { result ->
                        val token = result.token.orEmpty()
                        getSharedPreferences("auth", MODE_PRIVATE)
                            .edit()
                            .putString("firebase_id_token", token)
                            .apply()
                        goToMenu()
                    }
                    ?.addOnFailureListener { goToMenu() }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                val msg = when (e) {
                    is FirebaseNetworkException ->
                        "Sin conexión a Internet (o la red bloquea Firebase)."
                    is FirebaseAuthInvalidUserException ->
                        "Email o contraseña incorrectos."
                    is FirebaseAuthInvalidCredentialsException ->
                        "Email o contraseña incorrectos."
                    else ->
                        "Error al iniciar sesión: ${e.localizedMessage ?: "desconocido"}"
                }
                showError(msg)
            }
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
        binding.btnGoRegister.isEnabled = !loading
    }

    private fun showError(msg: String) {
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun goToMenu() {
        val intent = Intent(this, ElegirEquipo::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

}
