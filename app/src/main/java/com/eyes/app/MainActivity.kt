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
        layout.setPadding(50, 100, 50, 50)
        layout.setBackgroundColor(0xFF121212.toInt())
        
        val text = TextView(this)
        text.text = "✅ EYES FONCTIONNE !"
        text.textSize = 24f
        text.setTextColor(0xFFFFFFFF.toInt())
        
        layout.addView(text)
        setContentView(layout)
    }
}
