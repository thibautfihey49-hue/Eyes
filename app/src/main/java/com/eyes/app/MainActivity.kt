package com.eyes.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    private val REQUIRED_PERMISSIONS = mutableListOf<String>().apply {
        add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
        }
    }.toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 100, 40, 40)
        layout.setBackgroundColor(0xFF121212.toInt())

        // Titre
        val title = TextView(this)
        title.text = "👁️ EYES — CAMÉRA CACHÉE"
        title.textSize = 22f
        title.setTextColor(0xFF4CAF50.toInt())
        title.setPadding(0, 0, 0, 30)
        layout.addView(title)

        // Statut
        statusText = TextView(this)
        statusText.text = "✅ Prêt — Vérification des autorisations..."
        statusText.textSize = 14f
        statusText.setTextColor(0xFFFFFFFF.toInt())
        statusText.setPadding(0, 0, 0, 40)
        layout.addView(statusText)

        // Bouton Démarrer
        startButton = Button(this)
        startButton.text = "▶ DÉMARRER LA CAMÉRA"
        startButton.setBackgroundColor(0xFF2196F3.toInt())
        startButton.setTextColor(0xFFFFFFFF.toInt())
        startButton.setPadding(20, 30, 20, 30)
        startButton.setOnClickListener { startCameraService() }
        layout.addView(startButton)

        // Bouton Arrêter
        stopButton = Button(this)
        stopButton.text = "⏹ ARRÊTER"
        stopButton.setBackgroundColor(0xFFE91E63.toInt())
        stopButton.setTextColor(0xFFFFFFFF.toInt())
        stopButton.setPadding(20, 30, 20, 30)
        stopButton.setOnClickListener { stopCameraService() }
        stopButton.isEnabled = false
        layout.addView(stopButton)

        // Infos
        val info = TextView(this)
        info.text = "\n📸 Photos sauvegardées dans dossier caché\n🔵 Service actif même en arrière-plan\n📂 Stockage invisible dans .hidden_eyes"
        info.textSize = 12f
        info.setTextColor(0xFFAAAAAA.toInt())
        layout.addView(info)

        setContentView(layout)

        // Vérifier les autorisations
        checkPermissions()
    }

    private fun checkPermissions() {
        val missing = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            statusText.text = "⚠️ Demande des autorisations..."
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 1001)
        } else {
            statusText.text = "✅ Toutes autorisations accordées — Prêt !"
        }
    }

    private fun startCameraService() {
        if (!hasPermissions()) {
            statusText.text = "❌ Autorisation caméra manquante !"
            return
        }

        val intent = Intent(this, CameraService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }

        statusText.text = "✅ Service CAMÉRA DÉMARRÉ !\n📸 Photos sauvegardées automatiquement"
        startButton.isEnabled = false
        stopButton.isEnabled = true
    }

    private fun stopCameraService() {
        val intent = Intent(this, CameraService::class.java)
        stopService(intent)

        statusText.text = "⏹ Service arrêté"
        startButton.isEnabled = true
        stopButton.isEnabled = false
    }

    private fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                statusText.text = "✅ Toutes autorisations accordées — Prêt !"
            } else {
                statusText.text = "❌ Certaines autorisations sont refusées !"
            }
        }
    }
}
