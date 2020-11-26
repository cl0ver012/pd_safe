package com.example.pdsafe

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.pdsafe.databinding.ActivityMainBinding

class SpiralDetect : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var classifier: Classifier =
        Classifier(this, "spiral_model.tflite", "spiral_labels.txt")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.canvas.apply {
            setColor(resources.getColor(R.color.purple_700))
            setStrokeWidth(40.0f)
            setAlpha(60)
        }


        binding.classifyButton.setOnClickListener {
            val t1 = System.currentTimeMillis()
            classify()
            val t2 = System.currentTimeMillis()
            Log.e("Time taken", (t2 - t1).toString())
            Toast.makeText(this, "Time taken=" + (t2 - t1).toString() + "ms", Toast.LENGTH_SHORT)
                .show()
            binding.canvas.clearCanvas()
        }
        classifier.initialise()
            .addOnFailureListener {
                Log.e("Mainactivity", "Error", it)
            }

    }

    override fun onDestroy() {
        classifier.close()
        super.onDestroy()
    }

    fun classify() {
        var bitmap = binding.canvas.getBitmap()
        classifier.classifyAsync(bitmap)
            .addOnSuccessListener { result ->
                if(result.contains("0"))
                    binding.results.text="Negative"
                else
                    binding.results.text="Positive"
                binding.results.visibility = View.VISIBLE
                Log.e("Draw", result)
            }
            .addOnFailureListener {
                Log.e("MainActivity", "Fail", it)
                binding.results.text = "Fail"
            }
    }
}