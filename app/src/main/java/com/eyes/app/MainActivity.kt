package com.eyes.app

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 150, 50, 50)
        layout.setBackgroundColor(0xFF121212.toInt())
        
        val titre = TextView(this)
        titre.text = "✅ EYES — INSTALLATION RÉUSSIE !"
        titre.textSize = 22f
        titre.setTextColor(0xFF4CAF50.toInt())
        titre.setPadding(0, 0, 0, 40)
        layout.addView(titre)
        
        val info = TextView(this)
        info.text = "Tout est en ordre ✅\n\nFichiers présents :\n• MainActivity.kt ✅\n• AndroidManifest.xml ✅\n• Ressources ✅\n• Autorisations ✅\n\nPrêt pour les prochaines fonctionnalités !"
        info.textSize = 16f
        info.setTextColor(0xFFFFFFFF.toInt())
        layout.addView(info)
        
        setContentView(layout)
    }
}
