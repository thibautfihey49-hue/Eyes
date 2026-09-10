package com.eyes
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.eyes.p2p.P2PClient
import com.eyes.p2p.P2PServer
class MainActivity : AppCompatActivity() {
    private lateinit var tvMyId: TextView
    private lateinit var etTargetId: EditText
    private lateinit var btnConnect: Button
    private lateinit var btnDisconnect: Button
    private lateinit var ivVideo: ImageView
    private lateinit var tvStatus: TextView
    private var server: P2PServer? = null
    private var client: P2PClient? = null
    private val MY_ID = getOrGenerateMyId()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvMyId = findViewById(R.id.tvMyId)
        etTargetId = findViewById(R.id.etTargetId)
        btnConnect = findViewById(R.id.btnConnect)
        btnDisconnect = findViewById(R.id.btnDisconnect)
        ivVideo = findViewById(R.id.ivVideo)
        tvStatus = findViewById(R.id.tvStatus)
        tvMyId.text = "🔗 MON ID : $MY_ID"
        requestPermissions()
        startServerMode()
        btnConnect.setOnClickListener { connectToTarget() }
        btnDisconnect.setOnClickListener { disconnect() }
    }

    private fun requestPermissions() {
        val needed = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            needed.add(Manifest.permission.CAMERA)
        if (needed.isNotEmpty()) ActivityCompat.requestPermissions(this, needed.toTypedArray(), 100)
    }

    private fun startServerMode() {
        server = P2PServer(this, MY_ID)
        server!!.start { runOnUiThread { tvStatus.text = "📡 Connecté → Envoi de ma caméra" } }
        tvStatus.text = "✅ En attente de connexion..."
    }

    private fun connectToTarget() {
        val targetId = etTargetId.text.toString().trim()
        if (targetId.isEmpty()) { tvStatus.text = "⚠️ Entre un ID d'abord !"; return }
        tvStatus.text = "🔍 Connexion en cours..."
        client = P2PClient(this)
        val wifi = applicationContext.getSystemService(android.content.Context.WIFI_SERVICE) as android.net.wifi.WifiManager
        val ip = wifi.connectionInfo.ipAddress
        val prefix = String.format("%d.%d.%d", ip and 0xFF, ip shr 8 and 0xFF, ip shr 16 and 0xFF)
        val targetIp = "$prefix.${(targetId.hashCode() and 0xFF) + 2}"
        client!!.connect(targetIp) { bitmap ->
            runOnUiThread { ivVideo.setImageBitmap(bitmap); tvStatus.text = "✅ En direct depuis $targetId" }
        }
    }

    private fun disconnect() {
        client?.disconnect()
        ivVideo.setImageResource(android.R.color.transparent)
        tvStatus.text = "⏹️ Déconnecté"
    }

    private fun getOrGenerateMyId(): String {
        val prefs = getSharedPreferences("Eyes", MODE_PRIVATE)
        var id = prefs.getString("my_id", null)
        if (id == null) {
            val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
            id = List(4) { chars.random() }.joinToString("") + "-" + List(4) { chars.random() }.joinToString("")
            prefs.edit().putString("my_id", id).apply()
        }
        return id!!
    }

    override fun onDestroy() { super.onDestroy(); server?.stop(); client?.disconnect() }
}
