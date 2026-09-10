package com.eyes.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraService : Service() {

    companion object {
        const val CHANNEL_ID = "EYES_CAMERA_CHANNEL"
        const val TAG = "EYES-CAMERA"
    }

    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var hiddenDir: File? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initHiddenStorage()
        initCamera()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1001, buildNotification())
        takePhoto()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        closeCamera()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "EYES Caméra",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Service de prise de photos en arrière-plan"
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EYES — Caméra active")
            .setContentText("Prise de photos en cours...")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun initHiddenStorage() {
        val docsDir = ContextCompat.getExternalFilesDirs(this, null).firstOrNull()
        hiddenDir = File(docsDir, ".hidden_eyes")
        if (!hiddenDir!!.exists()) {
            hiddenDir!!.mkdirs()
        }
        Log.d(TAG, "📂 Dossier caché: ${hiddenDir!!.absolutePath}")
    }

    private fun initCamera() {
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameras = cameraManager!!.cameraIdList
            cameraId = cameras.firstOrNull()
            Log.d(TAG, "📷 Caméra trouvée: $cameraId")
        } catch (e: CameraAccessException) {
            Log.e(TAG, "❌ Erreur caméra: ${e.message}")
        }
    }

    private fun takePhoto() {
        try {
            if (cameraId == null) return

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(Date())
            val photoFile = File(hiddenDir, "eyes_photo_${timestamp}.jpg")

            Log.d(TAG, "📸 Photo: ${photoFile.absolutePath}")

            // Simuler la prise — dans version avancée on utilise CameraX
            photoFile.createNewFile()
            photoFile.writeText("EYES_PHOTO_PLACEHOLDER\n${Date()}\nCaméra active\n")

            Log.d(TAG, "✅ Sauvegardée: ${photoFile.name}")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur prise photo: ${e.message}")
        }
    }

    private fun closeCamera() {
        Log.d(TAG, "🔵 Service arrêté")
    }
}
