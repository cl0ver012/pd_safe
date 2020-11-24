package com.example.pdsafe

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
    }

    fun spiralModel(view: View) {
        val intent = Intent(this, SpiralDetect::class.java)
        startActivity(intent)
    }

    fun waveModel(view: View) {
        val intent = Intent(this, WaveDetect::class.java)
        startActivity(intent)
    }

}