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
import com.example.pizarra_tactica.databinding.ActivityRegisterBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener { doRegister() }

        binding.btnGoLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }

    private fun doRegister() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val pass = binding.etPassword.text?.toString().orEmpty()
        val pass2 = binding.etPassword2.text?.toString().orEmpty()

        binding.tvError.visibility = View.GONE

        if (email.isBlank() || pass.isBlank() || pass2.isBlank()) {
            showError("Rellena todos los campos.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Email no válido (ej: pablo@gmail.com).")
            return
        }

        if (pass.length < 6) {
            showError("La contraseña debe tener al menos 6 caracteres.")
            return
        }

        if (pass != pass2) {
            showError("Las contraseñas no coinciden.")
            return
        }

        setLoading(true)

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                // El usuario YA queda logueado tras registrarse.
                // Guardamos token si queréis usarlo luego con el backend
                auth.currentUser?.getIdToken(true)
                    ?.addOnSuccessListener { result ->
                        val token = result.token.orEmpty()
                        getSharedPreferences("auth", MODE_PRIVATE)
                            .edit()
                            .putString("firebase_id_token", token)
                            .apply()

                        goToEditProfile()
                    }
                    ?.addOnFailureListener {
                        goToEditProfile()
                    }
            }
            .addOnFailureListener { e ->
                setLoading(false)
                val msg = when (e) {
                    is FirebaseNetworkException ->
                        "Sin conexión a Internet."
                    is FirebaseAuthUserCollisionException ->
                        "Ese email ya está registrado."
                    is FirebaseAuthWeakPasswordException ->
                        "Contraseña demasiado débil."
                    else ->
                        "Error al registrarse: ${e.localizedMessage ?: "desconocido"}"
                }
                showError(msg)
            }
    }

    private fun goToEditProfile() {
        val intent = Intent(this, EditarPerfilActivity::class.java).apply {
            putExtra(EditarPerfilActivity.EXTRA_FROM_REGISTER, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
        binding.btnGoLogin.isEnabled = !loading
    }

    private fun showError(msg: String) {
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
