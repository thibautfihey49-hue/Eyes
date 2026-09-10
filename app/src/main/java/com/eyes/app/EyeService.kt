package com.eyes.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat

class EyeService : Service() {
    private val binder = LocalBinder()
    private var isRunning = false
    private var wakeLock: android.os.PowerManager.WakeLock? = null
    private var wifiLock: android.net.wifi.WifiManager.WifiLock? = null

    companion object {
        const val CHANNEL_ID = "EyeServiceChannel"
        const val NOTIFICATION_ID = 1337
    }

    inner class LocalBinder : android.os.Binder() {
        fun getService(): EyeService = this@EyeService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireLocks()
        startForeground(NOTIFICATION_ID, buildNotification())
        isRunning = true
        Toast.makeText(this, "✅ EYES Service démarré", Toast.LENGTH_SHORT).show()
    }

    private fun acquireLocks() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        wakeLock = powerManager.newWakeLock(
            android.os.PowerManager.PARTIAL_WAKE_LOCK,
            "EYES::KeepRunning"
        ).apply { acquire(10*60*1000L) }

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
        wifiLock = wifiManager.createWifiLock(
            android.net.wifi.WifiManager.WIFI_MODE_FULL_HIGH_PERF,
            "EYES::WifiLock"
        ).apply { acquire() }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "EYES Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Service caméra P2P en cours" }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("👁️ EYES — Actif")
            .setContentText("En attente de connexion — La caméra est prête")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    fun isRunning() = isRunning

    fun stopService() {
        isRunning = false
        wakeLock?.release()
        wifiLock?.release()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder = binder
    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }
}
