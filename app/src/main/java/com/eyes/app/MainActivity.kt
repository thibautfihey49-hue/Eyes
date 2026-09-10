package com.eyes.app

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var tvMyId: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private var eyeService: EyeService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as EyeService.LocalBinder
            eyeService = binder.getService()
            isBound = true
            updateUI()
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            eyeService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvMyId = findViewById(R.id.tvMyId)
        tvStatus = findViewById(R.id.tvStatus)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        btnStart.setOnClickListener { startService() }
        btnStop.setOnClickListener { stopService() }

        generateAndShowId()
    }

    private fun generateAndShowId() {
        val prefs = getSharedPreferences("EyesPrefs", Context.MODE_PRIVATE)
        var myId = prefs.getString("my_id", null)
        if (myId == null) {
            val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
            val part1 = (1..4).map { chars.random() }.joinToString("")
            val part2 = (1..4).map { chars.random() }.joinToString("")
            myId = "$part1-$part2"
            prefs.edit().putString("my_id", myId).apply()
        }
        tvMyId.text = "TON ID : $myId"
    }

    private fun checkPermissions(): Boolean {
        val needed = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            needed.add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                needed.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), 100)
            return false
        }
        return true
    }

    private fun startService() {
        if (!checkPermissions()) return
        
        val intent = Intent(this, EyeService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        Toast.makeText(this, "✅ EYES actif — tourne en arrière-plan !", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun stopService() {
        eyeService?.stopService()
        if (isBound) unbindService(connection)
        stopService(Intent(this, EyeService::class.java))
        tvStatus.text = "⏹️ Arrêté"
        btnStart.isEnabled = true
        btnStop.isEnabled = false
        Toast.makeText(this, "⏹️ EYES arrêté", Toast.LENGTH_SHORT).show()
    }

    private fun updateUI() {
        if (eyeService?.isRunning() == true) {
            tvStatus.text = "✅ ACTIF — En attente de connexion"
            btnStart.isEnabled = false
            btnStop.isEnabled = true
        }
    }

    override fun onStart() {
        super.onStart()
        if (isBound) updateUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) unbindService(connection)
        isBound = false
    }
}
