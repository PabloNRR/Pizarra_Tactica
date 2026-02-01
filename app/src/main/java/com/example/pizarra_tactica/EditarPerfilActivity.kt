package com.example.pizarra_tactica

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.UnknownHostException
import kotlin.math.abs

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var actCountry: MaterialAutoCompleteTextView
    private lateinit var tvNote: TextView
    private lateinit var progress: ProgressBar

    private lateinit var adapter: ArrayAdapter<String>
    private val countryItems = mutableListOf(
        "ES - España",
        "IT - Italia",
        "FR - Francia",
        "DE - Alemania",
        "UK - Reino Unido",
        "US - Estados Unidos"
    )

    private val requestLocationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val granted = (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                    (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true)

            if (granted) {
                detectCountryFromGps()
            } else {
                tvNote.text = "No has dado permisos de ubicación. Elige el país manualmente."
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_perfil)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, Login::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        findViewById<TextView>(R.id.tvEmail).text = "Email: ${user.email ?: "(sin email)"}"
        findViewById<TextView>(R.id.tvUid).text = "UID: ${user.uid}"

        actCountry = findViewById(R.id.actCountry)
        tvNote = findViewById(R.id.tvNote)
        progress = findViewById(R.id.progress)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, countryItems)
        actCountry.setAdapter(adapter)
        actCountry.setText(countryItems.first(), false)

        // Volver
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        // Logout
        findViewById<ImageButton>(R.id.btn_logout_profile).setOnClickListener {
            doLogoutToMenu()
        }

        // Detectar país
        findViewById<MaterialButton>(R.id.btnDetectCountry).setOnClickListener {
            if (hasLocationPermission()) {
                detectCountryFromGps()
            } else {
                requestLocationPermissions.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }

        // Guardar (por ahora solo UI; luego BD)
        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {
            val selection = actCountry.text?.toString().orEmpty()
            val code = selection.take(2)
            Toast.makeText(this, "Guardado (pendiente BD): $code", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun doLogoutToMenu() {
        FirebaseAuth.getInstance().signOut()
        // Por si guardaste token u otros datos
        getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()

        startActivity(Intent(this, MenuActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        val coarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    private fun detectCountryFromGps() {
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager

        val gpsEnabled = runCatching { lm.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false)
        val netEnabled = runCatching { lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false)

        if (!gpsEnabled && !netEnabled) {
            tvNote.text = "Ubicación desactivada. Activa GPS/Ubicación para recomendar país."
            Toast.makeText(this, "Activa la ubicación", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        tvNote.text = "Obteniendo ubicación..."

        // 1) Intentamos lastKnown (rápido)
        val last = listOfNotNull(
            if (gpsEnabled) lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) else null,
            if (netEnabled) lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) else null
        ).maxByOrNull { it.time }

        // Si la última ubicación es reciente (< 2 min), la usamos
        if (last != null && abs(System.currentTimeMillis() - last.time) < 2 * 60 * 1000) {
            callReverseGeocode(last)
            return
        }

        // 2) Si no, pedimos una actualización “one-shot” con timeout
        val handler = Handler(Looper.getMainLooper())
        val timeoutMs = 10_000L

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                lm.removeUpdates(this)
                handler.removeCallbacksAndMessages(null)
                callReverseGeocode(location)
            }
        }

        val provider = when {
            netEnabled -> LocationManager.NETWORK_PROVIDER
            else -> LocationManager.GPS_PROVIDER
        }

        lm.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())

        handler.postDelayed({
            lm.removeUpdates(listener)
            setLoading(false)
            tvNote.text = "No se pudo obtener ubicación (timeout). Puedes elegir el país manualmente."
            Toast.makeText(this, "No se pudo obtener ubicación", Toast.LENGTH_SHORT).show()
        }, timeoutMs)
    }

    private fun callReverseGeocode(location: Location) {
        val lat = location.latitude
        val lon = location.longitude

        tvNote.text = "Llamando a la API para obtener país..."
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    BigDataCloudClient.api.reverseGeocode(
                        latitude = lat,
                        longitude = lon,
                        localityLanguage = "es"
                    )
                }

                val code = resp.countryCode?.uppercase().orEmpty()
                val name = resp.countryName.orEmpty()

                if (code.isBlank() || name.isBlank()) {
                    setLoading(false)
                    tvNote.text = "No se pudo obtener el país desde la API. Elige manualmente."
                    Toast.makeText(this@EditarPerfilActivity, "Respuesta incompleta de la API", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Selecciona automáticamente
                val item = "$code - $name"
                if (!countryItems.contains(item)) {
                    countryItems.add(0, item)
                    adapter.notifyDataSetChanged()
                }
                actCountry.setText(item, false)

                tvNote.text = "Tu localización actual es $name ($code). Te lo hemos seleccionado; puedes cambiarlo."
                Toast.makeText(this@EditarPerfilActivity, "País recomendado: $name", Toast.LENGTH_SHORT).show()
                setLoading(false)

            } catch (e: Exception) {
                setLoading(false)
                val msg = when (e) {
                    is UnknownHostException, is IOException ->
                        "Sin conexión a Internet (o la red bloquea la API)."
                    else ->
                        "Error al llamar a la API: ${e.localizedMessage ?: "desconocido"}"
                }
                tvNote.text = msg
                Toast.makeText(this@EditarPerfilActivity, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        findViewById<MaterialButton>(R.id.btnDetectCountry).isEnabled = !loading
        findViewById<MaterialButton>(R.id.btnSave).isEnabled = !loading
    }
}
